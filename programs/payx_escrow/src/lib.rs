use anchor_lang::prelude::*;
use anchor_spl::token::{self, Mint, Token, TokenAccount, Transfer};

declare_id!("11111111111111111111111111111111");
// Placeholder program ID. Replaced by `anchor keys sync` on first deploy;
// backend PAYX_PROGRAM_ID must be updated to match (see docs/contracts.md).

pub const ESCROW_TIMEOUT_SECONDS: i64 = 24 * 60 * 60;

#[program]
pub mod payx_escrow {
    use super::*;

    pub fn initialize_escrow(
        ctx: Context<InitializeEscrow>,
        escrow_id: u64,
        receiver: Pubkey,
        amount: u64,
    ) -> Result<()> {
        require!(amount > 0, PayxError::ZeroAmount);
        require_keys_eq!(
            ctx.accounts.mint.key(),
            ctx.accounts.vault.mint,
            PayxError::BadMint
        );

        let escrow = &mut ctx.accounts.escrow;
        escrow.id = escrow_id;
        escrow.sender = ctx.accounts.sender.key();
        escrow.receiver = receiver;
        escrow.mint = ctx.accounts.mint.key();
        escrow.amount = amount;
        escrow.operator = ctx.accounts.operator.key();
        escrow.state = EscrowState::Initialized;
        escrow.deposit_timestamp = 0;
        escrow.bump = ctx.bumps.escrow;

        emit!(EscrowInitialized {
            escrow_id,
            sender: escrow.sender,
            receiver,
            mint: escrow.mint,
            amount,
            operator: escrow.operator,
        });

        Ok(())
    }

    pub fn deposit(ctx: Context<Deposit>) -> Result<()> {
        let escrow = &mut ctx.accounts.escrow;
        require!(
            escrow.state == EscrowState::Initialized,
            PayxError::BadState
        );
        require!(
            ctx.accounts.authority.key() == escrow.sender
                || ctx.accounts.authority.key() == escrow.operator,
            PayxError::Unauthorized
        );
        require_keys_eq!(ctx.accounts.from.mint, escrow.mint, PayxError::BadMint);
        require_keys_eq!(ctx.accounts.vault.mint, escrow.mint, PayxError::BadMint);

        token::transfer(
            CpiContext::new(
                ctx.accounts.token_program.to_account_info(),
                Transfer {
                    from: ctx.accounts.from.to_account_info(),
                    to: ctx.accounts.vault.to_account_info(),
                    authority: ctx.accounts.authority.to_account_info(),
                },
            ),
            escrow.amount,
        )?;

        escrow.state = EscrowState::Deposited;
        escrow.deposit_timestamp = Clock::get()?.unix_timestamp;

        emit!(EscrowDeposited {
            escrow_id: escrow.id,
            sender: escrow.sender,
            receiver: escrow.receiver,
            amount: escrow.amount,
        });

        Ok(())
    }

    pub fn confirm_funding(ctx: Context<Transition>) -> Result<()> {
        require_keys_eq!(
            ctx.accounts.operator.key(),
            ctx.accounts.escrow.operator,
            PayxError::Unauthorized
        );
        let escrow = &mut ctx.accounts.escrow;
        require!(escrow.state == EscrowState::Deposited, PayxError::BadState);
        escrow.state = EscrowState::ReadyForFunding;

        emit!(FundingConfirmed {
            escrow_id: escrow.id,
        });

        Ok(())
    }

    pub fn release(ctx: Context<Release>) -> Result<()> {
        require_keys_eq!(
            ctx.accounts.operator.key(),
            ctx.accounts.escrow.operator,
            PayxError::Unauthorized
        );
        let escrow = &ctx.accounts.escrow;
        require!(
            escrow.state == EscrowState::ReadyForFunding,
            PayxError::BadState
        );

        let id_bytes = escrow.id.to_le_bytes();
        let bump = [escrow.bump];
        let seeds: &[&[u8]] = &[b"escrow".as_ref(), &id_bytes, &bump];

        token::transfer(
            CpiContext::new_with_signer(
                ctx.accounts.token_program.to_account_info(),
                Transfer {
                    from: ctx.accounts.vault.to_account_info(),
                    to: ctx.accounts.receiver_account.to_account_info(),
                    authority: ctx.accounts.escrow.to_account_info(),
                },
                &[seeds],
            ),
            escrow.amount,
        )?;

        let escrow = &mut ctx.accounts.escrow;
        escrow.state = EscrowState::Released;

        emit!(EscrowReleased {
            escrow_id: escrow.id,
        });

        Ok(())
    }

