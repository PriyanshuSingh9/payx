import type { Payment } from "../lib/index.js";
import { prisma } from "../prisma.js";

export interface IPaymentStore {
  savePayment(payment: Payment): Promise<Payment>;
  getPayment(id: string): Promise<Payment | null>;
  listPayments(limit?: number): Promise<Payment[]>;
  recordIdempotency(key: string, paymentId: string): Promise<void>;
  getPaymentByIdempotencyKey(key: string): Promise<Payment | null>;
  reserveIdempotencyKey(key: string, executor: () => Promise<Payment>): Promise<Payment>;
}

export class InMemoryPaymentStore implements IPaymentStore {
  private payments = new Map<string, Payment>();
  private idempotencyKeys = new Map<string, string>(); // idempotencyKey -> paymentId
  private inFlightIdempotency = new Map<string, Promise<Payment>>();

  async savePayment(payment: Payment): Promise<Payment> {
    const clone = JSON.parse(JSON.stringify(payment)) as Payment;
    this.payments.set(payment.id, clone);
    if (payment.idempotencyKey) {
      this.idempotencyKeys.set(payment.idempotencyKey, payment.id);
    }
    return clone;
  }

  async getPayment(id: string): Promise<Payment | null> {
    let payment = this.payments.get(id);
    if (!payment) {
      if (id === "tx_priya_500") payment = this.payments.get("PX-PRIYA500");
      else if (id === "tx_rahul_250") payment = this.payments.get("PX-RAHUL250");
    }
    return payment ? (JSON.parse(JSON.stringify(payment)) as Payment) : null;
  }

