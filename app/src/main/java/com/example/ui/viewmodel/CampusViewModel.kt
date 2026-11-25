package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.CampusRepository
import com.example.data.model.AttendanceLog
import com.example.data.model.CampusMessage
import com.example.data.model.Course
import com.example.data.model.CourseModule
import com.example.data.model.CourseTask
import com.example.data.model.ForumPost
import com.example.data.model.SpaceReservation
import com.example.data.model.StudentProfile
import com.example.data.model.TuitionPayment
import com.example.data.model.UiState
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CampusViewModel(
    private val repository: CampusRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedCourseId = MutableStateFlow<String?>(null)
    val selectedCourseId: StateFlow<String?> = _selectedCourseId.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    private val _isActionBusy = MutableStateFlow(false)
    val isActionBusy: StateFlow<Boolean> = _isActionBusy.asStateFlow()

    val profileState: StateFlow<UiState<StudentProfile?>> =
        repository.observeProfile(currentUserId).asUiState("perfil")

    val coursesState: StateFlow<UiState<List<Course>>> =
        repository.observeCourses(currentUserId).asUiState("cursos")

    val modulesState: StateFlow<UiState<List<CourseModule>>> =
        repository.observeAllModules(currentUserId).asUiState("módulos")

    val tasksState: StateFlow<UiState<List<CourseTask>>> =
        repository.observeTasks(currentUserId).asUiState("tareas")

    val forumPostsState: StateFlow<UiState<List<ForumPost>>> =
        repository.observeForumPosts(currentUserId).asUiState("foro")

    val attendanceLogsState: StateFlow<UiState<List<AttendanceLog>>> =
        repository.observeAttendanceLogs(currentUserId).asUiState("asistencia")

    val paymentsState: StateFlow<UiState<List<TuitionPayment>>> =
        repository.observePayments(currentUserId).asUiState("pagos")

    val reservationsState: StateFlow<UiState<List<SpaceReservation>>> =
        repository.observeReservations(currentUserId).asUiState("reservas")

    val messagesState: StateFlow<UiState<List<CampusMessage>>> =
        repository.observeMessages(currentUserId).asUiState("mensajes")

    init {
        ensureSeeded()
    }

    fun ensureSeeded() {
        viewModelScope.launch {
            repository.ensureStudentSeeded().onFailure { error ->
                Log.w(TAG, "Seed check warning: ${error.message}")
            }
        }
    }

    fun openCourse(courseId: String) {
        _selectedCourseId.value = courseId
    }

    fun closeCourse() {
        _selectedCourseId.value = null
    }

    fun updateProfile(
        fullName: String,
        faculty: String,
        career: String,
        cycle: String,
        campus: String,
        modality: String,
        phone: String
    ) = launchAction("Perfil actualizado en el Campus") {
        repository.updateProfile(fullName, faculty, career, cycle, campus, modality, phone)
    }

    fun enrollCourse(
        code: String,
        name: String,
        professor: String,
        credits: Long,
        modality: String,
        classroom: String,
        dayOfWeek: Long,
        startHour: Long,
        scheduleSummary: String
    ) = launchAction("Curso matriculado correctamente") {
        repository.enrollCourse(code, name, professor, credits, modality, classroom, dayOfWeek, startHour, scheduleSummary)
    }

    fun toggleModuleCompleted(moduleId: String, completed: Boolean) = launchAction(
        if (completed) "Módulo marcado como completado" else "Módulo marcado como pendiente"
    ) {
        repository.toggleModuleCompleted(moduleId, completed)
    }

    fun addModule(
        courseId: String,
        weekNumber: Long,
        title: String,
        description: String,
        resourceLabel: String,
        resourceType: String
    ) = launchAction("Módulo agregado al sílabo") {
        repository.addModule(courseId, weekNumber, title, description, resourceLabel, resourceType)
    }

    fun createTask(
        course: Course,
        title: String,
        description: String,
        taskType: String,
        dueLabel: String
    ) = launchAction("Actividad académica registrada") {
        repository.createTask(course, title, description, taskType, dueLabel)
    }

    fun submitTask(
        taskId: String,
        submissionNote: String,
        attachmentName: String?
    ) = launchAction("Entrega enviada al Aula Virtual") {
        repository.submitTask(taskId, submissionNote, attachmentName)
    }

    fun updateCourseGrades(
        courseId: String,
        ec1: Double,
        ep: Double,
        ec2: Double,
        ef: Double
    ) = launchAction("Notas actualizadas") {
        repository.updateCourseGrades(courseId, ec1, ep, ec2, ef)
    }

    fun createForumPost(
        course: Course,
        authorName: String,
        title: String,
        content: String
    ) = launchAction("Participación publicada en el foro") {
        repository.createForumPost(course, authorName, title, content)
    }

    fun likeForumPost(postId: String) {
        viewModelScope.launch {
            repository.likeForumPost(postId).onFailure { e ->
                _snackbarEvents.tryEmit(e.localizedMessage ?: "No se pudo reaccionar")
            }
        }
    }

    fun recordAttendance(
        course: Course,
        sessionTitle: String,
        dateLabel: String,
        status: String
    ) = launchAction("Asistencia registrada: $status") {
        repository.recordAttendance(course, sessionTitle, dateLabel, status)
    }

    fun payInstallment(paymentId: String) = launchAction("Pago registrado · Boleta generada") {
        repository.payInstallment(paymentId)
    }

    fun createReservation(
        spaceName: String,
        location: String,
        dateLabel: String,
        timeSlot: String
    ) = launchAction("Reserva confirmada en el campus") {
        repository.createReservation(spaceName, location, dateLabel, timeSlot)
    }

    fun cancelReservation(reservationId: String) = launchAction("Reserva cancelada") {
        repository.cancelReservation(reservationId)
    }

    fun sendMessage(
        recipient: String,
        category: String,
        subject: String,
        body: String
    ) = launchAction("Mensaje enviado correctamente") {
        repository.sendMessage(recipient, category, subject, body)
    }

    private fun launchAction(successMessage: String, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _isActionBusy.value = true
            val result = block()
            _isActionBusy.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit(successMessage)
            }.onFailure { error ->
                _snackbarEvents.tryEmit(error.localizedMessage ?: "Ocurrió un error al guardar en Firebase")
            }
        }
    }

    private fun <T> Flow<T>.asUiState(label: String): StateFlow<UiState<T>> {
        return this
            .map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observando $label", rootError)
                emit(UiState.Error(rootError.localizedMessage ?: "No se pudo cargar $label"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )
    }

    private companion object {
        const val TAG = "CampusVM"
    }
}
