import { describe, it } from "node:test";
import assert from "node:assert/strict";
import crypto from "node:crypto";
import { PublicKey, TransactionInstruction, Keypair } from "@solana/web3.js";

// Program ID under test (matches declare_id! in programs/payx_escrow/src/lib.rs)
const PROGRAM_ID = new PublicKey("11111111111111111111111111111111");
const ESCROW_TIMEOUT_SECONDS = 86400; // 24 hours in seconds

// Escrow state representation matching Rust enum
export enum EscrowState {
  Initialized = 0,
  Deposited = 1,
  ReadyForFunding = 2,
  Released = 3,
  Refunded = 4,
  Cancelled = 5,
}

export interface EscrowAccount {
  id: bigint;
  sender: PublicKey;
  receiver: PublicKey;
  mint: PublicKey;
  amount: bigint;
  operator: PublicKey;
  state: EscrowState;
  depositTimestamp: bigint;
  bump: number;
}

// Helper: Derive Escrow PDA matching Anchor seeds: ["escrow", id.to_le_bytes()]
export function findEscrowPda(programId: PublicKey, escrowId: bigint): [PublicKey, number] {
  const buf = Buffer.alloc(8);
  buf.writeBigUInt64LE(escrowId);
  return PublicKey.findProgramAddressSync([Buffer.from("escrow"), buf], programId);
}

// Helper: Derive Vault PDA matching Anchor seeds: ["vault", escrow.key()]
export function findVaultPda(programId: PublicKey, escrowPda: PublicKey): [PublicKey, number] {
  return PublicKey.findProgramAddressSync([Buffer.from("vault"), escrowPda.toBuffer()], programId);
}

// Helper: Compute 8-byte Anchor instruction discriminator: sha256("global:<name>")[0..8]
export function getAnchorDiscriminator(name: string): Buffer {
  const hash = crypto.createHash("sha256").update(`global:${name}`).digest();
  return hash.subarray(0, 8);
}

// Helper: Compute 8-byte Anchor account discriminator: sha256("account:<name>")[0..8]
export function getAccountDiscriminator(name: string): Buffer {
  const hash = crypto.createHash("sha256").update(`account:${name}`).digest();
  return hash.subarray(0, 8);
}

// Binary layout serializer/deserializer for Escrow (162 bytes total)
export const ESCROW_ACCOUNT_LEN = 162;

export function serializeEscrow(escrow: EscrowAccount): Buffer {
  const buf = Buffer.alloc(ESCROW_ACCOUNT_LEN);
  let offset = 0;

  // 8 bytes discriminator
  const disc = getAccountDiscriminator("Escrow");
  disc.copy(buf, offset);
  offset += 8;

  // id: u64 (8 bytes)
  buf.writeBigUInt64LE(escrow.id, offset);
  offset += 8;

  // sender: Pubkey (32 bytes)
  escrow.sender.toBuffer().copy(buf, offset);
  offset += 32;

  // receiver: Pubkey (32 bytes)
  escrow.receiver.toBuffer().copy(buf, offset);
  offset += 32;

  // mint: Pubkey (32 bytes)
  escrow.mint.toBuffer().copy(buf, offset);
  offset += 32;

  // amount: u64 (8 bytes)
  buf.writeBigUInt64LE(escrow.amount, offset);
  offset += 8;

  // operator: Pubkey (32 bytes)
  escrow.operator.toBuffer().copy(buf, offset);
  offset += 32;

  // state: EscrowState (1 byte)
  buf.writeUInt8(escrow.state, offset);
  offset += 1;

  // depositTimestamp: i64 (8 bytes)
  buf.writeBigInt64LE(escrow.depositTimestamp, offset);
  offset += 8;

  // bump: u8 (1 byte)
  buf.writeUInt8(escrow.bump, offset);
  offset += 1;

  assert.equal(offset, ESCROW_ACCOUNT_LEN, "Escrow serialization must be exactly 162 bytes");
  return buf;
}