  async listPayments(limit = 50): Promise<Payment[]> {
    const list = Array.from(this.payments.values())
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, limit);
    return JSON.parse(JSON.stringify(list)) as Payment[];
  }

  async recordIdempotency(key: string, paymentId: string): Promise<void> {
    this.idempotencyKeys.set(key, paymentId);
  }

  async getPaymentByIdempotencyKey(key: string): Promise<Payment | null> {
    const paymentId = this.idempotencyKeys.get(key);
    if (!paymentId) return null;
    return this.getPayment(paymentId);
  }

  // Atomically reserves an idempotency key to prevent race conditions during concurrent requests.
  async reserveIdempotencyKey(key: string, executor: () => Promise<Payment>): Promise<Payment> {
    // If already completed in store
    const existing = await this.getPaymentByIdempotencyKey(key);
    if (existing) return existing;

    // If an execution is already in flight for this exact key, join it
    const inFlight = this.inFlightIdempotency.get(key);
    if (inFlight) return inFlight;

    // Otherwise launch and track the promise
    const promise = (async () => {
      try {
        const payment = await executor();
        this.idempotencyKeys.set(key, payment.id);
        return payment;
      } finally {
        this.inFlightIdempotency.delete(key);
      }
    })();

    this.inFlightIdempotency.set(key, promise);
    return promise;
  }

  constructor() {
    this.bootstrapDemoData();
  }

  bootstrapDemoData(): void {
    const demoPayments: Payment[] = [
      {
        id: "PX-PRIYA500",
        status: "COMPLETED",
        mode: "full_simulation",
        senderWallet: "7xK999999999999999999999999999999999999992PD",
        recipient: {
          id: "rec_priya",
          name: "Priya Sharma",
          phone: "+919876543210",
          upiId: "priya.sharma@oksbi",
          country: "IN",
          currency: "INR"
        },
        sourceAsset: "USDC",
        sourceAmount: 500,
        destinationCurrency: "INR",
        destinationAmount: 41950.0,
        exchangeRate: 92.9,
        fees: {
          offRampFee: 15.0,
          networkFee: 0.01,
          totalFee: 15.01
        },
        blockchainTransaction: {
          id: "btx_priya_1",
          paymentId: "PX-PRIYA500",
          chain: "solana",
          network: "simulator",
          token: "USDC",
          amount: 500,
          sender: "7xK999999999999999999999999999999999999992PD",
          recipient: "4vvzXwGLvriT9WuDJmTBcwVxiebLE5z9YUVQ6SZwiSDc",
          transactionSignature: "5Kq...PriyaSolanaTx",
          confirmationStatus: "finalized",
          explorerUrl: "https://solscan.io",
          createdAt: new Date(Date.now() - 86400000).toISOString()
        },
        timeline: [
          {
            id: "evt_1",
            timestamp: new Date(Date.now() - 86400000).toISOString(),
            status: "COMPLETED",
            title: "Payment Completed",
            description: "₹41,950 disbursed to Priya Sharma via UPI."
          }
        ],
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        updatedAt: new Date(Date.now() - 86400000).toISOString(),
        completedAt: new Date(Date.now() - 86395000).toISOString()
      },
      {
        id: "PX-RAHUL250",
        status: "COMPLETED",
        mode: "full_simulation",
        senderWallet: "7xK999999999999999999999999999999999999992PD",
        recipient: {
          id: "rec_rahul",
          name: "Rahul Verma",
          phone: "+919876543211",
          upiId: "rahul.verma@oksbi",
          country: "IN",
          currency: "INR"
        },
        sourceAsset: "USDC",
        sourceAmount: 250,
        destinationCurrency: "INR",
        destinationAmount: 20975.0,
        exchangeRate: 92.9,
        fees: {
          offRampFee: 7.5,
          networkFee: 0.01,
          totalFee: 7.51
        },
        blockchainTransaction: {
          id: "btx_rahul_1",
          paymentId: "PX-RAHUL250",
          chain: "solana",
          network: "simulator",
          token: "USDC",
          amount: 250,
          sender: "7xK999999999999999999999999999999999999992PD",
          recipient: "4vvzXwGLvriT9WuDJmTBcwVxiebLE5z9YUVQ6SZwiSDc",
          transactionSignature: "3Jr...RahulSolanaTx",
          confirmationStatus: "finalized",
          explorerUrl: "https://solscan.io",
          createdAt: new Date(Date.now() - 172800000).toISOString()
        },
        timeline: [
          {
            id: "evt_2",
            timestamp: new Date(Date.now() - 172800000).toISOString(),
            status: "COMPLETED",
            title: "Payment Completed",
            description: "₹20,975 disbursed to Rahul Verma via UPI."
          }
        ],
        createdAt: new Date(Date.now() - 172800000).toISOString(),
        updatedAt: new Date(Date.now() - 172800000).toISOString(),
        completedAt: new Date(Date.now() - 172795000).toISOString()
      },
      {
        id: "PX-SARAH1700",
        status: "COMPLETED",
        mode: "full_simulation",
        senderWallet: "7xK999999999999999999999999999999999999992PD",
        recipient: {
          id: "rec_sarah",
          name: "Sarah Smith",
          phone: "+919876543212",
          upiId: "sarah.smith@oksbi",
          country: "IN",
          currency: "INR"
        },
        sourceAsset: "USDC",
        sourceAmount: 1700,
        destinationCurrency: "INR",
        destinationAmount: 142630.0,
        exchangeRate: 92.9,
        fees: {
          offRampFee: 51.0,
          networkFee: 0.01,
          totalFee: 51.01
        },
        blockchainTransaction: {
          id: "btx_sarah_1",
          paymentId: "PX-SARAH1700",
          chain: "solana",
          network: "simulator",
          token: "USDC",
          amount: 1700,
          sender: "7xK999999999999999999999999999999999999992PD",
          recipient: "4vvzXwGLvriT9WuDJmTBcwVxiebLE5z9YUVQ6SZwiSDc",
          transactionSignature: "8Lp...SarahSolanaTx",
          confirmationStatus: "finalized",
          explorerUrl: "https://solscan.io",
          createdAt: new Date(Date.now() - 259200000).toISOString()
        },
        timeline: [
          {
            id: "evt_3",
            timestamp: new Date(Date.now() - 259200000).toISOString(),
            status: "COMPLETED",
            title: "Payment Completed",
            description: "₹142,630 disbursed to Sarah Smith via UPI."
          }
        ],
        createdAt: new Date(Date.now() - 259200000).toISOString(),
        updatedAt: new Date(Date.now() - 259200000).toISOString(),
        completedAt: new Date(Date.now() - 259195000).toISOString()
      }
    ];

    for (const p of demoPayments) {
      this.payments.set(p.id, p);
    }
  }

  // Clear helper for tests
  clear(): void {
    this.payments.clear();
    this.idempotencyKeys.clear();
    this.inFlightIdempotency.clear();
  }
}

