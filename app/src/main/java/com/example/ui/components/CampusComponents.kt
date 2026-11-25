package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Course
import com.example.data.model.StudentProfile
import com.example.ui.theme.AutonomaOrange
import com.example.ui.theme.AutonomaOrangeContainer
import com.example.ui.theme.AutonomaOrangeDark
import com.example.ui.theme.AutonomaOrangeDeep
import com.example.ui.theme.CampusCobalt
import com.example.ui.theme.CampusCobaltContainer
import com.example.ui.theme.CampusCrimson
import com.example.ui.theme.CampusCrimsonContainer
import com.example.ui.theme.CampusEmerald
import com.example.ui.theme.CampusEmeraldContainer
import com.example.ui.theme.CampusOchre
import com.example.ui.theme.CampusOchreContainer
import com.example.ui.theme.CampusSlate
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Locale

fun resolveCourseAccent(colorHex: String): Color {
    return when (colorHex.uppercase()) {
        "#FF8F1C" -> AutonomaOrange
        "#D96B00" -> AutonomaOrangeDark
        "#1565C0" -> CampusCobalt
        "#18864B" -> CampusEmerald
        "#C88A00" -> CampusOchre
        else -> AutonomaOrange
    }
}

@Composable
fun StatusPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = containerColor
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun GradePill(score: Double, modifier: Modifier = Modifier) {
    val (bg, fg) = when {
        score <= 0.0 -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        score >= 14.0 -> CampusEmeraldContainer to CampusEmerald
        score >= 11.0 -> CampusOchreContainer to CampusOchre
        else -> CampusCrimsonContainer to CampusCrimson
    }
    val label = if (score <= 0.0) "--" else String.format(Locale.US, "%.1f", score)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bg
    ) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun CourseSummaryCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = resolveCourseAccent(course.colorHex)
    val partialAvg = course.currentPartialAverage()
    val modalityBg = when (course.modality) {
        "Presencial" -> CampusEmeraldContainer
        "Remoto" -> CampusCobaltContainer
        else -> AutonomaOrangeContainer
    }
    val modalityFg = when (course.modality) {
        "Presencial" -> CampusEmerald
        "Remoto" -> CampusCobalt
        else -> AutonomaOrangeDeep
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("course_card_${course.code}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(accent)
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accent.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = course.code,
                                color = accent,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        StatusPill(
                            text = course.modality,
                            containerColor = modalityBg,
                            contentColor = modalityFg
                        )
                    }
                    GradePill(score = partialAvg)
                }

                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = course.professor,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = course.scheduleSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${course.credits} cr.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = course.classroom,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Avance del sílabo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${course.progressPercent}% · Asist. ${course.attendancePercent()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (course.progressPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50)),
                        color = accent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Entrar al aula",
                        style = MaterialTheme.typography.labelLarge,
                        color = accent
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CarnetDigitalCard(
    profile: StudentProfile,
    modifier: Modifier = Modifier
) {
    val qrSeed = remember(profile.studentCode, profile.userId) {
        "${profile.studentCode}|${profile.userId}".hashCode()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AutonomaOrangeDark,
                            AutonomaOrange,
                            Color(0xFFFFA94D)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.22f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "CAMPUS VIRTUAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                            Text(
                                text = "CARNET UNIVERSITARIO",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CampusEmeraldContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = CampusEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "ACTIVO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CampusEmerald
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (profile.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = profile.photoUrl,
                            contentDescription = "Foto del estudiante",
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.img_student_avatar),
                            error = painterResource(R.drawable.img_student_avatar),
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(2.dp, Color.White, RoundedCornerShape(18.dp))
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.img_student_avatar),
                            contentDescription = "Avatar del estudiante",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(2.dp, Color.White, RoundedCornerShape(18.dp))
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = profile.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = profile.career,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.94f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Código: ${profile.studentCode}",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = profile.faculty,
                                style = MaterialTheme.typography.labelMedium,
                                color = CampusSlate,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${profile.cycle} · ${profile.modality}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5A626D)
                            )
                            Text(
                                text = profile.campus,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5A626D),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        DeterministicQrMatrix(
                            seed = qrSeed,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeterministicQrMatrix(seed: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val grid = 9
        val cellW = size.width / grid
        val cellH = size.height / grid
        var state = seed
        for (r in 0 until grid) {
            for (c in 0 until grid) {
                val isFinder = (r < 3 && c < 3) || (r < 3 && c >= grid - 3) || (r >= grid - 3 && c < 3)
                state = state * 1103515245 + 12345
                val fillCell = if (isFinder) {
                    val lr = if (r >= grid - 3) r - (grid - 3) else r
                    val lc = if (c >= grid - 3) c - (grid - 3) else c
                    !(lr == 1 && lc == 1)
                } else {
                    ((state ushr 16) and 1) == 0
                }
                if (fillCell) {
                    drawRect(
                        color = CampusSlate,
                        topLeft = Offset(c * cellW, r * cellH),
                        size = Size(cellW * 0.92f, cellH * 0.92f)
                    )
                }
            }
        }
    }
}

@Composable
fun AcademicEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.img_empty_tasks),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(140.dp)
                .clip(MaterialTheme.shapes.medium)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