    pub fn refund(ctx: Context<Refund>) -> Result<()> {
        require_keys_eq!(
            ctx.accounts.operator.key(),
            ctx.accounts.escrow.operator,
            PayxError::Unauthorized
        );
        let escrow = &ctx.accounts.escrow;
        require!(
            escrow.state == EscrowState::Deposited || escrow.state == EscrowState::ReadyForFunding,
            PayxError::BadState
        );

        let id_bytes = escrow.id.to_le_bytes();
        let bump = [escrow.bump];
        let seeds: &[&[u8]] = &[b"escrow".as_ref(), &id_bytes, &bump];

        token::transfer(
            CpiContext::new_with_signer(
                ctx.accounts.token_program.to_account_info(),
                Transfer {
                    from: ctx.accounts.vault.to_account_info(),
                    to: ctx.accounts.sender_account.to_account_info(),
                    authority: ctx.accounts.escrow.to_account_info(),
                },
                &[seeds],
            ),
            escrow.amount,
        )?;

        let escrow = &mut ctx.accounts.escrow;
        escrow.state = EscrowState::Refunded;

        emit!(EscrowRefunded {
            escrow_id: escrow.id,
        });

        Ok(())
    }

    pub fn refund_timeout(ctx: Context<RefundTimeout>) -> Result<()> {
        let escrow = &ctx.accounts.escrow;
        require!(
            escrow.state == EscrowState::Deposited || escrow.state == EscrowState::ReadyForFunding,
            PayxError::BadState
        );

        let now = Clock::get()?.unix_timestamp;
        require!(
            now - escrow.deposit_timestamp >= ESCROW_TIMEOUT_SECONDS,
            PayxError::TimeoutNotReached
        );

        let id_bytes = escrow.id.to_le_bytes();
        let bump = [escrow.bump];
        let seeds: &[&[u8]] = &[b"escrow".as_ref(), &id_bytes, &bump];

        token::transfer(
            CpiContext::new_with_signer(
                ctx.accounts.token_program.to_account_info(),
                Transfer {
                    from: ctx.accounts.vault.to_account_info(),
                    to: ctx.accounts.sender_account.to_account_info(),
                    authority: ctx.accounts.escrow.to_account_info(),
                },
                &[seeds],
            ),
            escrow.amount,
        )?;

        let escrow = &mut ctx.accounts.escrow;
        escrow.state = EscrowState::Refunded;

        emit!(EscrowRefunded {
            escrow_id: escrow.id,
        });

        Ok(())
    }

    pub fn cancel(ctx: Context<Cancel>) -> Result<()> {
        require!(
            ctx.accounts.operator.key() == ctx.accounts.escrow.operator
                || ctx.accounts.operator.key() == ctx.accounts.escrow.sender,
            PayxError::Unauthorized
        );
        let escrow = &ctx.accounts.escrow;
        require!(
            escrow.state == EscrowState::Initialized,
            PayxError::BadState
        );

        let id_bytes = escrow.id.to_le_bytes();
        let bump = [escrow.bump];
        let seeds: &[&[u8]] = &[b"escrow".as_ref(), &id_bytes, &bump];

        token::close_account(CpiContext::new_with_signer(
            ctx.accounts.token_program.to_account_info(),
            token::CloseAccount {
                account: ctx.accounts.vault.to_account_info(),
                destination: ctx.accounts.operator.to_account_info(),
                authority: ctx.accounts.escrow.to_account_info(),
            },
            &[seeds],
        ))?;

        emit!(EscrowCancelled {
            escrow_id: escrow.id,
        });

        Ok(())
    }
}

#[account]
pub struct Escrow {
    pub id: u64,
    pub sender: Pubkey,
    pub receiver: Pubkey,
    pub mint: Pubkey,
    pub amount: u64,
    pub operator: Pubkey,
    pub state: EscrowState,
    pub deposit_timestamp: i64,
    pub bump: u8,
}