export class PostgresPaymentStore implements IPaymentStore {
  private memoryCache = new Map<string, Payment>();
  private idempotencyKeys = new Map<string, string>();
  private inFlightIdempotency = new Map<string, Promise<Payment>>();
  private listCache: { payments: Payment[]; cachedAt: number } | null = null;
  private readonly LIST_CACHE_TTL_MS = 3000;
  private isTableInitialized = false;

  constructor() {
    this.initStore().catch((err) => {
      console.warn("[PostgresPaymentStore] Init warning:", err instanceof Error ? err.message : err);
    });
  }

  private async ensureTable(): Promise<void> {
    if (this.isTableInitialized) return;
    try {
      await prisma.$executeRawUnsafe(`
        CREATE TABLE IF NOT EXISTS payments (
          id TEXT PRIMARY KEY,
          status TEXT NOT NULL,
          mode TEXT NOT NULL,
          sender_wallet TEXT NOT NULL,
          recipient_name TEXT NOT NULL,
          recipient_phone TEXT NOT NULL,
          recipient_upi_id TEXT,
          recipient_bank_account TEXT,
          recipient_ifsc TEXT,
          source_asset TEXT NOT NULL DEFAULT 'USDC',
          source_amount NUMERIC NOT NULL,
          destination_currency TEXT NOT NULL DEFAULT 'INR',
          destination_amount NUMERIC,
          exchange_rate NUMERIC,
          quote_id TEXT,
          idempotency_key TEXT UNIQUE,
          data JSONB NOT NULL,
          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
        );
        CREATE INDEX IF NOT EXISTS idx_payments_sender_wallet ON payments(sender_wallet);
        CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at DESC);
      `);
      this.isTableInitialized = true;
    } catch {
      // Gracefully handled if offline
    }
  }

  private async initStore(): Promise<void> {
    await this.ensureTable();

    try {
      const countRes = await prisma.$queryRawUnsafe<Array<{ c: number }>>(
        "SELECT count(*)::int as c FROM payments;"
      );
      const count = countRes?.[0]?.c ?? 0;

      if (count === 0) {
        const fallback = new InMemoryPaymentStore();
        const initialPayments = await fallback.listPayments(10);
        for (const p of initialPayments) {
          await this.savePayment(p);
        }
      } else {
        const rows = await prisma.$queryRawUnsafe<Array<{ data: any }>>(
          "SELECT data FROM payments ORDER BY created_at DESC LIMIT 50;"
        );
        for (const row of rows) {
          if (row.data?.id) {
            const p = row.data as Payment;
            this.memoryCache.set(p.id, p);
            if (p.idempotencyKey) {
              this.idempotencyKeys.set(p.idempotencyKey, p.id);
            }
          }
        }
        this.listCache = {
          payments: Array.from(this.memoryCache.values()).sort(
            (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
          ),
          cachedAt: Date.now()
        };
      }
    } catch {
      const fallback = new InMemoryPaymentStore();
      const initialPayments = await fallback.listPayments(10);
      for (const p of initialPayments) {
        this.memoryCache.set(p.id, p);
      }
    }
  }

  async savePayment(payment: Payment): Promise<Payment> {
    const clone = JSON.parse(JSON.stringify(payment)) as Payment;
    this.memoryCache.set(payment.id, clone);
    this.listCache = null; // Invalidate list cache
    if (payment.idempotencyKey) {
      this.idempotencyKeys.set(payment.idempotencyKey, payment.id);
    }

    try {
      await this.ensureTable();
      await prisma.$executeRawUnsafe(
        `INSERT INTO payments (
          id, status, mode, sender_wallet, recipient_name, recipient_phone,
          recipient_upi_id, recipient_bank_account, recipient_ifsc,
          source_asset, source_amount, destination_currency, destination_amount,
          exchange_rate, quote_id, idempotency_key, data, created_at, updated_at
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17::jsonb, $18::timestamptz, $19::timestamptz)
        ON CONFLICT (id) DO UPDATE SET
          status = EXCLUDED.status,
          destination_amount = EXCLUDED.destination_amount,
          exchange_rate = EXCLUDED.exchange_rate,
          quote_id = EXCLUDED.quote_id,
          data = EXCLUDED.data,
          updated_at = EXCLUDED.updated_at;`,
        clone.id,
        clone.status,
        clone.mode,
        clone.senderWallet,
        clone.recipient.name,
        clone.recipient.phone,
        clone.recipient.upiId || null,
        clone.recipient.bankAccount || null,
        clone.recipient.ifsc || null,
        clone.sourceAsset,
        clone.sourceAmount,
        clone.destinationCurrency,
        clone.destinationAmount ?? null,
        clone.exchangeRate ?? null,
        clone.quoteId || null,
        clone.idempotencyKey || null,
        JSON.stringify(clone),
        clone.createdAt,
        clone.updatedAt
      );
    } catch (err) {
      console.warn("[PostgresPaymentStore] DB write failed, retained in memory:", err instanceof Error ? err.message : err);
    }

    return clone;
  }

  async getPayment(id: string): Promise<Payment | null> {
    let payment = this.memoryCache.get(id);
    if (!payment) {
      if (id === "tx_priya_500") payment = this.memoryCache.get("PX-PRIYA500");
      else if (id === "tx_rahul_250") payment = this.memoryCache.get("PX-RAHUL250");
    }
    if (payment) return JSON.parse(JSON.stringify(payment)) as Payment;

    try {
      await this.ensureTable();
      const rows = await prisma.$queryRawUnsafe<Array<{ data: any }>>(
        "SELECT data FROM payments WHERE id = $1 LIMIT 1;",
        id
      );
      if (rows?.[0]?.data) {
        const loaded = rows[0].data as Payment;
        this.memoryCache.set(loaded.id, loaded);
        return loaded;
      }
    } catch {
      // Fallback
    }

    return null;
  }

  async listPayments(limit = 50): Promise<Payment[]> {
    const now = Date.now();
    if (this.listCache && (now - this.listCache.cachedAt) < this.LIST_CACHE_TTL_MS) {
      return JSON.parse(JSON.stringify(this.listCache.payments.slice(0, limit))) as Payment[];
    }

    try {
      await this.ensureTable();
      const rows = await prisma.$queryRawUnsafe<Array<{ data: any }>>(
        "SELECT data FROM payments ORDER BY created_at DESC LIMIT $1;",
        limit
      );
      if (rows && rows.length > 0) {
        const loaded = rows.map((r) => r.data as Payment);
        for (const p of loaded) {
          this.memoryCache.set(p.id, p);
        }
        this.listCache = {
          payments: loaded,
          cachedAt: now
        };
        return JSON.parse(JSON.stringify(loaded)) as Payment[];
      }
    } catch {
      // Fallback to in-memory
    }

    const list = Array.from(this.memoryCache.values())
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, limit);
    return JSON.parse(JSON.stringify(list)) as Payment[];
  }

