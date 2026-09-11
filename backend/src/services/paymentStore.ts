import type {
  Payment,
  WebhookEventRecord
} from "../lib/index.js";

export interface IPaymentStore {
  savePayment(payment: Payment): Promise<Payment>;
  getPayment(id: string): Promise<Payment | null>;
  listPayments(limit?: number): Promise<Payment[]>;
  recordIdempotency(key: string, paymentId: string): Promise<void>;
  getPaymentByIdempotencyKey(key: string): Promise<Payment | null>;
  reserveIdempotencyKey(key: string, executor: () => Promise<Payment>): Promise<Payment>;
  hasWebhookProcessed(eventId: string): Promise<boolean>;
  acquireWebhookLock(eventId: string): Promise<boolean>;
  releaseWebhookLock(eventId: string): Promise<void>;
  recordWebhook(record: WebhookEventRecord): Promise<void>;
  getWebhook(eventId: string): Promise<WebhookEventRecord | null>;
}

export class InMemoryPaymentStore implements IPaymentStore {
  private payments = new Map<string, Payment>();
  private idempotencyKeys = new Map<string, string>(); // idempotencyKey -> paymentId
  private inFlightIdempotency = new Map<string, Promise<Payment>>();
  private webhooks = new Map<string, WebhookEventRecord>(); // eventId -> WebhookEventRecord
  private inFlightWebhooks = new Set<string>();

  async savePayment(payment: Payment): Promise<Payment> {
    const clone = JSON.parse(JSON.stringify(payment)) as Payment;
    this.payments.set(payment.id, clone);
    if (payment.idempotencyKey) {
      this.idempotencyKeys.set(payment.idempotencyKey, payment.id);
    }
    return clone;
  }

  async getPayment(id: string): Promise<Payment | null> {
    const payment = this.payments.get(id);
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

  async hasWebhookProcessed(eventId: string): Promise<boolean> {
    const record = this.webhooks.get(eventId);
    return Boolean(record && record.status === "processed");
  }

  // Synchronously acquires lock on eventId to guard against concurrent duplicate webhook deliveries.
  async acquireWebhookLock(eventId: string): Promise<boolean> {
    const processed = await this.hasWebhookProcessed(eventId);
    if (processed || this.inFlightWebhooks.has(eventId)) {
      return false; // Lock denied, already processed or processing
    }
    this.inFlightWebhooks.add(eventId);
    return true;
  }

  async releaseWebhookLock(eventId: string): Promise<void> {
    this.inFlightWebhooks.delete(eventId);
  }

  async recordWebhook(record: WebhookEventRecord): Promise<void> {
    this.webhooks.set(record.eventId, JSON.parse(JSON.stringify(record)) as WebhookEventRecord);
  }

  async getWebhook(eventId: string): Promise<WebhookEventRecord | null> {
    const record = this.webhooks.get(eventId);
    return record ? (JSON.parse(JSON.stringify(record)) as WebhookEventRecord) : null;
  }

  // Clear helper for tests
  clear(): void {
    this.payments.clear();
    this.idempotencyKeys.clear();
    this.inFlightIdempotency.clear();
    this.webhooks.clear();
    this.inFlightWebhooks.clear();
  }
}

// Global store instance for backend
export const globalPaymentStore = new InMemoryPaymentStore();
