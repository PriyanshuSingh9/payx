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
        escrow.state = EscrowState::Deposited;
        escrow.deposit_timestamp = Clock::get()?.unix_timestamp;
        escrow.bump = ctx.bumps.escrow;
        emit!(EscrowDeposited {
            escrow_id,
            sender: escrow.sender,
            receiver,
            amount
        });
        Ok(())
    }

    pub fn deposit(ctx: Context<Deposit>) -> Result<()> {
        let escrow = &ctx.accounts.escrow;
        require!(escrow.state == EscrowState::Deposited, PayxError::BadState);
        let id_bytes = escrow.id.to_le_bytes();
        let bump = [escrow.bump];
        let seeds: &[&[u8]] = &[b"escrow".as_ref(), &id_bytes, &bump];
        token::transfer(
            CpiContext::new_with_signer(
                ctx.accounts.token_program.to_account_info(),
                Transfer {
                    from: ctx.accounts.from.to_account_info(),
                    to: ctx.accounts.vault.to_account_info(),
                    authority: ctx.accounts.authority.to_account_info(),
                },
                &[seeds],
            ),
            escrow.amount,
        )?;
        Ok(())
    }

    pub fn confirm_funding(ctx: Context<Transition>) -> Result<()> {
        require_keys_eq!(
            ctx.accounts.sender.key(),
            ctx.accounts.escrow.sender,
            PayxError::Unauthorized
        );
        let escrow = &mut ctx.accounts.escrow;
        require!(escrow.state == EscrowState::Deposited, PayxError::BadState);
        escrow.state = EscrowState::ReadyForFunding;
        emit!(FundingConfirmed {
            escrow_id: escrow.id
        });
        Ok(())
    }

    pub fn release(ctx: Context<Release>) -> Result<()> {
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
            escrow_id: escrow.id
        });
        Ok(())
    }

    pub fn refund(ctx: Context<ReleaseToSender>) -> Result<()> {
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
            escrow_id: escrow.id
        });
        Ok(())
    }

    pub fn refund_timeout(ctx: Context<ReleaseToSender>) -> Result<()> {
        let escrow = &ctx.accounts.escrow;
        require!(escrow.state == EscrowState::Deposited, PayxError::BadState);
        let now = Clock::get()?.unix_timestamp;
        require!(
            now - escrow.deposit_timestamp >= ESCROW_TIMEOUT_SECONDS,
            PayxError::TimeoutNotReached
        );
        // Permissionless: any caller may trigger a timed-out refund.
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
            escrow_id: escrow.id
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
    pub state: EscrowState,
    pub deposit_timestamp: i64,
    pub bump: u8,
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone, Copy, PartialEq, Eq)]
pub enum EscrowState {
    Deposited,
    ReadyForFunding,
    Released,
    Refunded,
}

#[derive(Accounts)]
#[instruction(escrow_id: u64)]
pub struct InitializeEscrow<'info> {
    #[account(
        init,
        payer = operator,
        space = 8 + 8 + 32 + 32 + 32 + 8 + 1 + 8 + 1,
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
    #[account(mut)]
    pub escrow: Account<'info, Escrow>,
    #[account(mut)]
    pub from: Account<'info, TokenAccount>,
    #[account(mut)]
    pub vault: Account<'info, TokenAccount>,
    pub authority: Signer<'info>,
    pub token_program: Program<'info, Token>,
}

#[derive(Accounts)]
pub struct Transition<'info> {
    #[account(mut)]
    pub escrow: Account<'info, Escrow>,
    /// CHECK: constrained against escrow.sender by require_keys_eq!.
    pub sender: AccountInfo<'info>,
    pub operator: Signer<'info>,
}

#[derive(Accounts)]
pub struct Release<'info> {
    #[account(mut)]
    pub escrow: Account<'info, Escrow>,
    #[account(mut)]
    pub vault: Account<'info, TokenAccount>,
    #[account(mut)]
    pub receiver_account: Account<'info, TokenAccount>,
    pub operator: Signer<'info>,
    pub token_program: Program<'info, Token>,
}

#[derive(Accounts)]
pub struct ReleaseToSender<'info> {
    #[account(mut)]
    pub escrow: Account<'info, Escrow>,
    #[account(mut)]
    pub vault: Account<'info, TokenAccount>,
    #[account(mut)]
    pub sender_account: Account<'info, TokenAccount>,
    pub token_program: Program<'info, Token>,
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
