package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AttendanceLog
import com.example.data.model.Course
import com.example.data.model.CourseModule
import com.example.data.model.CourseTask
import com.example.data.model.ForumPost
import com.example.ui.components.AcademicEmptyState
import com.example.ui.components.GradePill
import com.example.ui.components.StatusPill
import com.example.ui.theme.AutonomaOrange
import com.example.ui.theme.AutonomaOrangeContainer
import com.example.ui.theme.AutonomaOrangeDeep
import com.example.ui.theme.CampusCobalt
import com.example.ui.theme.CampusCobaltContainer
import com.example.ui.theme.CampusCrimson
import com.example.ui.theme.CampusCrimsonContainer
import com.example.ui.theme.CampusEmerald
import com.example.ui.theme.CampusEmeraldContainer
import com.example.ui.theme.CampusOchre
import com.example.ui.theme.CampusOchreContainer
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Locale

@Composable
internal fun SyllabusTab(
    course: Course,
    modules: List<CourseModule>,
    onToggleModule: (String, Boolean) -> Unit,
    onAddModule: (Long, String, String, String, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddModuleDialog(
            nextWeek = (modules.maxOfOrNull { it.weekNumber } ?: 0L) + 1L,
            onDismiss = { showAddDialog = false },
            onConfirm = { week, title, desc, res, type ->
                onAddModule(week, title, desc, res, type)
                showAddDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Sumilla del Curso", style = MaterialTheme.typography.titleSmall, color = AutonomaOrangeDeep)
                    Text(course.syllabusSummary, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Agregar semana")
                        }
                    }
                }
            }
        }

        if (modules.isEmpty()) {
            item {
                AcademicEmptyState(
                    title = "Sin módulos registrados",
                    subtitle = "Agrega las semanas y materiales del curso."
                )
            }
        } else {
            items(modules, key = { it.id }) { mod ->
                val resIcon = when (mod.resourceType) {
                    "Video" -> Icons.Default.PlayCircle
                    "Lectura" -> Icons.Default.MenuBook
                    "Enlace" -> Icons.Default.Link
                    else -> Icons.Default.Description
                }
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleModule(mod.id, !mod.completed) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Checkbox(
                            checked = mod.completed,
                            onCheckedChange = { onToggleModule(mod.id, it) },
                            colors = CheckboxDefaults.colors(checkedColor = AutonomaOrange)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatusPill(
                                    text = "Semana ${mod.weekNumber}",
                                    containerColor = AutonomaOrangeContainer,
                                    contentColor = AutonomaOrangeDeep
                                )
                                StatusPill(
                                    text = mod.resourceType,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(mod.title, style = MaterialTheme.typography.titleSmall)
                            if (mod.description.isNotBlank()) {
                                Text(
                                    mod.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(resIcon, contentDescription = null, tint = AutonomaOrange, modifier = Modifier.size(16.dp))
                                Text(
                                    text = mod.resourceLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AutonomaOrangeDeep
                                )
                            }
                        }
                        Icon(
                            imageVector = if (mod.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (mod.completed) CampusEmerald else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun CourseTasksTab(
    tasks: List<CourseTask>,
    onSubmitTask: (taskId: String, note: String, attachment: String?) -> Unit,
    onCreateTask: (title: String, desc: String, type: String, due: String) -> Unit
) {
    var selectedTaskForSubmit by remember { mutableStateOf<CourseTask?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    selectedTaskForSubmit?.let { task ->
        TaskSubmitDialog(
            task = task,
            onDismiss = { selectedTaskForSubmit = null },
            onSubmit = { note, attachment ->
                onSubmitTask(task.id, note, attachment)
                selectedTaskForSubmit = null
            }
        )
    }

    if (showCreateDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, desc, type, due ->
                onCreateTask(title, desc, type, due)
                showCreateDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Actividades (${tasks.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Nueva tarea")
                }
            }
        }

        if (tasks.isEmpty()) {
            item {
                AcademicEmptyState(
                    title = "Sin tareas asignadas",
                    subtitle = "Crea una actividad o espera nuevas evaluaciones del docente."
                )
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                val (statusBg, statusFg) = when (task.status) {
                    "Calificado" -> CampusEmeraldContainer to CampusEmerald
                    "Entregado" -> CampusCobaltContainer to CampusCobalt
                    else -> AutonomaOrangeContainer to AutonomaOrangeDeep
                }
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusPill(task.taskType, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                                StatusPill(task.status, statusBg, statusFg)
                            }
                            if (task.score != null) {
                                GradePill(score = task.score)
                            }
                        }
                        Text(task.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Vencimiento: ${task.dueLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!task.submissionNote.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Tu entrega:", style = MaterialTheme.typography.labelSmall)
                                    Text(task.submissionNote, style = MaterialTheme.typography.bodySmall)
                                    if (!task.attachmentName.isNullOrBlank()) {
                                        Text(
                                            "Adjunto: ${task.attachmentName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AutonomaOrangeDeep
                                        )
                                    }
                                }
                            }
                        }
                        if (task.status == "Pendiente") {
                            Button(
                                onClick = { selectedTaskForSubmit = task },
                                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_task_btn_${task.id}")
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text("Entregar Actividad")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun GradesTab(
    course: Course,
    onUpdateGrades: (Double, Double, Double, Double) -> Unit
) {
    var showEditGrades by remember { mutableStateOf(false) }

    if (showEditGrades) {
        EditGradesDialog(
            course = course,
            onDismiss = { showEditGrades = false },
            onSave = { ec1, ep, ec2, ef ->
                onUpdateGrades(ec1, ep, ec2, ef)
                showEditGrades = false
            }
        )
    }

    val finalWeighted = course.weightedAverage()
    val partialAvg = course.currentPartialAverage()
    val isPassing = partialAvg >= 10.5

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, AutonomaOrange.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Promedio Ponderado Actual", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (partialAvg > 0) String.format(Locale.US, "%.2f / 20", partialAvg) else "Sin notas",
                                style = MaterialTheme.typography.headlineMedium,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = if (isPassing) CampusEmerald else AutonomaOrangeDeep
                            )
                        }
                        StatusPill(
                            text = if (isPassing) "Aprobado" else "En progreso",
                            containerColor = if (isPassing) CampusEmeraldContainer else AutonomaOrangeContainer,
                            contentColor = if (isPassing) CampusEmerald else AutonomaOrangeDeep
                        )
                    }
                    Text(
                        text = "Proyección acumulada del ciclo: ${String.format(Locale.US, "%.2f", finalWeighted)} / 20.00",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { showEditGrades = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Registrar o Simular Notas")
                    }
                }
            }
        }

        val items = listOf(
            Triple("Evaluación Continua 1 (EC1)", "20%", course.gradeEc1),
            Triple("Examen Parcial (EP)", "25%", course.gradeEp),
            Triple("Evaluación Continua 2 (EC2)", "25%", course.gradeEc2),
            Triple("Examen Final (EF)", "30%", course.gradeEf)
        )
        items(items) { (name, weight, score) ->
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, style = MaterialTheme.typography.titleSmall)
                        Text("Peso oficial: $weight", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    GradePill(score = score)
                }
            }
        }
    }
}

@Composable
internal fun ForumTab(
    posts: List<ForumPost>,
    onCreatePost: (String, String) -> Unit,
    onLikePost: (String) -> Unit
) {
    var showNewPost by remember { mutableStateOf(false) }

    if (showNewPost) {
        NewForumPostDialog(
            onDismiss = { showNewPost = false },
            onConfirm = { title, content ->
                onCreatePost(title, content)
                showNewPost = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = { showNewPost = true },
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Publicar Consulta en el Foro")
            }
        }

        if (posts.isEmpty()) {
            item {
                AcademicEmptyState(
                    title = "Foro vacío",
                    subtitle = "Inicia un hilo de consulta o debate con tu docente."
                )
            }
        } else {
            items(posts, key = { it.id }) { post ->
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        if (post.pinned) AutonomaOrange.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                StatusPill(
                                    text = post.authorRole,
                                    containerColor = if (post.authorRole == "Docente") AutonomaOrangeContainer else CampusCobaltContainer,
                                    contentColor = if (post.authorRole == "Docente") AutonomaOrangeDeep else CampusCobalt
                                )
                                Text(post.authorName, style = MaterialTheme.typography.labelMedium)
                            }
                            if (post.pinned) {
                                Icon(Icons.Default.PushPin, contentDescription = "Fijado", tint = AutonomaOrange, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(post.title, style = MaterialTheme.typography.titleSmall)
                        Text(post.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onLikePost(post.id) }) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "Me gusta", tint = AutonomaOrange)
                            }
                            Text("${post.likesCount}", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AttendanceTab(
    course: Course,
    logs: List<AttendanceLog>,
    onRecordAttendance: (String, String, String) -> Unit
) {
    val pct = course.attendancePercent()
    val safePct = pct >= 70

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Asistencia Acumulada", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$pct% (${course.attendancePresent}/${course.attendanceTotal} sesiones)",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (safePct) CampusEmerald else CampusCrimson
                            )
                        }
                        StatusPill(
                            text = if (safePct) "Habilitado" else "Riesgo DPI",
                            containerColor = if (safePct) CampusEmeraldContainer else CampusCrimsonContainer,
                            contentColor = if (safePct) CampusEmerald else CampusCrimson
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (pct / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (safePct) CampusEmerald else CampusCrimson,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "Mínimo requerido por reglamento: 70% de asistencia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            onRecordAttendance(
                                "Sesión ${course.attendanceTotal + 1} · Registro en Aula Virtual",
                                "Hoy · 25 Nov 2026",
                                "Presente"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Marcar Asistencia de Hoy")
                    }
                }
            }
        }

        items(logs, key = { it.id }) { log ->
            val (bg, fg) = when (log.status) {
                "Presente" -> CampusEmeraldContainer to CampusEmerald
                "Tardanza" -> CampusOchreContainer to CampusOchre
                else -> CampusCrimsonContainer to CampusCrimson
            }
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(log.sessionTitle, style = MaterialTheme.typography.titleSmall)
                        Text(log.dateLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusPill(log.status, bg, fg)
                }
            }
        }
    }
}

