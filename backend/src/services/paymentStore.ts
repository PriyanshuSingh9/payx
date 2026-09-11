import type { Payment } from "../lib/index.js";

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

// Global store instance for backend
export const globalPaymentStore = new InMemoryPaymentStore();