impl Escrow {
    pub const LEN: usize = 8  // discriminator
        + 8                   // id: u64
        + 32                  // sender: Pubkey
        + 32                  // receiver: Pubkey
        + 32                  // mint: Pubkey
        + 8                   // amount: u64
        + 32                  // operator: Pubkey
        + 1                   // state: EscrowState
        + 8                   // deposit_timestamp: i64
        + 1; // bump: u8
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone, Copy, PartialEq, Eq, Debug)]
pub enum EscrowState {
    Initialized,
    Deposited,
    ReadyForFunding,
    Released,
    Refunded,
    Cancelled,
}

#[derive(Accounts)]
#[instruction(escrow_id: u64)]
pub struct InitializeEscrow<'info> {
    #[account(
        init,
        payer = operator,
        space = Escrow::LEN,
        seeds = [b"escrow".as_ref(), &escrow_id.to_le_bytes()],
        bump
    )]
    pub escrow: Account<'info, Escrow>,
    /// CHECK: sender identity recorded on the escrow; no data read.
    pub sender: AccountInfo<'info>,
    pub mint: Account<'info, Mint>,
    #[account(
        init,
        payer = operator,
        token::mint = mint,
        token::authority = escrow,
        seeds = [b"vault".as_ref(), escrow.key().as_ref()],
        bump
    )]
    pub vault: Account<'info, TokenAccount>,
    #[account(mut)]
    pub operator: Signer<'info>,
    pub system_program: Program<'info, System>,
    pub token_program: Program<'info, Token>,
    pub rent: Sysvar<'info, Rent>,
}

#[derive(Accounts)]
pub struct Deposit<'info> {
    #[account(
        mut,
        seeds = [b"escrow".as_ref(), &escrow.id.to_le_bytes()],
        bump = escrow.bump
    )]
    pub escrow: Account<'info, Escrow>,
    #[account(
        mut,
        constraint = from.mint == escrow.mint @ PayxError::BadMint
    )]
    pub from: Account<'info, TokenAccount>,
    #[account(
        mut,
        seeds = [b"vault".as_ref(), escrow.key().as_ref()],
        bump
    )]
    pub vault: Account<'info, TokenAccount>,
    pub authority: Signer<'info>,
    pub token_program: Program<'info, Token>,
}

#[derive(Accounts)]
pub struct Transition<'info> {
    #[account(
        mut,
        seeds = [b"escrow".as_ref(), &escrow.id.to_le_bytes()],
        bump = escrow.bump
    )]
    pub escrow: Account<'info, Escrow>,
    pub operator: Signer<'info>,
}

#[derive(Accounts)]
pub struct Release<'info> {
    #[account(
        mut,
        seeds = [b"escrow".as_ref(), &escrow.id.to_le_bytes()],
        bump = escrow.bump
    )]
    pub escrow: Account<'info, Escrow>,
    #[account(
        mut,
        seeds = [b"vault".as_ref(), escrow.key().as_ref()],
        bump
    )]
    pub vault: Account<'info, TokenAccount>,
    #[account(
        mut,
        constraint = receiver_account.mint == escrow.mint @ PayxError::BadMint,
        constraint = receiver_account.owner == escrow.receiver @ PayxError::Unauthorized,
    )]
    pub receiver_account: Account<'info, TokenAccount>,
    pub operator: Signer<'info>,
    pub token_program: Program<'info, Token>,
}

#[derive(Accounts)]
pub struct Refund<'info> {
    #[account(
        mut,
        seeds = [b"escrow".as_ref(), &escrow.id.to_le_bytes()],
        bump = escrow.bump
    )]
    pub escrow: Account<'info, Escrow>,
    #[account(
        mut,
        seeds = [b"vault".as_ref(), escrow.key().as_ref()],
        bump
    )]
    pub vault: Account<'info, TokenAccount>,
    #[account(
        mut,
        constraint = sender_account.mint == escrow.mint @ PayxError::BadMint,
        constraint = sender_account.owner == escrow.sender @ PayxError::Unauthorized,
    )]
    pub sender_account: Account<'info, TokenAccount>,
    pub operator: Signer<'info>,
    pub token_program: Program<'info, Token>,
}

