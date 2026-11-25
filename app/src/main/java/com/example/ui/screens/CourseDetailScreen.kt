package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AttendanceLog
import com.example.data.model.Course
import com.example.data.model.CourseModule
import com.example.data.model.CourseTask
import com.example.data.model.ForumPost
import com.example.ui.components.resolveCourseAccent
import com.example.ui.theme.AutonomaOrange
import com.example.ui.theme.AutonomaOrangeDark

@Composable
fun CourseDetailScreen(
    course: Course,
    studentName: String,
    modules: List<CourseModule>,
    tasks: List<CourseTask>,
    forumPosts: List<ForumPost>,
    attendanceLogs: List<AttendanceLog>,
    onBack: () -> Unit,
    onToggleModule: (String, Boolean) -> Unit,
    onAddModule: (Long, String, String, String, String) -> Unit,
    onSubmitTask: (String, String, String?) -> Unit,
    onCreateTask: (String, String, String, String) -> Unit,
    onUpdateGrades: (Double, Double, Double, Double) -> Unit,
    onCreatePost: (String, String) -> Unit,
    onLikePost: (String) -> Unit,
    onRecordAttendance: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Sílabo", "Tareas", "Notas", "Foro", "Asistencia")
    val accent = resolveCourseAccent(course.colorHex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header del aula del curso
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(AutonomaOrangeDark, accent)
                    )
                )
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("course_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver a cursos",
                        tint = Color.White
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${course.code} · Sección ${course.section} · ${course.modality}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = "${course.professor} · ${course.classroom}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = AutonomaOrange,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AutonomaOrange
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == index) AutonomaOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> SyllabusTab(
                course = course,
                modules = modules.sortedBy { it.weekNumber },
                onToggleModule = onToggleModule,
                onAddModule = onAddModule
            )
            1 -> CourseTasksTab(
                tasks = tasks.sortedBy { it.dueEpochMs },
                onSubmitTask = onSubmitTask,
                onCreateTask = onCreateTask
            )
            2 -> GradesTab(
                course = course,
                onUpdateGrades = onUpdateGrades
            )
            3 -> ForumTab(
                posts = forumPosts.sortedByDescending { it.pinned },
                onCreatePost = onCreatePost,
                onLikePost = onLikePost
            )
            4 -> AttendanceTab(
                course = course,
                logs = attendanceLogs,
                onRecordAttendance = onRecordAttendance
            )
        }
    }
}