@Composable
fun TaskSubmitDialog(
    task: CourseTask,
    onDismiss: () -> Unit,
    onSubmit: (note: String, attachment: String?) -> Unit
) {
    var note by remember { mutableStateOf("") }
    var attachmentName by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachmentName = uri.lastPathSegment?.take(80) ?: "evidencia_entrega.jpg"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Entregar: ${task.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "${task.courseCode} · ${task.dueLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AutonomaOrangeDeep
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Comentario o enlace de entrega") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_submission_note")
                )
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(attachmentName ?: "Adjuntar imagen de evidencia")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(note, attachmentName) },
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange),
                modifier = Modifier.testTag("confirm_submit_task_button")
            ) {
                Text("Enviar Entrega")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, type: String, due: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Tarea") }
    var due by remember { mutableStateOf("Vie 04 Dic · 23:59") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Actividad") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la tarea o examen") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Tarea", "Examen", "Proyecto", "Foro").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t) }
                        )
                    }
                }
                OutlinedTextField(
                    value = due,
                    onValueChange = { due = it },
                    label = { Text("Fecha límite") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, desc, type, due) },
                enabled = title.trim().length >= 2 && due.trim().isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun AddModuleDialog(
    nextWeek: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("Semana $nextWeek: Sesión Académica") }
    var desc by remember { mutableStateOf("") }
    var resource by remember { mutableStateOf("Guia_Semana_$nextWeek.pdf") }
    var resType by remember { mutableStateOf("PDF") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Módulo (Semana $nextWeek)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = resource, onValueChange = { resource = it }, label = { Text("Archivo / Enlace") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("PDF", "Video", "Lectura", "Enlace").forEach { t ->
                        FilterChip(selected = resType == t, onClick = { resType = t }, label = { Text(t) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nextWeek.coerceIn(1L, 20L), title, desc, resource, resType) },
                enabled = title.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange)
            ) { Text("Agregar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun EditGradesDialog(
    course: Course,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Double, Double) -> Unit
) {
    var ec1 by remember { mutableStateOf(course.gradeEc1.toString()) }
    var ep by remember { mutableStateOf(course.gradeEp.toString()) }
    var ec2 by remember { mutableStateOf(course.gradeEc2.toString()) }
    var ef by remember { mutableStateOf(course.gradeEf.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notas de ${course.code}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = ec1, onValueChange = { ec1 = it }, label = { Text("EC1 (20%) · 0 - 20") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ep, onValueChange = { ep = it }, label = { Text("Examen Parcial (25%) · 0 - 20") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ec2, onValueChange = { ec2 = it }, label = { Text("EC2 (25%) · 0 - 20") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ef, onValueChange = { ef = it }, label = { Text("Examen Final (30%) · 0 - 20") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ec1.toDoubleOrNull()?.coerceIn(0.0, 20.0) ?: 0.0,
                        ep.toDoubleOrNull()?.coerceIn(0.0, 20.0) ?: 0.0,
                        ec2.toDoubleOrNull()?.coerceIn(0.0, 20.0) ?: 0.0,
                        ef.toDoubleOrNull()?.coerceIn(0.0, 20.0) ?: 0.0
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange)
            ) { Text("Guardar Notas") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun NewForumPostDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Publicación en Foro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Asunto / Título") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Mensaje o consulta") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, content) },
                enabled = title.trim().length >= 2 && content.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange)
            ) { Text("Publicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
