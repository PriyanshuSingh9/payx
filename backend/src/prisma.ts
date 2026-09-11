import { PrismaClient } from "../prisma/generated/client/client.js";
import { PrismaNeon } from "@prisma/adapter-neon";

const connectionString = process.env["DIRECT_URL"] ?? process.env["DATABASE_URL"];
if (!connectionString) {
  throw new Error("DIRECT_URL or DATABASE_URL is required.");
}

const adapter = new PrismaNeon({ connectionString });

export const prisma = new PrismaClient({ adapter });
