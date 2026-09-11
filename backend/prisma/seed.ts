import { prisma } from "../src/prisma.js";
import { CORRIDORS } from "../src/lib/index.js";

async function main(): Promise<void> {
  for (const c of CORRIDORS) {
    await prisma.corridor.upsert({
      where: { corridorKey: c.id },
      update: {
        feeBps: c.feeBps,
        etaSeconds: c.etaSeconds,
        enabled: c.enabled,
        inProvider: c.inProvider,
        outProvider: c.outProvider
      },
      create: {
        corridorKey: c.id,
        sourceCurrency: c.sourceCurrency,
        destCurrency: c.destCurrency,
        destRail: c.destRail,
        inProvider: c.inProvider,
        outProvider: c.outProvider,
        feeBps: c.feeBps,
        etaSeconds: c.etaSeconds,
        enabled: c.enabled
      }
    });
  }

  await prisma.exchangeRate.upsert({
    where: { baseCurrency_quoteCurrency: { baseCurrency: "USD", quoteCurrency: "INR" } },
    update: {},
    create: {
      baseCurrency: "USD",
      quoteCurrency: "INR",
      rate: 83.42,
      cheaperPercentage: 2.3,
      asOf: new Date()
    }
  });

  console.log("Seed complete.");
}

main()
  .catch((err: unknown) => {
    console.error(err);
    process.exitCode = 1;
  })
  .finally(() => {
    void prisma.$disconnect();
  });
