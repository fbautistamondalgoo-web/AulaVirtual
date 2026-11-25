package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Course
import com.example.data.model.CourseTask
import com.example.data.model.ForumPost
import com.example.data.model.StudentProfile
import com.example.data.model.TuitionPayment
import com.example.data.model.UiState
import com.example.ui.components.CarnetDigitalCard
import com.example.ui.components.CourseSummaryCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.AutonomaOrange
import com.example.ui.theme.AutonomaOrangeContainer
import com.example.ui.theme.AutonomaOrangeDark
import com.example.ui.theme.AutonomaOrangeDeep
import com.example.ui.theme.CampusCobalt
import com.example.ui.theme.CampusCobaltContainer
import com.example.ui.theme.CampusEmerald
import com.example.ui.theme.CampusEmeraldContainer
import com.example.ui.theme.CampusOchre
import com.example.ui.theme.CampusOchreContainer
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Locale

@Composable
fun HomeScreen(
    profileState: UiState<StudentProfile?>,
    coursesState: UiState<List<Course>>,
    tasksState: UiState<List<CourseTask>>,
    postsState: UiState<List<ForumPost>>,
    paymentsState: UiState<List<TuitionPayment>>,
    onOpenCourse: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onOpenServiceSection: (Int) -> Unit,
    onRetrySeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCarnetModal by remember { mutableStateOf(false) }

    val profile = (profileState as? UiState.Success)?.data
    val courses = (coursesState as? UiState.Success)?.data.orEmpty()
    val tasks = (tasksState as? UiState.Success)?.data.orEmpty()
    val posts = (postsState as? UiState.Success)?.data.orEmpty()
    val payments = (paymentsState as? UiState.Success)?.data.orEmpty()

    val pendingTasks = remember(tasks) { tasks.filter { it.status == "Pendiente" } }
    val pendingPayments = remember(payments) { payments.filter { it.status != "Pagado" } }
    val pinnedPosts = remember(posts) { posts.filter { it.pinned } }

    val avgGpa = remember(courses) {
        val graded = courses.map { it.currentPartialAverage() }.filter { it > 0.0 }
        if (graded.isEmpty()) 0.0 else graded.average()
    }
    val avgAttendance = remember(courses) {
        if (courses.isEmpty()) 100 else courses.map { it.attendancePercent() }.average().toInt()
    }

    if (showCarnetModal && profile != null) {
        Dialog(onDismissRequest = { showCarnetModal = false }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CarnetDigitalCard(profile = profile)
                Button(
                    onClick = { showCarnetModal = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange)
                ) {
                    Text("Cerrar Carnet")
                }
            }
        }
    }

    if (coursesState is UiState.Loading && courses.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = AutonomaOrange)
                Text("Sincronizando Campus Virtual...", style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    if (coursesState is UiState.Error) {
        Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = coursesState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = onRetrySeed,
                    colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange)
                ) {
                    Text("Reintentar conexión")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header institucional Naranja Autónoma
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AutonomaOrangeDark, AutonomaOrange)
                        )
                    )
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = size.height * 0.65f,
                        center = Offset(size.width * 0.92f, size.height * 0.15f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = size.height * 0.4f,
                        center = Offset(size.width * 0.12f, size.height * 0.95f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile?.cycle ?: "Ciclo 2026-II",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "Hola, ${profile?.fullName?.substringBefore(" ") ?: "Estudiante"}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = "${profile?.career ?: "Pregrado"} · ${profile?.studentCode ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { if (profile != null) showCarnetModal = true }
                                .testTag("home_carnet_button"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = "Abrir Carnet Digital",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Carnet",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Indicadores académicos rápidos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HeaderStatBox(
                            label = "Ponderado",
                            value = if (avgGpa > 0) String.format(Locale.US, "%.1f", avgGpa) else "--",
                            modifier = Modifier.weight(1f)
                        )
                        HeaderStatBox(
                            label = "Asistencia",
                            value = "$avgAttendance%",
                            modifier = Modifier.weight(1f)
                        )
                        HeaderStatBox(
                            label = "Pendientes",
                            value = "${pendingTasks.size}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Accesos rápidos del Campus Virtual
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Accesos del Campus",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        icon = Icons.Default.CalendarMonth,
                        label = "Horario",
                        tint = AutonomaOrangeDeep,
                        bg = AutonomaOrangeContainer,
                        onClick = { onNavigateToTab(2) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        icon = Icons.Default.Payments,
                        label = "Pagos (${pendingPayments.size})",
                        tint = CampusEmerald,
                        bg = CampusEmeraldContainer,
                        onClick = { onOpenServiceSection(1) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        icon = Icons.Default.EventAvailable,
                        label = "Reservas",
                        tint = CampusCobalt,
                        bg = CampusCobaltContainer,
                        onClick = { onOpenServiceSection(2) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        icon = Icons.Default.Mail,
                        label = "Trámites",
                        tint = CampusOchre,
                        bg = CampusOchreContainer,
                        onClick = { onOpenServiceSection(3) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Próxima clase / Clase destacada
        courses.firstOrNull()?.let { nextCourse ->
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Siguiente Sesión Académica",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenCourse(nextCourse.id) },
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, AutonomaOrange.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = AutonomaOrangeContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Class,
                                    contentDescription = null,
                                    tint = AutonomaOrangeDeep,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .size(26.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${nextCourse.code} · ${nextCourse.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${nextCourse.scheduleSummary} · ${nextCourse.classroom}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            StatusPill(
                                text = nextCourse.modality,
                                containerColor = AutonomaOrangeContainer,
                                contentColor = AutonomaOrangeDeep
                            )
                        }
                    }
                }
            }
        }

        // Tareas próximas
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Entregas y Evaluaciones Próximas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { onNavigateToTab(2) }) {
                        Text("Ver todo", color = AutonomaOrangeDeep)
                    }
                }

                if (pendingTasks.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = CampusEmeraldContainer)
                    ) {
                        Text(
                            text = "Estás al día: no tienes entregas pendientes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CampusEmerald,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pendingTasks, key = { it.id }) { task ->
                            Card(
                                modifier = Modifier
                                    .width(265.dp)
                                    .clickable { onOpenCourse(task.courseId) },
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StatusPill(
                                            text = "${task.courseCode} · ${task.taskType}",
                                            containerColor = AutonomaOrangeContainer,
                                            contentColor = AutonomaOrangeDeep
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                                            contentDescription = null,
                                            tint = AutonomaOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = task.dueLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Anuncios recientes de docentes
        if (pinnedPosts.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Anuncios de Docentes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    pinnedPosts.take(2).forEach { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenCourse(post.courseId) },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = AutonomaOrangeContainer
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = AutonomaOrangeDeep,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = post.courseName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AutonomaOrangeDeep
                                    )
                                    Text(
                                        text = post.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = post.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mis cursos activos
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mis Cursos (${courses.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { onNavigateToTab(1) }) {
                    Text("Administrar", color = AutonomaOrangeDeep)
                }
            }
        }

        items(courses.take(3), key = { it.id }) { course ->
            CourseSummaryCard(
                course = course,
                onClick = { onOpenCourse(course.id) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun HeaderStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    tint: Color,
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(shape = CircleShape, color = bg) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
