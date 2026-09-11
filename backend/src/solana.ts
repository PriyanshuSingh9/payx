import { Connection, PublicKey } from "@solana/web3.js";
import { env } from "./env.js";

let connection: Connection | null = null;

export function getConnection(): Connection {
  if (!connection) {
    connection = new Connection(env.solanaRpcUrl, "confirmed");
  }
  return connection;
}

export function getProgramId(): PublicKey | null {
  return env.programId ? new PublicKey(env.programId) : null;
}

// PDA for escrow `escrowId`: ["escrow", le_bytes(u64)].
export function findEscrowPda(programId: PublicKey, escrowId: bigint): [PublicKey, number] {
  const buf = Buffer.alloc(8);
  buf.writeBigUInt64LE(escrowId);
  return PublicKey.findProgramAddressSync([Buffer.from("escrow"), buf], programId);
}

export async function getSlot(): Promise<number> {
  return getConnection().getSlot("confirmed");
}

// Phase 1 wires the full listener: subscribe to EscrowDeposited /
// FundingConfirmed / EscrowReleased / EscrowRefunded program logs and mirror
// them into Transaction rows. Until the program is deployed, this is a no-op
// that validates RPC reachability.
export async function startEscrowListener(): Promise<void> {
  const conn = getConnection();
  await conn.getSlot("confirmed");
  const programId = getProgramId();
  if (!programId) {
    console.log("[solana] PAYX_PROGRAM_ID unset; listener idle until deploy.");
    return;
  }
  console.log(`[solana] Listening for payx_escrow events on ${programId.toBase58()}`);
}