  async recordIdempotency(key: string, paymentId: string): Promise<void> {
    this.idempotencyKeys.set(key, paymentId);
  }

  async getPaymentByIdempotencyKey(key: string): Promise<Payment | null> {
    const paymentId = this.idempotencyKeys.get(key);
    if (paymentId) return this.getPayment(paymentId);

    try {
      await this.ensureTable();
      const rows = await prisma.$queryRawUnsafe<Array<{ data: any }>>(
        "SELECT data FROM payments WHERE idempotency_key = $1 LIMIT 1;",
        key
      );
      if (rows?.[0]?.data) {
        const loaded = rows[0].data as Payment;
        this.memoryCache.set(loaded.id, loaded);
        this.idempotencyKeys.set(key, loaded.id);
        return loaded;
      }
    } catch {
      // Fallback
    }

    return null;
  }

  async reserveIdempotencyKey(key: string, executor: () => Promise<Payment>): Promise<Payment> {
    const existing = await this.getPaymentByIdempotencyKey(key);
    if (existing) return existing;

    const inFlight = this.inFlightIdempotency.get(key);
    if (inFlight) return inFlight;

    const promise = (async () => {
      try {
        const payment = await executor();
        await this.recordIdempotency(key, payment.id);
        return payment;
      } finally {
        this.inFlightIdempotency.delete(key);
      }
    })();

    this.inFlightIdempotency.set(key, promise);
    return promise;
  }

  clear(): void {
    this.memoryCache.clear();
    this.idempotencyKeys.clear();
    this.inFlightIdempotency.clear();
    this.listCache = null;
    prisma.$executeRawUnsafe("DELETE FROM payments;").catch(() => {});
  }
}

// Global persistent store instance for backend backed by Neon Postgres with in-memory caching
export const globalPaymentStore: IPaymentStore = new PostgresPaymentStore();

