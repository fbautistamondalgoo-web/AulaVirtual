package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Course
import com.example.data.model.UiState
import com.example.ui.components.AcademicEmptyState
import com.example.ui.components.CourseSummaryCard
import com.example.ui.theme.AutonomaOrange
import com.example.ui.theme.AutonomaOrangeContainer
import com.example.ui.theme.AutonomaOrangeDeep

@Composable
fun CoursesScreen(
    coursesState: UiState<List<Course>>,
    onOpenCourse: (String) -> Unit,
    onEnrollCourse: (
        code: String,
        name: String,
        professor: String,
        credits: Long,
        modality: String,
        classroom: String,
        dayOfWeek: Long,
        startHour: Long,
        scheduleSummary: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedModality by remember { mutableStateOf("Todos") }
    var showEnrollDialog by remember { mutableStateOf(false) }

    val courses = (coursesState as? UiState.Success)?.data.orEmpty()
    val filteredCourses = remember(courses, searchQuery, selectedModality) {
        courses.filter { course ->
            val matchesModality = selectedModality == "Todos" || course.modality == selectedModality
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                course.name.lowercase().contains(q) ||
                course.code.lowercase().contains(q) ||
                course.professor.lowercase().contains(q)
            matchesModality && matchesQuery
        }
    }

    if (showEnrollDialog) {
        EnrollCourseDialog(
            onDismiss = { showEnrollDialog = false },
            onConfirm = { code, name, prof, credits, mod, room, day, hour, sched ->
                onEnrollCourse(code, name, prof, credits, mod, room, day, hour, sched)
                showEnrollDialog = false
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showEnrollDialog = true },
                containerColor = AutonomaOrange,
                contentColor = Color.White,
                modifier = Modifier.testTag("enroll_course_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Matricular nuevo curso")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Mis Cursos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por código, curso o docente") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("courses_search_input")
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Todos", "Presencial", "Híbrido", "Remoto")) { mod ->
                        FilterChip(
                            selected = selectedModality == mod,
                            onClick = { selectedModality = mod },
                            label = { Text(mod) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AutonomaOrangeContainer,
                                selectedLabelColor = AutonomaOrangeDeep
                            )
                        )
                    }
                }
            }

            when (coursesState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AutonomaOrange)
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(coursesState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    if (filteredCourses.isEmpty()) {
                        AcademicEmptyState(
                            title = "No encontramos cursos",
                            subtitle = "Prueba otro filtro o matricula un nuevo curso con el botón +."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(filteredCourses, key = { it.id }) { course ->
                                CourseSummaryCard(
                                    course = course,
                                    onClick = { onOpenCourse(course.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnrollCourseDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        code: String,
        name: String,
        professor: String,
        credits: Long,
        modality: String,
        classroom: String,
        dayOfWeek: Long,
        startHour: Long,
        scheduleSummary: String
    ) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var professor by remember { mutableStateOf("") }
    var creditsText by remember { mutableStateOf("3") }
    var modality by remember { mutableStateOf("Híbrido") }
    var classroom by remember { mutableStateOf("Pabellón A · Aula 202") }
    var dayOfWeek by remember { mutableLongStateOf(4L) }
    var startHour by remember { mutableLongStateOf(14L) }
    var scheduleSummary by remember { mutableStateOf("Jue · 14:00 - 16:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Matricular Curso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código (ej. IS-605)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del curso") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = professor,
                    onValueChange = { professor = it },
                    label = { Text("Docente") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = creditsText,
                        onValueChange = { creditsText = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("Créditos") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = classroom,
                        onValueChange = { classroom = it },
                        label = { Text("Aula / Sala") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Presencial", "Híbrido", "Remoto").forEach { mod ->
                        FilterChip(
                            selected = modality == mod,
                            onClick = { modality = mod },
                            label = { Text(mod) }
                        )
                    }
                }
                OutlinedTextField(
                    value = scheduleSummary,
                    onValueChange = { scheduleSummary = it },
                    label = { Text("Horario resumen") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cr = creditsText.toLongOrNull()?.coerceIn(1L, 10L) ?: 3L
                    onConfirm(
                        code.ifBlank { "IS-608" },
                        name.ifBlank { "Curso Electivo" },
                        professor.ifBlank { "Mg. Docente Asignado" },
                        cr,
                        modality,
                        classroom,
                        dayOfWeek,
                        startHour,
                        scheduleSummary
                    )
                },
                enabled = code.trim().length >= 2 && name.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange)
            ) {
                Text("Matricular")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
