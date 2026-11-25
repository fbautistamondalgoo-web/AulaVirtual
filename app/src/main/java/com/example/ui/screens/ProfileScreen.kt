package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Course
import com.example.data.model.StudentProfile
import com.example.data.model.UiState
import com.example.ui.theme.AutonomaOrange
import com.example.ui.theme.AutonomaOrangeDeep
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Locale

@Composable
fun ProfileScreen(
    profileState: UiState<StudentProfile?>,
    coursesState: UiState<List<Course>>,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onUpdateProfile: (String, String, String, String, String, String, String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = (profileState as? UiState.Success)?.data
    val courses = (coursesState as? UiState.Success)?.data.orEmpty()
    var showEditDialog by remember { mutableStateOf(false) }

    val totalCredits = remember(courses) { courses.sumOf { it.credits } }
    val gpa = remember(courses) {
        val list = courses.map { it.currentPartialAverage() }.filter { it > 0.0 }
        if (list.isEmpty()) 0.0 else list.average()
    }

    if (showEditDialog && profile != null) {
        EditProfileDialog(
            profile = profile,
            onDismiss = { showEditDialog = false },
            onSave = { name, faculty, career, cycle, campus, modality, phone ->
                onUpdateProfile(name, faculty, career, cycle, campus, modality, phone)
                showEditDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Mi Perfil Académico",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!profile?.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profile?.photoUrl,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.img_student_avatar),
                            error = painterResource(R.drawable.img_student_avatar),
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .border(3.dp, AutonomaOrange, CircleShape)
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.img_student_avatar),
                            contentDescription = "Avatar universitario",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .border(3.dp, AutonomaOrange, CircleShape)
                        )
                    }
                    Text(
                        text = profile?.fullName ?: "Estudiante",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = profile?.email ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Código: ${profile?.studentCode ?: "--"}",
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = AutonomaOrangeDeep
                    )
                    if (profile != null) {
                        OutlinedButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Editar Datos Académicos")
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricBox("Cursos", "${courses.size}", Modifier.weight(1f))
                MetricBox("Créditos", "$totalCredits", Modifier.weight(1f))
                MetricBox("Ponderado", if (gpa > 0) String.format(Locale.US, "%.1f", gpa) else "--", Modifier.weight(1f))
            }
        }

        if (profile != null) {
            item {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoRow(Icons.Default.School, "Facultad y Carrera", "${profile.faculty}\n${profile.career}")
                        InfoRow(Icons.Default.LocationCity, "Sede y Modalidad", "${profile.campus} · ${profile.modality} (${profile.cycle})")
                        InfoRow(Icons.Default.Phone, "Teléfono de contacto", profile.phone.ifBlank { "No registrado" })
                    }
                }
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = AutonomaOrange)
                        Column {
                            Text("Modo Oscuro", style = MaterialTheme.typography.titleSmall)
                            Text("Apariencia del Aula Virtual", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onToggleDarkTheme,
                        colors = SwitchDefaults.colors(checkedTrackColor = AutonomaOrange)
                    )
                }
            }
        }

        item {
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_out_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Cerrar Sesión del Campus")
            }
        }
    }
}

@Composable
private fun MetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = JetBrainsMonoFontFamily,
                color = AutonomaOrangeDeep
            )
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = AutonomaOrange, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EditProfileDialog(
    profile: StudentProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf(profile.fullName) }
    var faculty by remember { mutableStateOf(profile.faculty) }
    var career by remember { mutableStateOf(profile.career) }
    var cycle by remember { mutableStateOf(profile.cycle) }
    var campus by remember { mutableStateOf(profile.campus) }
    var modality by remember { mutableStateOf(profile.modality) }
    var phone by remember { mutableStateOf(profile.phone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nombre completo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = career, onValueChange = { career = it }, label = { Text("Carrera") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cycle, onValueChange = { cycle = it }, label = { Text("Ciclo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = campus, onValueChange = { campus = it }, label = { Text("Sede") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Presencial", "Semipresencial", "Virtual").forEach { mod ->
                        FilterChip(selected = modality == mod, onClick = { modality = mod }, label = { Text(mod) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(fullName, faculty, career, cycle, campus, modality, phone) },
                enabled = fullName.trim().isNotBlank() && career.trim().isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange)
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
