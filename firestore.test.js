const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_student_123";
const BOB_UID = "bob_student_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
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

test("Unauthenticated user cannot read or write any campus collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("courses").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("tasks").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Hacker",
      studentCode: "U00000",
      faculty: "Ing",
      career: "Sistemas",
      cycle: "2026-II",
      campus: "Lima Sur",
      modality: "Presencial",
      seeded: true,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Student profile: valid create and partial update succeed; cross-user and immutable edits fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Lucia Mendoza",
      email: "lucia@autonoma.edu.pe",
      studentCode: "U202614820",
      faculty: "Ingenieria y Arquitectura",
      career: "Ingenieria de Sistemas",
      cycle: "2026-II · VI Ciclo",
      campus: "Campus Villa El Salvador",
      modality: "Semipresencial",
      seeded: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Partial update of mutable fields succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      phone: "+51 987 654 321",
      modality: "Presencial",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject studentCode or createdAt tampering
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      studentCode: "U99999999",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field injection
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject cross-user read or write
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      fullName: "Bob",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Courses, modules, tasks, forum posts, and attendance logs enforce schema & ownership", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("courses").doc("course_is601").set({
      userId: ALICE_UID,
      code: "IS-601",
      name: "Arquitectura de Software",
      professor: "Mg. Carlos Paredes",
      credits: 4,
      section: "A1",
      modality: "Híbrido",
      classroom: "Pabellón B - Aula 304",
      scheduleSummary: "Lun y Mié 08:00 - 10:15",
      dayOfWeek: 1,
      startHour: 8,
      startMinute: 0,
      durationMinutes: 135,
      colorHex: "#FF8F1C",
      syllabusSummary: "Patrones arquitectónicos, microservicios y calidad.",
      progressPercent: 45,
      gradeEc1: 16.5,
      gradeEp: 15.0,
      gradeEc2: 0.0,
      gradeEf: 0.0,
      attendancePresent: 8,
      attendanceTotal: 9,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("modules").doc("mod_1").set({
      userId: ALICE_UID,
      courseId: "course_is601",
      weekNumber: 1,
      title: "Semana 1: Fundamentos de Arquitectura",
      description: "Revisión de atributos de calidad ISO 25010.",
      resourceLabel: "Diapositivas_Semana_01.pdf",
      resourceType: "PDF",
      completed: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("modules").doc("mod_1").update({
      completed: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Task create without optional fields, then submit
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_1").set({
      userId: ALICE_UID,
      courseId: "course_is601",
      courseCode: "IS-601",
      courseName: "Arquitectura de Software",
      title: "Informe de Patrones C4",
      description: "Elaborar diagrama de contexto y contenedores.",
      taskType: "Tarea",
      dueLabel: "Vie 28 Nov · 23:59",
      dueEpochMs: 1795910340000,
      status: "Pendiente",
      maxScore: 20,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_1").update({
      status: "Entregado",
      submissionNote: "Adjunto diagrama C4 completo.",
      attachmentName: "diagrama_c4.png",
      updatedAt: new Date(Date.now() - 400),
    })
  );

  // Reject extra @DocumentId 'id' field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_bad").set({
      id: "task_bad",
      userId: ALICE_UID,
      courseId: "course_is601",
      courseCode: "IS-601",
      courseName: "Arquitectura de Software",
      title: "Tarea invalida",
      description: "No debe incluir id",
      taskType: "Tarea",
      dueLabel: "Hoy",
      dueEpochMs: 1795910340000,
      status: "Pendiente",
      maxScore: 20,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("forum_posts").doc("post_future").set({
      userId: ALICE_UID,
      courseId: "course_is601",
      courseName: "Arquitectura de Software",
      authorName: "Lucia",
      authorRole: "Estudiante",
      title: "Consulta",
      content: "Timestamp futuro",
      pinned: false,
      likesCount: 0,
      repliesCount: 0,
      createdAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Subcollection list queries succeed for owner and fail for other user
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("courses").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("courses").get()
  );
});

test("Payments, reservations, and campus messages validate bounds and ownership", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 3000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("payments").doc("pay_1").set({
      userId: ALICE_UID,
      concept: "Cuota 3 - Pensión 2026-II",
      period: "2026-II",
      dueDateLabel: "30 Nov 2026",
      amountSoles: 680.0,
      status: "Pendiente",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("payments").doc("pay_1").update({
      status: "Pagado",
      receiptCode: "BOL-2026-88412",
      updatedAt: new Date(Date.now() - 300),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("reservations").doc("res_1").set({
      userId: ALICE_UID,
      spaceName: "Sala de Estudio Grupal 04",
      location: "Biblioteca Central · Piso 2",
      dateLabel: "26 Nov 2026",
      timeSlot: "14:00 - 16:00",
      status: "Confirmada",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("messages").doc("msg_1").set({
      userId: ALICE_UID,
      recipient: "Servicios Académicos",
      category: "Trámite",
      subject: "Solicitud de Constancia de Matrícula",
      body: "Solicito constancia digital del ciclo 2026-II.",
      status: "Enviado",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("payments").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("reservations").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("messages").get()
  );
});
