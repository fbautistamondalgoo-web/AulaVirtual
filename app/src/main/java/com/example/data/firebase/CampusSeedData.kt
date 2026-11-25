package com.example.data.firebase

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

internal object CampusSeedData {

    fun buildStudentProfilePayload(user: FirebaseUser): Map<String, Any> {
        val uid = user.uid
        val displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "Estudiante Universitario"
        val email = user.email?.takeIf { it.isNotBlank() }
        val photoUrl = user.photoUrl?.toString()?.takeIf { it.isNotBlank() && it.length <= 500 }
        val codeSuffix = uid.filter { it.isDigit() }.take(5).padEnd(5, '4')

        return buildMap<String, Any?> {
            put("userId", uid)
            put("fullName", displayName.take(100))
            put("email", email?.take(120))
            put("studentCode", "U2026$codeSuffix")
            put("faculty", "Facultad de Ingeniería y Arquitectura")
            put("career", "Ingeniería de Sistemas")
            put("cycle", "2026-II · VI Ciclo")
            put("campus", "Campus Lima Sur · Villa El Salvador")
            put("modality", "Semipresencial")
            put("phone", "+51 990 412 835")
            put("photoUrl", photoUrl)
            put("seeded", true)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }.filterValues { it != null } as Map<String, Any>
    }

