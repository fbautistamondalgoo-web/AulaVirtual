package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.AttendanceLog
import com.example.data.model.CampusMessage
import com.example.data.model.Course
import com.example.data.model.CourseModule
import com.example.data.model.CourseTask
import com.example.data.model.ForumPost
import com.example.data.model.SpaceReservation
import com.example.data.model.StudentProfile
import com.example.data.model.TuitionPayment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CampusRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Debes iniciar sesión con Google para acceder al Aula Virtual.")
    }

    private fun <T> observeSubcollection(
        userId: String,
        collectionName: String,
        clazz: Class<T>
    ): Flow<List<T>> = flow {
        require(userId.isNotBlank()) { "El ID de usuario es obligatorio." }
        val path = "users/$userId/$collectionName"
        emitAll(
            db.collection("users").document(userId).collection(collectionName)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(clazz, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeProfile(userId: String): Flow<StudentProfile?> = flow {
        require(userId.isNotBlank()) { "El ID de usuario es obligatorio." }
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(StudentProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeCourses(userId: String): Flow<List<Course>> =
        observeSubcollection(userId, "courses", Course::class.java)

    fun observeAllModules(userId: String): Flow<List<CourseModule>> =
        observeSubcollection(userId, "modules", CourseModule::class.java)

    fun observeTasks(userId: String): Flow<List<CourseTask>> =
        observeSubcollection(userId, "tasks", CourseTask::class.java)

    fun observeForumPosts(userId: String): Flow<List<ForumPost>> =
        observeSubcollection(userId, "forum_posts", ForumPost::class.java)

    fun observeAttendanceLogs(userId: String): Flow<List<AttendanceLog>> =
        observeSubcollection(userId, "attendance_logs", AttendanceLog::class.java)

    fun observePayments(userId: String): Flow<List<TuitionPayment>> =
        observeSubcollection(userId, "payments", TuitionPayment::class.java)

    fun observeReservations(userId: String): Flow<List<SpaceReservation>> =
        observeSubcollection(userId, "reservations", SpaceReservation::class.java)

    fun observeMessages(userId: String): Flow<List<CampusMessage>> =
        observeSubcollection(userId, "messages", CampusMessage::class.java)

    suspend fun ensureStudentSeeded(): Result<Unit> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("Usuario no autenticado."))
        val uid = user.uid
        val path = "users/$uid"
        return try {
            val docRef = db.collection("users").document(uid)
            val snapshot = docRef.get().await()
            val alreadySeeded = snapshot.exists() && (snapshot.getBoolean("seeded") == true)
            if (!alreadySeeded) {
                val profilePayload = CampusSeedData.buildStudentProfilePayload(user)
                if (!snapshot.exists()) {
                    docRef.set(profilePayload).await()
                } else {
                    docRef.update(
                        mapOf(
                            "seeded" to true,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
                val studentName = (profilePayload["fullName"] as? String) ?: "Estudiante"
                CampusSeedData.enqueueInitialAcademicData(db, uid, studentName)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        fullName: String,
        faculty: String,
        career: String,
        cycle: String,
        campus: String,
        modality: String,
        phone: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val path = "users/$uid"
        return try {
            require(fullName.trim().length in 1..100) { "Nombre inválido." }
            require(faculty.trim().length in 1..100) { "Facultad inválida." }
            require(career.trim().length in 1..100) { "Carrera inválida." }
            require(cycle.trim().length in 1..30) { "Ciclo inválido." }
            require(campus.trim().length in 1..60) { "Sede inválida." }
            require(modality in listOf("Presencial", "Semipresencial", "Virtual")) { "Modalidad inválida." }

            val updates = buildMap<String, Any?> {
                put("fullName", fullName.trim())
                put("faculty", faculty.trim())
                put("career", career.trim())
                put("cycle", cycle.trim())
                put("campus", campus.trim())
                put("modality", modality)
                put("phone", phone.trim().take(30))
                put("updatedAt", FieldValue.serverTimestamp())
            }.filterValues { it != null } as Map<String, Any>

            db.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun enrollCourse(
        code: String,
        name: String,
        professor: String,
        credits: Long,
        modality: String,
        classroom: String,
        dayOfWeek: Long,
        startHour: Long,
        scheduleSummary: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val docRef = db.collection("users").document(uid).collection("courses").document()
        return try {
            require(code.trim().length in 2..20) { "El código debe tener entre 2 y 20 caracteres." }
            require(name.trim().length in 2..120) { "El nombre del curso debe tener al menos 2 caracteres." }
            require(professor.trim().length in 2..100) { "Ingresa el nombre del docente." }
            require(credits in 1L..10L) { "Los créditos deben estar entre 1 y 10." }
            require(modality in listOf("Presencial", "Remoto", "Híbrido")) { "Modalidad inválida." }
            require(classroom.trim().length in 1..80) { "Ingresa el aula o enlace." }

            val courseId = docRef.id
            val batch = db.batch()
            batch.set(
                docRef,
                mapOf(
                    "userId" to uid,
                    "code" to code.trim().uppercase(),
                    "name" to name.trim(),
                    "professor" to professor.trim(),
                    "credits" to credits,
                    "section" to "A1",
                    "modality" to modality,
                    "classroom" to classroom.trim(),
                    "scheduleSummary" to scheduleSummary.trim().ifBlank { "Horario por definir" }.take(100),
                    "dayOfWeek" to dayOfWeek.coerceIn(1L, 7L),
                    "startHour" to startHour.coerceIn(7L, 21L),
                    "startMinute" to 0L,
                    "durationMinutes" to 120L,
                    "colorHex" to "#FF8F1C",
                    "syllabusSummary" to "Sílabo oficial del curso ${name.trim()} - Ciclo 2026-II.",
                    "progressPercent" to 10L,
                    "gradeEc1" to 0.0,
                    "gradeEp" to 0.0,
                    "gradeEc2" to 0.0,
                    "gradeEf" to 0.0,
                    "attendancePresent" to 1L,
                    "attendanceTotal" to 1L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val modRef = db.collection("users").document(uid).collection("modules").document()
            batch.set(
                modRef,
                mapOf(
                    "userId" to uid,
                    "courseId" to courseId,
                    "weekNumber" to 1L,
                    "title" to "Semana 1: Introducción al curso",
                    "description" to "Presentación del sílabo y metodología de evaluación.",
                    "resourceLabel" to "${code.trim().uppercase()}_Silabo.pdf",
                    "resourceType" to "PDF",
                    "completed" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleModuleCompleted(moduleId: String, completed: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val path = "users/$uid/modules/$moduleId"
        return try {
            require(moduleId.isNotBlank())
            db.collection("users").document(uid).collection("modules").document(moduleId)
                .update(
                    mapOf(
                        "completed" to completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun addModule(
        courseId: String,
        weekNumber: Long,
        title: String,
        description: String,
        resourceLabel: String,
        resourceType: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val docRef = db.collection("users").document(uid).collection("modules").document()
        return try {
            require(courseId.isNotBlank())
            require(weekNumber in 1L..20L)
            require(title.trim().length in 2..140)
            require(resourceType in listOf("PDF", "Video", "Lectura", "Enlace"))
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "courseId" to courseId,
                    "weekNumber" to weekNumber,
                    "title" to title.trim(),
                    "description" to description.trim().take(500),
                    "resourceLabel" to resourceLabel.trim().ifBlank { "Recurso_Semana_$weekNumber" }.take(100),
                    "resourceType" to resourceType,
                    "completed" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createTask(
        course: Course,
        title: String,
        description: String,
        taskType: String,
        dueLabel: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val docRef = db.collection("users").document(uid).collection("tasks").document()
        return try {
            require(course.id.isNotBlank())
            require(title.trim().length in 2..140) { "Título inválido." }
            require(taskType in listOf("Tarea", "Examen", "Foro", "Proyecto"))
            require(dueLabel.trim().length in 1..60) { "Fecha límite requerida." }
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "courseId" to course.id,
                    "courseCode" to course.code.take(20),
                    "courseName" to course.name.take(120),
                    "title" to title.trim(),
                    "description" to description.trim().take(600),
                    "taskType" to taskType,
                    "dueLabel" to dueLabel.trim(),
                    "dueEpochMs" to (System.currentTimeMillis() + 3 * 86_400_000L),
                    "status" to "Pendiente",
                    "maxScore" to 20.0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun submitTask(
        taskId: String,
        submissionNote: String,
        attachmentName: String?
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val path = "users/$uid/tasks/$taskId"
        return try {
            require(taskId.isNotBlank())
            val updates = buildMap<String, Any?> {
                put("status", "Entregado")
                put("submissionNote", submissionNote.trim().ifBlank { "Entrega registrada desde el Aula Virtual Android." }.take(600))
                put("attachmentName", attachmentName?.trim()?.takeIf { it.isNotBlank() }?.take(120))
                put("updatedAt", FieldValue.serverTimestamp())
            }.filterValues { it != null } as Map<String, Any>

            db.collection("users").document(uid).collection("tasks").document(taskId)
                .update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateCourseGrades(
        courseId: String,
        ec1: Double,
        ep: Double,
        ec2: Double,
        ef: Double
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val path = "users/$uid/courses/$courseId"
        return try {
            require(courseId.isNotBlank())
            db.collection("users").document(uid).collection("courses").document(courseId)
                .update(
                    mapOf(
                        "gradeEc1" to ec1.coerceIn(0.0, 20.0),
                        "gradeEp" to ep.coerceIn(0.0, 20.0),
                        "gradeEc2" to ec2.coerceIn(0.0, 20.0),
                        "gradeEf" to ef.coerceIn(0.0, 20.0),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun createForumPost(
        course: Course,
        authorName: String,
        title: String,
        content: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val docRef = db.collection("users").document(uid).collection("forum_posts").document()
        return try {
            require(course.id.isNotBlank())
            require(title.trim().length in 2..140) { "Título de publicación muy corto." }
            require(content.trim().length in 2..1000) { "Escribe el contenido de tu participación." }
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "courseId" to course.id,
                    "courseName" to course.name.take(120),
                    "authorName" to authorName.ifBlank { "Estudiante" }.take(100),
                    "authorRole" to "Estudiante",
                    "title" to title.trim(),
                    "content" to content.trim(),
                    "pinned" to false,
                    "likesCount" to 0L,
                    "repliesCount" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun likeForumPost(postId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val path = "users/$uid/forum_posts/$postId"
        return try {
            require(postId.isNotBlank())
            db.collection("users").document(uid).collection("forum_posts").document(postId)
                .update(
                    mapOf(
                        "likesCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun recordAttendance(
        course: Course,
        sessionTitle: String,
        dateLabel: String,
        status: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val userDoc = db.collection("users").document(uid)
        val logRef = userDoc.collection("attendance_logs").document()
        return try {
            require(course.id.isNotBlank())
            require(status in listOf("Presente", "Tardanza", "Falta"))
            require(sessionTitle.trim().length in 2..120)
            require(dateLabel.trim().length in 2..40)

            val batch = db.batch()
            batch.set(
                logRef,
                mapOf(
                    "userId" to uid,
                    "courseId" to course.id,
                    "sessionTitle" to sessionTitle.trim(),
                    "dateLabel" to dateLabel.trim(),
                    "status" to status,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val courseUpdates = mutableMapOf<String, Any>(
                "attendanceTotal" to FieldValue.increment(1L),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (status == "Presente" || status == "Tardanza") {
                courseUpdates["attendancePresent"] = FieldValue.increment(1L)
            }
            batch.update(userDoc.collection("courses").document(course.id), courseUpdates)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, logRef.path)
            Result.failure(e)
        }
    }

    suspend fun payInstallment(paymentId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val path = "users/$uid/payments/$paymentId"
        return try {
            require(paymentId.isNotBlank())
            val code = "BOL-2026-${(10000..99999).random()}"
            db.collection("users").document(uid).collection("payments").document(paymentId)
                .update(
                    mapOf(
                        "status" to "Pagado",
                        "receiptCode" to code,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun createReservation(
        spaceName: String,
        location: String,
        dateLabel: String,
        timeSlot: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val docRef = db.collection("users").document(uid).collection("reservations").document()
        return try {
            require(spaceName.trim().length in 2..120)
            require(location.trim().length in 2..120)
            require(dateLabel.trim().length in 2..40)
            require(timeSlot.trim().length in 2..50)
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "spaceName" to spaceName.trim(),
                    "location" to location.trim(),
                    "dateLabel" to dateLabel.trim(),
                    "timeSlot" to timeSlot.trim(),
                    "status" to "Confirmada",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun cancelReservation(reservationId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val path = "users/$uid/reservations/$reservationId"
        return try {
            require(reservationId.isNotBlank())
            db.collection("users").document(uid).collection("reservations").document(reservationId)
                .update(
                    mapOf(
                        "status" to "Cancelada",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        recipient: String,
        category: String,
        subject: String,
        body: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No autenticado"))
        val docRef = db.collection("users").document(uid).collection("messages").document()
        return try {
            require(recipient.trim().length in 2..100)
            require(category in listOf("Docente", "Trámite", "Soporte"))
            require(subject.trim().length in 2..140)
            require(body.trim().length in 2..1000)
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "recipient" to recipient.trim(),
                    "category" to category,
                    "subject" to subject.trim(),
                    "body" to body.trim(),
                    "status" to "Enviado",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }
}
