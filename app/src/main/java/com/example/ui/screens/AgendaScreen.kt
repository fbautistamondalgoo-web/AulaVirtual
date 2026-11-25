package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Course
import com.example.data.model.CourseTask
import com.example.data.model.UiState
import com.example.ui.components.AcademicEmptyState
import com.example.ui.components.GradePill
import com.example.ui.components.StatusPill
import com.example.ui.components.resolveCourseAccent
import com.example.ui.theme.AutonomaOrange
import com.example.ui.theme.AutonomaOrangeContainer
import com.example.ui.theme.AutonomaOrangeDeep
import com.example.ui.theme.CampusCobalt
import com.example.ui.theme.CampusCobaltContainer
import com.example.ui.theme.CampusEmerald
import com.example.ui.theme.CampusEmeraldContainer
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun AgendaScreen(
    coursesState: UiState<List<Course>>,
    tasksState: UiState<List<CourseTask>>,
    onOpenCourse: (String) -> Unit,
    onSubmitTask: (String, String, String?) -> Unit,
    onCreateTask: (Course, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Horario, 1 = Tareas
    var taskFilter by remember { mutableStateOf("Pendiente") }
    var taskToSubmit by remember { mutableStateOf<CourseTask?>(null) }
    var showCreateTask by remember { mutableStateOf(false) }

    val courses = (coursesState as? UiState.Success)?.data.orEmpty()
    val tasks = (tasksState as? UiState.Success)?.data.orEmpty()

    taskToSubmit?.let { task ->
        TaskSubmitDialog(
            task = task,
            onDismiss = { taskToSubmit = null },
            onSubmit = { note, att ->
                onSubmitTask(task.id, note, att)
                taskToSubmit = null
            }
        )
    }

    if (showCreateTask && courses.isNotEmpty()) {
        AgendaNewTaskDialog(
            courses = courses,
            onDismiss = { showCreateTask = false },
            onConfirm = { course, title, desc, type, due ->
                onCreateTask(course, title, desc, type, due)
                showCreateTask = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Agenda Académica",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (selectedSection == 1 && courses.isNotEmpty()) {
                    OutlinedButton(onClick = { showCreateTask = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Tarea")
                    }
                }
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("Horario Semanal", "Tareas y Exámenes").forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = AutonomaOrangeContainer,
                            activeContentColor = AutonomaOrangeDeep
                        ),
                        modifier = Modifier.testTag("agenda_segment_$index")
                    ) {
                        Text(label)
                    }
                }
            }
        }

        if (selectedSection == 0) {
            WeeklyScheduleList(courses = courses, onOpenCourse = onOpenCourse)
        } else {
            val filtered = remember(tasks, taskFilter) {
                val list = if (taskFilter == "Todas") tasks else tasks.filter { it.status == taskFilter }
                list.sortedBy { it.dueEpochMs }
            }
            Column(modifier = Modifier.fillMaxSize()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("Pendiente", "Entregado", "Calificado", "Todas")) { status ->
                        FilterChip(
                            selected = taskFilter == status,
                            onClick = { taskFilter = status },
                            label = { Text(status) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AutonomaOrangeContainer,
                                selectedLabelColor = AutonomaOrangeDeep
                            )
                        )
                    }
                }
                if (filtered.isEmpty()) {
                    AcademicEmptyState(
                        title = "Sin actividades en $taskFilter",
                        subtitle = "Cambia el filtro o registra una nueva tarea."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.id }) { task ->
                            val (bg, fg) = when (task.status) {
                                "Calificado" -> CampusEmeraldContainer to CampusEmerald
                                "Entregado" -> CampusCobaltContainer to CampusCobalt
                                else -> AutonomaOrangeContainer to AutonomaOrangeDeep
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            StatusPill(task.courseCode, AutonomaOrangeContainer, AutonomaOrangeDeep)
                                            StatusPill(task.status, bg, fg)
                                        }
                                        if (task.score != null) {
                                            GradePill(score = task.score)
                                        }
                                    }
                                    Text(task.title, style = MaterialTheme.typography.titleMedium)
                                    Text(task.courseName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = AutonomaOrange, modifier = Modifier.size(15.dp))
                                        Text(task.dueLabel, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onOpenCourse(task.courseId) },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Ir al curso") }
                                        if (task.status == "Pendiente") {
                                            Button(
                                                onClick = { taskToSubmit = task },
                                                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.size(4.dp))
                                                Text("Entregar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyScheduleList(
    courses: List<Course>,
    onOpenCourse: (String) -> Unit
) {
    val dayNames = mapOf(
        1L to "Lunes",
        2L to "Martes",
        3L to "Miércoles",
        4L to "Jueves",
        5L to "Viernes",
        6L to "Sábado",
        7L to "Domingo"
    )
    val grouped = remember(courses) {
        courses.sortedWith(compareBy({ it.dayOfWeek }, { it.startHour })).groupBy { it.dayOfWeek }
    }

    if (courses.isEmpty()) {
        AcademicEmptyState(
            title = "Horario sin cursos",
            subtitle = "Matricula cursos para visualizar tu horario semanal."
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        grouped.forEach { (day, dayCourses) ->
            item(key = "day_$day") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AutonomaOrange, modifier = Modifier.size(18.dp))
                    Text(
                        text = dayNames[day] ?: "Día $day",
                        style = MaterialTheme.typography.titleMedium,
                        color = AutonomaOrangeDeep
                    )
                }
            }
            items(dayCourses, key = { "${day}_${it.id}" }) { course ->
                val accent = resolveCourseAccent(course.colorHex)
                val endMinutes = course.startHour * 60 + course.startMinute + course.durationMinutes
                val endHour = endMinutes / 60
                val endMin = endMinutes % 60
                val startTime = "%02d:%02d".format(course.startHour, course.startMinute)
                val endTime = "%02d:%02d".format(endHour, endMin)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCourse(course.id) },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accent.copy(alpha = 0.14f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(74.dp)
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = startTime,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = accent
                                )
                                Text(
                                    text = endTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("${course.code} · ${course.name}", style = MaterialTheme.typography.titleSmall)
                            Text(course.professor, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                                Text(course.classroom, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        StatusPill(course.modality, AutonomaOrangeContainer, AutonomaOrangeDeep)
                    }
                }
            }
        }
    }
}

@Composable
private fun AgendaNewTaskDialog(
    courses: List<Course>,
    onDismiss: () -> Unit,
    onConfirm: (Course, String, String, String, String) -> Unit
) {
    var selectedCourse by remember { mutableStateOf(courses.first()) }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Tarea") }
    var due by remember { mutableStateOf("Sáb 05 Dic · 23:59") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Curso:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(courses, key = { it.id }) { c ->
                        FilterChip(
                            selected = selectedCourse.id == c.id,
                            onClick = { selectedCourse = c },
                            label = { Text(c.code) }
                        )
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Tarea", "Examen", "Proyecto").forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                    }
                }
                OutlinedTextField(value = due, onValueChange = { due = it }, label = { Text("Vencimiento") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedCourse, title, desc, type, due) },
                enabled = title.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange)
            ) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
