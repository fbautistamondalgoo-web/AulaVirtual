package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Keep
@IgnoreExtraProperties
data class StudentProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val studentCode: String = "",
    val faculty: String = "",
    val career: String = "",
    val cycle: String = "",
    val campus: String = "",
    val modality: String = "Semipresencial",
    val phone: String = "",
    val photoUrl: String = "",
    val seeded: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Course(
    @DocumentId val id: String = "",
    val userId: String = "",
    val code: String = "",
    val name: String = "",
    val professor: String = "",
    val credits: Long = 3L,
    val section: String = "A1",
    val modality: String = "Presencial",
    val classroom: String = "",
    val scheduleSummary: String = "",
    val dayOfWeek: Long = 1L, // 1=Lunes .. 7=Domingo
    val startHour: Long = 8L,
    val startMinute: Long = 0L,
    val durationMinutes: Long = 120L,
    val colorHex: String = "#FF8F1C",
    val syllabusSummary: String = "",
    val progressPercent: Long = 0L,
    val gradeEc1: Double = 0.0,
    val gradeEp: Double = 0.0,
    val gradeEc2: Double = 0.0,
    val gradeEf: Double = 0.0,
    val attendancePresent: Long = 0L,
    val attendanceTotal: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    /**
     * Ponderado estándar universitario (EC1 20% + EP 25% + EC2 25% + EF 30%).
     */
    fun weightedAverage(): Double {
        return (gradeEc1 * 0.20) + (gradeEp * 0.25) + (gradeEc2 * 0.25) + (gradeEf * 0.30)
    }

    fun currentPartialAverage(): Double {
        var sum = 0.0
        var weight = 0.0
        if (gradeEc1 > 0.0) { sum += gradeEc1 * 0.20; weight += 0.20 }
        if (gradeEp > 0.0) { sum += gradeEp * 0.25; weight += 0.25 }
        if (gradeEc2 > 0.0) { sum += gradeEc2 * 0.25; weight += 0.25 }
        if (gradeEf > 0.0) { sum += gradeEf * 0.30; weight += 0.30 }
        return if (weight > 0.0) sum / weight else 0.0
    }

    fun attendancePercent(): Int {
        if (attendanceTotal <= 0L) return 100
        return ((attendancePresent.toDouble() / attendanceTotal.toDouble()) * 100.0).toInt().coerceIn(0, 100)
    }
}

@Keep
@IgnoreExtraProperties
data class CourseModule(
    @DocumentId val id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val weekNumber: Long = 1L,
    val title: String = "",
    val description: String = "",
    val resourceLabel: String = "",
    val resourceType: String = "PDF",
    val completed: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CourseTask(
    @DocumentId val id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val courseCode: String = "",
    val courseName: String = "",
    val title: String = "",
    val description: String = "",
    val taskType: String = "Tarea",
    val dueLabel: String = "",
    val dueEpochMs: Long = 0L,
    val status: String = "Pendiente",
    val submissionNote: String? = null,
    val attachmentName: String? = null,
    val score: Double? = null,
    val maxScore: Double = 20.0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ForumPost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val authorName: String = "",
    val authorRole: String = "Estudiante",
    val title: String = "",
    val content: String = "",
    val pinned: Boolean = false,
    val likesCount: Long = 0L,
    val repliesCount: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AttendanceLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val sessionTitle: String = "",
    val dateLabel: String = "",
    val status: String = "Presente",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TuitionPayment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val concept: String = "",
    val period: String = "",
    val dueDateLabel: String = "",
    val amountSoles: Double = 0.0,
    val status: String = "Pendiente",
    val receiptCode: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SpaceReservation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val spaceName: String = "",
    val location: String = "",
    val dateLabel: String = "",
    val timeSlot: String = "",
    val status: String = "Confirmada",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CampusMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val recipient: String = "",
    val category: String = "Docente",
    val subject: String = "",
    val body: String = "",
    val status: String = "Enviado",
    val replyText: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