    fun enqueueInitialAcademicData(db: FirebaseFirestore, uid: String, studentName: String) {
        val userDoc = db.collection("users").document(uid)
        val batch = db.batch()
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        val courses = listOf(
            CourseSeed(
                id = "course_is601",
                code = "IS-601",
                name = "Arquitectura de Software",
                professor = "Mg. Carlos Paredes Vela",
                credits = 4L,
                section = "A1",
                modality = "Híbrido",
                classroom = "Pabellón B · Aula 304",
                scheduleSummary = "Lun y Mié · 08:00 - 10:15",
                dayOfWeek = 1L,
                startHour = 8L,
                startMinute = 0L,
                durationMinutes = 135L,
                colorHex = "#FF8F1C",
                syllabusSummary = "Estilos arquitectónicos, modelo C4, microservicios, patrones resilientes y atributos de calidad ISO/IEC 25010.",
                progressPercent = 65L,
                gradeEc1 = 17.0,
                gradeEp = 16.0,
                gradeEc2 = 16.5,
                gradeEf = 0.0,
                attendancePresent = 11L,
                attendanceTotal = 12L
            ),
            CourseSeed(
                id = "course_is602",
                code = "IS-602",
                name = "Desarrollo de Aplicaciones Móviles",
                professor = "Ing. Valeria Rojas Quispe",
                credits = 4L,
                section = "B2",
                modality = "Presencial",
                classroom = "Lab. Cómputo 03 · Piso 2",
                scheduleSummary = "Mar y Jue · 10:30 - 12:45",
                dayOfWeek = 2L,
                startHour = 10L,
                startMinute = 30L,
                durationMinutes = 135L,
                colorHex = "#D96B00",
                syllabusSummary = "Arquitectura MVVM en Kotlin, Jetpack Compose, Material Design 3, persistencia en nube y seguridad móvil.",
                progressPercent = 72L,
                gradeEc1 = 18.0,
                gradeEp = 17.0,
                gradeEc2 = 18.5,
                gradeEf = 0.0,
                attendancePresent = 12L,
                attendanceTotal = 12L
            ),
            CourseSeed(
                id = "course_is603",
                code = "IS-603",
                name = "Inteligencia de Negocios y Big Data",
                professor = "Dr. Ricardo Salinas Mendoza",
                credits = 3L,
                section = "A1",
                modality = "Remoto",
                classroom = "Sala Zoom · Enlace Campus",
                scheduleSummary = "Mié · 18:30 - 21:00",
                dayOfWeek = 3L,
                startHour = 18L,
                startMinute = 30L,
                durationMinutes = 150L,
                colorHex = "#1565C0",
                syllabusSummary = "Modelamiento dimensional, procesos ETL, Data Warehousing, analítica descriptiva y tableros ejecutivos.",
                progressPercent = 58L,
                gradeEc1 = 15.0,
                gradeEp = 14.5,
                gradeEc2 = 16.0,
                gradeEf = 0.0,
                attendancePresent = 9L,
                attendanceTotal = 11L
            ),
            CourseSeed(
                id = "course_is604",
                code = "IS-604",
                name = "Gestión de Proyectos TI (PMBOK)",
                professor = "Mg. Patricia Huamán Flores",
                credits = 3L,
                section = "C1",
                modality = "Híbrido",
                classroom = "Pabellón A · Aula 205",
                scheduleSummary = "Vie · 16:00 - 18:30",
                dayOfWeek = 5L,
                startHour = 16L,
                startMinute = 0L,
                durationMinutes = 150L,
                colorHex = "#18864B",
                syllabusSummary = "Planificación ágil y predictiva, gestión de alcance, cronograma, costos, riesgos y cierre de proyectos tecnológicos.",
                progressPercent = 60L,
                gradeEc1 = 16.0,
                gradeEp = 15.5,
                gradeEc2 = 0.0,
                gradeEf = 0.0,
                attendancePresent = 10L,
                attendanceTotal = 11L
            ),
            CourseSeed(
                id = "course_hu301",
                code = "HU-301",
                name = "Ética Profesional y Liderazgo",
                professor = "Dra. Sofía Navarro León",
                credits = 2L,
                section = "A2",
                modality = "Remoto",
                classroom = "Aula Virtual Síncrona",
                scheduleSummary = "Sáb · 09:00 - 11:15",
                dayOfWeek = 6L,
                startHour = 9L,
                startMinute = 0L,
                durationMinutes = 135L,
                colorHex = "#C88A00",
                syllabusSummary = "Deontología en ingeniería, ética digital, privacidad de datos, liderazgo colaborativo y responsabilidad social.",
                progressPercent = 75L,
                gradeEc1 = 17.5,
                gradeEp = 18.0,
                gradeEc2 = 17.0,
                gradeEf = 0.0,
                attendancePresent = 11L,
                attendanceTotal = 11L
            )
        )

        courses.forEach { c ->
            batch.set(
                userDoc.collection("courses").document(c.id),
                mapOf(
                    "userId" to uid,
                    "code" to c.code,
                    "name" to c.name,
                    "professor" to c.professor,
                    "credits" to c.credits,
                    "section" to c.section,
                    "modality" to c.modality,
                    "classroom" to c.classroom,
                    "scheduleSummary" to c.scheduleSummary,
                    "dayOfWeek" to c.dayOfWeek,
                    "startHour" to c.startHour,
                    "startMinute" to c.startMinute,
                    "durationMinutes" to c.durationMinutes,
                    "colorHex" to c.colorHex,
                    "syllabusSummary" to c.syllabusSummary,
                    "progressPercent" to c.progressPercent,
                    "gradeEc1" to c.gradeEc1,
                    "gradeEp" to c.gradeEp,
                    "gradeEc2" to c.gradeEc2,
                    "gradeEf" to c.gradeEf,
                    "attendancePresent" to c.attendancePresent,
                    "attendanceTotal" to c.attendanceTotal,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            // 4 módulos por curso
            val modules = listOf(
                Triple(1L, "Semana 1: Fundamentos y Marco Conceptual", "PDF") to true,
                Triple(2L, "Semana 2: Casos de Estudio Aplicados", "Video") to true,
                Triple(3L, "Semana 3: Taller Práctico Guiado", "Lectura") to (c.progressPercent >= 65L),
                Triple(4L, "Semana 4: Evaluación de Avance y Rúbrica", "Enlace") to false
            )
            modules.forEachIndexed { idx, (triple, completed) ->
                val (week, title, type) = triple
                batch.set(
                    userDoc.collection("modules").document("${c.id}_mod_$week"),
                    mapOf(
                        "userId" to uid,
                        "courseId" to c.id,
                        "weekNumber" to week,
                        "title" to title,
                        "description" to "Material académico oficial de la semana $week para ${c.name}.",
                        "resourceLabel" to "${c.code}_Semana_0${idx + 1}.$type",
                        "resourceType" to type,
                        "completed" to completed,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            // 2 registros de asistencia recientes por curso
            batch.set(
                userDoc.collection("attendance_logs").document("${c.id}_att_1"),
                mapOf(
                    "userId" to uid,
                    "courseId" to c.id,
                    "sessionTitle" to "Sesión 11 · Clase Teórico-Práctica",
                    "dateLabel" to "18 Nov 2026",
                    "status" to "Presente",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                userDoc.collection("attendance_logs").document("${c.id}_att_2"),
                mapOf(
                    "userId" to uid,
                    "courseId" to c.id,
                    "sessionTitle" to "Sesión 12 · Taller de Laboratorio",
                    "dateLabel" to "23 Nov 2026",
                    "status" to if (c.id == "course_is603") "Tardanza" else "Presente",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // Tareas y evaluaciones
        val tasks = listOf(
            mapOf(
                "id" to "task_1",
                "courseId" to "course_is601",
                "courseCode" to "IS-601",
                "courseName" to "Arquitectura de Software",
                "title" to "Informe Técnico: Diagrama C4 y ADR",
                "description" to "Documentar los 3 niveles C4 de un sistema de matrícula universitaria y redactar 2 Architecture Decision Records.",
                "taskType" to "Proyecto",
                "dueLabel" to "Vie 27 Nov · 23:59",
                "dueEpochMs" to (now + 2 * dayMs),
                "status" to "Pendiente",
                "maxScore" to 20.0
            ),
            mapOf(
                "id" to "task_2",
                "courseId" to "course_is602",
                "courseCode" to "IS-602",
                "courseName" to "Desarrollo de Aplicaciones Móviles",
                "title" to "Laboratorio 09: Compose + Cloud Firestore",
                "description" to "Implementar flujos reactivos en tiempo real con Kotlin Flow y reglas de seguridad por usuario.",
                "taskType" to "Tarea",
                "dueLabel" to "Sáb 28 Nov · 23:59",
                "dueEpochMs" to (now + 3 * dayMs),
                "status" to "Pendiente",
                "maxScore" to 20.0
            ),
            mapOf(
                "id" to "task_3",
                "courseId" to "course_is603",
                "courseCode" to "IS-603",
                "courseName" to "Inteligencia de Negocios y Big Data",
                "title" to "Evaluación Continua 2: Modelo Estrella",
                "description" to "Cuestionario práctico sobre tablas de hechos, dimensiones y consultas analíticas.",
                "taskType" to "Examen",
                "dueLabel" to "Lun 30 Nov · 19:00",
                "dueEpochMs" to (now + 5 * dayMs),
                "status" to "Pendiente",
                "maxScore" to 20.0
            ),
            mapOf(
                "id" to "task_4",
                "courseId" to "course_is604",
                "courseCode" to "IS-604",
                "courseName" to "Gestión de Proyectos TI (PMBOK)",
                "title" to "Matriz de Riesgos y Cronograma EDT",
                "description" to "Subir archivo con estructura de desglose de trabajo y matriz de probabilidad e impacto.",
                "taskType" to "Tarea",
                "dueLabel" to "Mié 02 Dic · 23:59",
                "dueEpochMs" to (now + 7 * dayMs),
                "status" to "Pendiente",
                "maxScore" to 20.0
            ),
            mapOf(
                "id" to "task_5",
                "courseId" to "course_hu301",
                "courseCode" to "HU-301",
                "courseName" to "Ética Profesional y Liderazgo",
                "title" to "Foro Debatido: Gobernanza de Datos",
                "description" to "Analizar un caso real sobre protección de datos personales en plataformas educativas.",
                "taskType" to "Foro",
                "dueLabel" to "Entregado · 22 Nov",
                "dueEpochMs" to (now - 2 * dayMs),
                "status" to "Calificado",
                "submissionNote" to "Aporte analítico publicado con referencias a la Ley N° 29733.",
                "score" to 18.0,
                "maxScore" to 20.0
            ),
            mapOf(
                "id" to "task_6",
                "courseId" to "course_is601",
                "courseCode" to "IS-601",
                "courseName" to "Arquitectura de Software",
                "title" to "Examen Parcial Teórico-Práctico",
                "description" to "Evaluación parcial de patrones arquitectónicos y calidad de software.",
                "taskType" to "Examen",
                "dueLabel" to "Calificado · 14 Nov",
                "dueEpochMs" to (now - 10 * dayMs),
                "status" to "Calificado",
                "submissionNote" to "Evaluación presencial rendida en Pabellón B.",
                "score" to 16.0,
                "maxScore" to 20.0
            )
        )

        tasks.forEach { t ->
            val taskId = t["id"] as String
            val payload = buildMap<String, Any?> {
                put("userId", uid)
                put("courseId", t["courseId"])
                put("courseCode", t["courseCode"])
                put("courseName", t["courseName"])
                put("title", t["title"])
                put("description", t["description"])
                put("taskType", t["taskType"])
                put("dueLabel", t["dueLabel"])
                put("dueEpochMs", t["dueEpochMs"])
                put("status", t["status"])
                put("submissionNote", t["submissionNote"])
                put("score", t["score"])
                put("maxScore", t["maxScore"])
                put("createdAt", FieldValue.serverTimestamp())
                put("updatedAt", FieldValue.serverTimestamp())
            }.filterValues { it != null } as Map<String, Any>
            batch.set(userDoc.collection("tasks").document(taskId), payload)
        }

        // Anuncios y foros
        val posts = listOf(
            mapOf(
                "id" to "post_1",
                "courseId" to "course_is601",
                "courseName" to "Arquitectura de Software",
                "authorName" to "Mg. Carlos Paredes Vela",
                "authorRole" to "Docente",
                "title" to "Lineamientos para la Sustentación del Proyecto C4",
                "content" to "Estimados estudiantes, recuerden adjuntar su informe técnico hasta el viernes 27. Las exposiciones serán por equipos de 4 integrantes en el Pabellón B - Aula 304.",
                "pinned" to true,
                "likesCount" to 14L,
                "repliesCount" to 3L
            ),
            mapOf(
                "id" to "post_2",
                "courseId" to "course_is602",
                "courseName" to "Desarrollo de Aplicaciones Móviles",
                "authorName" to "Ing. Valeria Rojas Quispe",
                "authorRole" to "Docente",
                "title" to "Rúbrica Actualizada · Laboratorio 09",
                "content" to "Se ha habilitado la plantilla de evaluación y el enlace de repositorio base en la Semana 4. Ante cualquier consulta, responder en este hilo.",
                "pinned" to true,
                "likesCount" to 19L,
                "repliesCount" to 5L
            ),
            mapOf(
                "id" to "post_3",
                "courseId" to "course_is603",
                "courseName" to "Inteligencia de Negocios y Big Data",
                "authorName" to studentName.take(100),
                "authorRole" to "Estudiante",
                "title" to "Consulta sobre granularidad de la tabla de hechos",
                "content" to "Profesor, ¿para la EC2 debemos considerar la granularidad diaria o por transacción en el esquema estrella?",
                "pinned" to false,
                "likesCount" to 4L,
                "repliesCount" to 1L
            )
        )
        posts.forEach { p ->
            val postId = p["id"] as String
            batch.set(
                userDoc.collection("forum_posts").document(postId),
                mapOf(
                    "userId" to uid,
                    "courseId" to p["courseId"]!!,
                    "courseName" to p["courseName"]!!,
                    "authorName" to p["authorName"]!!,
                    "authorRole" to p["authorRole"]!!,
                    "title" to p["title"]!!,
                    "content" to p["content"]!!,
                    "pinned" to p["pinned"]!!,
                    "likesCount" to p["likesCount"]!!,
                    "repliesCount" to p["repliesCount"]!!,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // Estado de cuenta / Pensiones
        val payments = listOf(
            mapOf("id" to "pay_1", "concept" to "Matrícula Ciclo Regular 2026-II", "period" to "2026-II", "dueDateLabel" to "15 Ago 2026", "amountSoles" to 380.0, "status" to "Pagado", "receiptCode" to "BOL-2026-01482"),
            mapOf("id" to "pay_2", "concept" to "Cuota 1 · Pensión Académica", "period" to "2026-II", "dueDateLabel" to "30 Set 2026", "amountSoles" to 690.0, "status" to "Pagado", "receiptCode" to "BOL-2026-04910"),
            mapOf("id" to "pay_3", "concept" to "Cuota 2 · Pensión Académica", "period" to "2026-II", "dueDateLabel" to "30 Oct 2026", "amountSoles" to 690.0, "status" to "Pagado", "receiptCode" to "BOL-2026-07834"),
            mapOf("id" to "pay_4", "concept" to "Cuota 3 · Pensión Académica", "period" to "2026-II", "dueDateLabel" to "30 Nov 2026", "amountSoles" to 690.0, "status" to "Pendiente", "receiptCode" to null),
            mapOf("id" to "pay_5", "concept" to "Cuota 4 · Pensión Académica", "period" to "2026-II", "dueDateLabel" to "20 Dic 2026", "amountSoles" to 690.0, "status" to "Pendiente", "receiptCode" to null)
        )
        payments.forEach { pay ->
            val payId = pay["id"] as String
            val payload = buildMap<String, Any?> {
                put("userId", uid)
                put("concept", pay["concept"])
                put("period", pay["period"])
                put("dueDateLabel", pay["dueDateLabel"])
                put("amountSoles", pay["amountSoles"])
                put("status", pay["status"])
                put("receiptCode", pay["receiptCode"])
                put("createdAt", FieldValue.serverTimestamp())
                put("updatedAt", FieldValue.serverTimestamp())
            }.filterValues { it != null } as Map<String, Any>
            batch.set(userDoc.collection("payments").document(payId), payload)
        }

        // Reservas de campus
        val reservations = listOf(
            mapOf(
                "id" to "res_1",
                "spaceName" to "Cubículo de Estudio Grupal 04",
                "location" to "Biblioteca Central · Piso 2",
                "dateLabel" to "26 Nov 2026",
                "timeSlot" to "14:00 - 16:00",
                "status" to "Confirmada"
            ),
            mapOf(
                "id" to "res_2",
                "spaceName" to "Laboratorio Mac & Android",
                "location" to "Pabellón de Ingeniería · Piso 3",
                "dateLabel" to "28 Nov 2026",
                "timeSlot" to "10:00 - 12:00",
                "status" to "Confirmada"
            )
        )
        reservations.forEach { r ->
            val resId = r["id"] as String
            batch.set(
                userDoc.collection("reservations").document(resId),
                mapOf(
                    "userId" to uid,
                    "spaceName" to r["spaceName"]!!,
                    "location" to r["location"]!!,
                    "dateLabel" to r["dateLabel"]!!,
                    "timeSlot" to r["timeSlot"]!!,
                    "status" to r["status"]!!,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        // Mensajes y trámites
        val messages = listOf(
            mapOf(
                "id" to "msg_1",
                "recipient" to "Servicios Académicos",
                "category" to "Trámite",
                "subject" to "Constancia de Matrícula 2026-II",
                "body" to "Estimados, solicito la emisión digital de mi constancia de matrícula regular del presente ciclo.",
                "status" to "Respondido",
                "replyText" to "Su constancia fue emitida con código TRM-2026-9021 y se encuentra validada en su expediente."
            ),
            mapOf(
                "id" to "msg_2",
                "recipient" to "Mg. Carlos Paredes Vela",
                "category" to "Docente",
                "subject" to "Revisión de avance de Proyecto C4",
                "body" to "Buenas tardes profesor, ¿podríamos validar nuestro diagrama de contenedores antes de la entrega final?",
                "status" to "Enviado",
                "replyText" to null
            )
        )
        messages.forEach { m ->
            val msgId = m["id"] as String
            val payload = buildMap<String, Any?> {
                put("userId", uid)
                put("recipient", m["recipient"])
                put("category", m["category"])
                put("subject", m["subject"])
                put("body", m["body"])
                put("status", m["status"])
                put("replyText", m["replyText"])
                put("createdAt", FieldValue.serverTimestamp())
                put("updatedAt", FieldValue.serverTimestamp())
            }.filterValues { it != null } as Map<String, Any>
            batch.set(userDoc.collection("messages").document(msgId), payload)
        }

        batch.commit()
    }

    private data class CourseSeed(
        val id: String,
        val code: String,
        val name: String,
        val professor: String,
        val credits: Long,
        val section: String,
        val modality: String,
        val classroom: String,
        val scheduleSummary: String,
        val dayOfWeek: Long,
        val startHour: Long,
        val startMinute: Long,
        val durationMinutes: Long,
        val colorHex: String,
        val syllabusSummary: String,
        val progressPercent: Long,
        val gradeEc1: Double,
        val gradeEp: Double,
        val gradeEc2: Double,
        val gradeEf: Double,
        val attendancePresent: Long,
        val attendanceTotal: Long
    )
}