#[derive(Accounts)]
pub struct RefundTimeout<'info> {
    #[account(
        mut,
        seeds = [b"escrow".as_ref(), &escrow.id.to_le_bytes()],
        bump = escrow.bump
    )]
    pub escrow: Account<'info, Escrow>,
    #[account(
        mut,
        seeds = [b"vault".as_ref(), escrow.key().as_ref()],
        bump
    )]
    pub vault: Account<'info, TokenAccount>,
    #[account(
        mut,
        constraint = sender_account.mint == escrow.mint @ PayxError::BadMint,
        constraint = sender_account.owner == escrow.sender @ PayxError::Unauthorized,
    )]
    pub sender_account: Account<'info, TokenAccount>,
    pub token_program: Program<'info, Token>,
}

#[derive(Accounts)]
pub struct Cancel<'info> {
    #[account(
        mut,
        seeds = [b"escrow".as_ref(), &escrow.id.to_le_bytes()],
        bump = escrow.bump,
        close = operator
    )]
    pub escrow: Account<'info, Escrow>,
    #[account(
        mut,
        seeds = [b"vault".as_ref(), escrow.key().as_ref()],
        bump
    )]
    pub vault: Account<'info, TokenAccount>,
    #[account(mut)]
    pub operator: Signer<'info>,
    pub token_program: Program<'info, Token>,
}

#[event]
pub struct EscrowInitialized {
    pub escrow_id: u64,
    pub sender: Pubkey,
    pub receiver: Pubkey,
    pub mint: Pubkey,
    pub amount: u64,
    pub operator: Pubkey,
}

#[event]
pub struct EscrowDeposited {
    pub escrow_id: u64,
    pub sender: Pubkey,
    pub receiver: Pubkey,
    pub amount: u64,
}

#[event]
pub struct FundingConfirmed {
    pub escrow_id: u64,
}

#[event]
pub struct EscrowReleased {
    pub escrow_id: u64,
}

#[event]
pub struct EscrowRefunded {
    pub escrow_id: u64,
}

#[event]
pub struct EscrowCancelled {
    pub escrow_id: u64,
}

#[error_code]
pub enum PayxError {
    #[msg("Amount must be greater than zero.")]
    ZeroAmount,
    #[msg("Vault mint does not match the escrow mint.")]
    BadMint,
    #[msg("Escrow is not in a state that allows this transition.")]
    BadState,
    #[msg("Caller is not authorized for this escrow.")]
    Unauthorized,
    #[msg("Escrow timeout has not been reached yet.")]
    TimeoutNotReached,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_escrow_space() {
        assert_eq!(Escrow::LEN, 8 + 8 + 32 + 32 + 32 + 8 + 32 + 1 + 8 + 1);
        assert_eq!(Escrow::LEN, 162);
    }

    #[test]
    fn test_escrow_state_serialization() {
        let states = vec![
            EscrowState::Initialized,
            EscrowState::Deposited,
            EscrowState::ReadyForFunding,
            EscrowState::Released,
            EscrowState::Refunded,
            EscrowState::Cancelled,
        ];
        for state in states {
            let serialized = state.try_to_vec().unwrap();
            let deserialized: EscrowState = EscrowState::try_from_slice(&serialized).unwrap();
            assert_eq!(state, deserialized);
        }
    }

    #[test]
    fn test_pda_seeds_derivation() {
        let escrow_id: u64 = 42;
        let id_bytes = escrow_id.to_le_bytes();
        let program_id = crate::ID;
        let (pda, _bump) = Pubkey::find_program_address(&[b"escrow", &id_bytes], &program_id);
        assert_ne!(pda, Pubkey::default());
        let (vault_pda, _vault_bump) =
            Pubkey::find_program_address(&[b"vault", pda.as_ref()], &program_id);
        assert_ne!(vault_pda, Pubkey::default());
        assert_ne!(pda, vault_pda);
    }

    #[test]
    fn test_timeout_constant() {
        assert_eq!(ESCROW_TIMEOUT_SECONDS, 86400);
    }
}
