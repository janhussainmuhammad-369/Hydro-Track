const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read profile or logs", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("waterLogs").get());
});

test("Authenticated user: can create and read their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(
    profileRef.set({
      userId: ALICE_UID,
      displayName: "Alice",
      dailyGoalMl: 2500,
      unit: "ml",
      reminderEnabled: true,
      reminderIntervalMinutes: 60,
      reminderStartTime: "08:00",
      reminderEndTime: "22:00",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(profileRef.get());
});

test("Authenticated user: cannot read another user's profile or logs", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      dailyGoalMl: 2000,
      createdAt: new Date(),
    });
    await context.firestore().collection("users").doc(ALICE_UID).collection("waterLogs").doc("log_1").set({
      userId: ALICE_UID,
      amountMl: 250,
      beverageType: "water",
      dateString: "2026-10-21",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("waterLogs").doc("log_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("waterLogs").get());
});

test("Authenticated user: can log water and query their own logs", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const logRef = aliceDb.collection("users").doc(ALICE_UID).collection("waterLogs").doc("alice_log_1");

  await assertSucceeds(
    logRef.set({
      userId: ALICE_UID,
      amountMl: 350,
      beverageType: "water",
      container: "glass",
      dateString: "2026-10-21",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("waterLogs").get()
  );
});

test("Authenticated user: cannot forge logs with invalid amount", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const logRef = aliceDb.collection("users").doc(ALICE_UID).collection("waterLogs").doc("bad_log");

  // negative or zero amount
  await assertFails(
    logRef.set({
      userId: ALICE_UID,
      amountMl: 0,
      beverageType: "water",
      dateString: "2026-10-21",
      createdAt: new Date(),
    })
  );
});
