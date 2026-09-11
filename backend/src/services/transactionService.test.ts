import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { computeQuote, findCorridor, CORRIDORS, type Corridor } from "../lib/index.js";
import { canTransition, type EscrowState, type TransactionStatus } from "../lib/status.js";

describe("Transaction DB Logic & Pure Money Math Domain", () => {
  it("calculates exact transfer amounts for USD-INR corridor at 83.42 rate", () => {
    const corridor = findCorridor("USD-INR-UPI");
    assert.ok(corridor, "Corridor USD-INR-UPI must exist");

    const amountSource = 100;
    const fxRate = 83.42;

    const quote = computeQuote(corridor, amountSource, fxRate);

    // feeBps = 95 (0.95%) -> fee = 0.95
    assert.equal(quote.amountSource, 100);
    assert.equal(quote.feeSource, 0.95);
    assert.equal(quote.amountUsdc, 99.05);
    // netSource * fxRate = 99.05 * 83.42 = 8262.75
    assert.equal(quote.amountDest, 8262.75);
    assert.equal(quote.destCurrency, "INR");
    assert.equal(quote.corridorId, "USD-INR-UPI");
  });

  it("calculates fee and net destination amount accurately for GBP-INR corridor", () => {
    const corridor = findCorridor("GBP-INR-UPI");
    assert.ok(corridor, "Corridor GBP-INR-UPI must exist");

    const amountSource = 250;
    const fxRate = 105.50;

    const quote = computeQuote(corridor, amountSource, fxRate);

    // feeBps = 100 (1.00%) -> fee = 2.50
    assert.equal(quote.amountSource, 250);
    assert.equal(quote.feeSource, 2.50);
    assert.equal(quote.amountUsdc, 247.50);
    assert.equal(quote.amountDest, 26111.25);
    assert.equal(quote.destCurrency, "INR");
  });

  it("rejects non-positive amountSource", () => {
    const corridor = CORRIDORS[0] as Corridor;
    assert.throws(() => computeQuote(corridor, 0, 83.42), /positive number/);
    assert.throws(() => computeQuote(corridor, -50, 83.42), /positive number/);
  });

  it("rejects non-positive fxRate", () => {
    const corridor = CORRIDORS[0] as Corridor;
    assert.throws(() => computeQuote(corridor, 100, 0), /positive number/);
    assert.throws(() => computeQuote(corridor, 100, -1), /positive number/);
  });

  it("validates legal on-chain escrow state transitions", () => {
    assert.equal(canTransition("Deposited", "ReadyForFunding"), true);
    assert.equal(canTransition("Deposited", "Refunded"), true);
    assert.equal(canTransition("Deposited", "Released"), false);

    assert.equal(canTransition("ReadyForFunding", "Released"), true);
    assert.equal(canTransition("ReadyForFunding", "Refunded"), true);
    assert.equal(canTransition("ReadyForFunding", "Deposited"), false);

    assert.equal(canTransition("Released", "Refunded"), false);
    assert.equal(canTransition("Refunded", "Released"), false);
  });

  it("defines standard transaction lifecycle statuses", () => {
    const statuses: TransactionStatus[] = [
      "pending",
      "escrow_locked",
      "offramp_pending",
      "offramp_ready",
      "escrow_released",
      "completed",
      "failed",
      "refunded"
    ];
    assert.equal(statuses.length, 8);
  });
});