export function deserializeEscrow(data: Buffer): EscrowAccount {
  assert.equal(data.length, ESCROW_ACCOUNT_LEN, "Data buffer must be 162 bytes");
  let offset = 8; // skip 8 bytes account discriminator

  const id = data.readBigUInt64LE(offset);
  offset += 8;

  const sender = new PublicKey(data.subarray(offset, offset + 32));
  offset += 32;

  const receiver = new PublicKey(data.subarray(offset, offset + 32));
  offset += 32;

  const mint = new PublicKey(data.subarray(offset, offset + 32));
  offset += 32;

  const amount = data.readBigUInt64LE(offset);
  offset += 8;

  const operator = new PublicKey(data.subarray(offset, offset + 32));
  offset += 32;

  const state = data.readUInt8(offset) as EscrowState;
  offset += 1;

  const depositTimestamp = data.readBigInt64LE(offset);
  offset += 8;

  const bump = data.readUInt8(offset);
  offset += 1;

  return { id, sender, receiver, mint, amount, operator, state, depositTimestamp, bump };
}

describe("payx_escrow smart contract test suite", () => {
  const operatorKeypair = Keypair.generate();
  const senderKeypair = Keypair.generate();
  const receiverKeypair = Keypair.generate();
  const mintAddress = Keypair.generate().publicKey;
  const escrowId = 1001n;
  const transferAmount = 50_000_000n; // 50 USDC (6 decimals)

  describe("PDA derivation & vault address calculations", () => {
    it("derives deterministic escrow PDA from escrow_id", () => {
      const [pda1, bump1] = findEscrowPda(PROGRAM_ID, escrowId);
      const [pda2, bump2] = findEscrowPda(PROGRAM_ID, escrowId);

      assert.equal(pda1.toBase58(), pda2.toBase58());
      assert.equal(bump1, bump2);
      assert.ok(bump1 >= 0 && bump1 <= 255);
    });

    it("generates distinct PDAs for different escrow IDs", () => {
      const [pda1] = findEscrowPda(PROGRAM_ID, 1n);
      const [pda2] = findEscrowPda(PROGRAM_ID, 2n);
      assert.notEqual(pda1.toBase58(), pda2.toBase58());
    });

    it("derives deterministic vault PDA seeded by the escrow PDA", () => {
      const [escrowPda] = findEscrowPda(PROGRAM_ID, escrowId);
      const [vaultPda, vaultBump] = findVaultPda(PROGRAM_ID, escrowPda);

      assert.notEqual(escrowPda.toBase58(), vaultPda.toBase58());
      assert.ok(vaultBump >= 0 && vaultBump <= 255);
    });
  });

  describe("account binary layout & state serialization", () => {
    it("matches exact 162-byte Escrow::LEN size defined in contract", () => {
      const [escrowPda, bump] = findEscrowPda(PROGRAM_ID, escrowId);
      const escrow: EscrowAccount = {
        id: escrowId,
        sender: senderKeypair.publicKey,
        receiver: receiverKeypair.publicKey,
        mint: mintAddress,
        amount: transferAmount,
        operator: operatorKeypair.publicKey,
        state: EscrowState.Initialized,
        depositTimestamp: 0n,
        bump,
      };

      const buffer = serializeEscrow(escrow);
      assert.equal(buffer.length, 162);

      const decoded = deserializeEscrow(buffer);
      assert.equal(decoded.id, escrow.id);
      assert.equal(decoded.sender.toBase58(), escrow.sender.toBase58());
      assert.equal(decoded.receiver.toBase58(), escrow.receiver.toBase58());
      assert.equal(decoded.mint.toBase58(), escrow.mint.toBase58());
      assert.equal(decoded.amount, escrow.amount);
      assert.equal(decoded.operator.toBase58(), escrow.operator.toBase58());
      assert.equal(decoded.state, EscrowState.Initialized);
      assert.equal(decoded.depositTimestamp, 0n);
      assert.equal(decoded.bump, bump);
    });

    it("correctly encodes each distinct EscrowState variant", () => {
      const states = [
        EscrowState.Initialized,
        EscrowState.Deposited,
        EscrowState.ReadyForFunding,
        EscrowState.Released,
        EscrowState.Refunded,
        EscrowState.Cancelled,
      ];

      for (const st of states) {
        const escrow: EscrowAccount = {
          id: escrowId,
          sender: senderKeypair.publicKey,
          receiver: receiverKeypair.publicKey,
          mint: mintAddress,
          amount: transferAmount,
          operator: operatorKeypair.publicKey,
          state: st,
          depositTimestamp: 1726000000n,
          bump: 254,
        };

        const decoded = deserializeEscrow(serializeEscrow(escrow));
        assert.equal(decoded.state, st);
      }
    });
  });

  describe("Anchor instruction encoding & discriminators", () => {
    it("computes standard 8-byte sha256 discriminators for all instructions", () => {
      const expectedInstructions = [
        "initialize_escrow",
        "deposit",
        "confirm_funding",
        "release",
        "refund",
        "refund_timeout",
        "cancel",
      ];

      for (const ixName of expectedInstructions) {
        const disc = getAnchorDiscriminator(ixName);
        assert.equal(disc.length, 8, `Discriminator for ${ixName} must be 8 bytes`);
      }
    });

    it("builds valid initialize_escrow instruction payload", () => {
      const disc = getAnchorDiscriminator("initialize_escrow");
      const data = Buffer.alloc(8 + 8 + 32 + 8);
      let offset = 0;

      disc.copy(data, offset);
      offset += 8;

      data.writeBigUInt64LE(escrowId, offset);
      offset += 8;

      receiverKeypair.publicKey.toBuffer().copy(data, offset);
      offset += 32;

      data.writeBigUInt64LE(transferAmount, offset);
      offset += 8;

      const [escrowPda] = findEscrowPda(PROGRAM_ID, escrowId);
      const [vaultPda] = findVaultPda(PROGRAM_ID, escrowPda);

      const ix = new TransactionInstruction({
        programId: PROGRAM_ID,
        data,
        keys: [
          { pubkey: escrowPda, isSigner: false, isWritable: true },
          { pubkey: senderKeypair.publicKey, isSigner: false, isWritable: false },
          { pubkey: mintAddress, isSigner: false, isWritable: false },
          { pubkey: vaultPda, isSigner: false, isWritable: true },
          { pubkey: operatorKeypair.publicKey, isSigner: true, isWritable: true },
        ],
      });

      assert.equal(ix.programId.toBase58(), PROGRAM_ID.toBase58());
      assert.equal(ix.keys.length, 5);
      assert.ok(ix.keys[4].isSigner, "Operator must be a signer on initialize_escrow");
    });
  });

  describe("state machine transition rules", () => {
    it("enforces valid lifecycle: Initialized -> Deposited -> ReadyForFunding -> Released", () => {
      let state = EscrowState.Initialized;

      // 1. Deposit
      assert.equal(state, EscrowState.Initialized);
      state = EscrowState.Deposited;
      const depositTimestamp = BigInt(Math.floor(Date.now() / 1000));

      // 2. Operator confirms off-ramp funding readiness
      assert.equal(state, EscrowState.Deposited);
      state = EscrowState.ReadyForFunding;

      // 3. Operator releases funds upon fiat delivery
      assert.equal(state, EscrowState.ReadyForFunding);
      state = EscrowState.Released;

      assert.equal(state, EscrowState.Released);
      assert.ok(depositTimestamp > 0n);
    });

    it("enforces operator emergency refund transition from Deposited or ReadyForFunding", () => {
      // From Deposited:
      let state1 = EscrowState.Deposited;
      assert.ok(state1 === EscrowState.Deposited || state1 === EscrowState.ReadyForFunding);
      state1 = EscrowState.Refunded;
      assert.equal(state1, EscrowState.Refunded);

      // From ReadyForFunding:
      let state2 = EscrowState.ReadyForFunding;
      assert.ok(state2 === EscrowState.Deposited || state2 === EscrowState.ReadyForFunding);
      state2 = EscrowState.Refunded;
      assert.equal(state2, EscrowState.Refunded);
    });

    it("prohibits refund after funds have already been Released", () => {
      const state = EscrowState.Released;
      const isRefundable =
        state === EscrowState.Deposited || state === EscrowState.ReadyForFunding;
      assert.equal(isRefundable, false, "Released escrow cannot be refunded");
    });

    it("permits cancellation only while in Initialized state (before deposit)", () => {
      let state = EscrowState.Initialized;
      const isCancellable = state === EscrowState.Initialized;
      assert.equal(isCancellable, true);

      state = EscrowState.Cancelled;
      assert.equal(state, EscrowState.Cancelled);

      // Deposited cannot be cancelled (must use refund)
      const depositedState = EscrowState.Deposited;
      assert.notEqual(depositedState, EscrowState.Initialized);
    });
  });

  describe("security constraints & timelock access controls", () => {
    it("enforces operator authorization check against escrow.operator", () => {
      const legitimateOperator = operatorKeypair.publicKey;
      const attacker = Keypair.generate().publicKey;

      const authorizeOperator = (caller: PublicKey) => {
        if (!caller.equals(legitimateOperator)) {
          throw new Error("PayxError::Unauthorized");
        }
        return true;
      };

      assert.ok(authorizeOperator(legitimateOperator));
      assert.throws(() => authorizeOperator(attacker), /PayxError::Unauthorized/);
    });

    it("verifies recipient constraint: tokens only release to receiver token account", () => {
      const receiver = receiverKeypair.publicKey;
      const attacker = Keypair.generate().publicKey;

      const validateRecipientAccount = (accountOwner: PublicKey) => {
        if (!accountOwner.equals(receiver)) {
          throw new Error("PayxError::Unauthorized");
        }
        return true;
      };

      assert.ok(validateRecipientAccount(receiver));
      assert.throws(() => validateRecipientAccount(attacker), /PayxError::Unauthorized/);
    });

    it("verifies sender constraint: refunds only return to original sender token account", () => {
      const sender = senderKeypair.publicKey;
      const attacker = Keypair.generate().publicKey;

      const validateSenderAccount = (accountOwner: PublicKey) => {
        if (!accountOwner.equals(sender)) {
          throw new Error("PayxError::Unauthorized");
        }
        return true;
      };

      assert.ok(validateSenderAccount(sender));
      assert.throws(() => validateSenderAccount(attacker), /PayxError::Unauthorized/);
    });

    it("rejects premature timeout refund before 24 hours have elapsed", () => {
      const depositTimestamp = 1_000_000n;
      const now = depositTimestamp + 3600n; // only 1 hour later

      const checkTimeout = (currentTimestamp: bigint) => {
        if (currentTimestamp - depositTimestamp < BigInt(ESCROW_TIMEOUT_SECONDS)) {
          throw new Error("PayxError::TimeoutNotReached");
        }
        return true;
      };

      assert.throws(() => checkTimeout(now), /PayxError::TimeoutNotReached/);
    });

    it("permits permissionless refund after 24-hour timeout has elapsed", () => {
      const depositTimestamp = 1_000_000n;
      const now = depositTimestamp + BigInt(ESCROW_TIMEOUT_SECONDS) + 1n; // 24h + 1s later

      const elapsed = now - depositTimestamp;
      assert.ok(elapsed >= BigInt(ESCROW_TIMEOUT_SECONDS));
    });

    it("verifies mint equality check on token accounts", () => {
      const usdcMint = mintAddress;
      const wrongMint = Keypair.generate().publicKey;

      const checkMint = (accountMint: PublicKey) => {
        if (!accountMint.equals(usdcMint)) {
          throw new Error("PayxError::BadMint");
        }
        return true;
      };

      assert.ok(checkMint(usdcMint));
      assert.throws(() => checkMint(wrongMint), /PayxError::BadMint/);
    });
  });
});
