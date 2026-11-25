package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CampusMessage
import com.example.data.model.SpaceReservation
import com.example.data.model.StudentProfile
import com.example.data.model.TuitionPayment
import com.example.data.model.UiState
import com.example.ui.components.AcademicEmptyState
import com.example.ui.components.CarnetDigitalCard
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
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Locale

@Composable
fun ServicesScreen(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    profileState: UiState<StudentProfile?>,
    paymentsState: UiState<List<TuitionPayment>>,
    reservationsState: UiState<List<SpaceReservation>>,
    messagesState: UiState<List<CampusMessage>>,
    onPayInstallment: (String) -> Unit,
    onCreateReservation: (String, String, String, String) -> Unit,
    onCancelReservation: (String) -> Unit,
    onSendMessage: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Carnet", "Finanzas", "Reservas", "Mensajes")
    val profile = (profileState as? UiState.Success)?.data
    val payments = (paymentsState as? UiState.Success)?.data.orEmpty()
    val reservations = (reservationsState as? UiState.Success)?.data.orEmpty()
    val messages = (messagesState as? UiState.Success)?.data.orEmpty()

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Servicios del Campus",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab.coerceIn(0, tabs.lastIndex),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = AutonomaOrange,
            edgePadding = 12.dp,
            indicator = { positions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(positions[selectedTab.coerceIn(0, tabs.lastIndex)]),
                    color = AutonomaOrange
                )
            }
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onSelectTab(index) },
                    modifier = Modifier.testTag("services_tab_$index"),
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == index) AutonomaOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> CarnetTab(profile = profile)
            1 -> FinanceTab(payments = payments, onPayInstallment = onPayInstallment)
            2 -> ReservationsTab(
                reservations = reservations,
                onCreateReservation = onCreateReservation,
                onCancelReservation = onCancelReservation
            )
            3 -> MessagesTab(messages = messages, onSendMessage = onSendMessage)
        }
    }
}

@Composable
private fun CarnetTab(profile: StudentProfile?) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (profile != null) {
            item { CarnetDigitalCard(profile = profile) }
        }
        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(12.dp), color = AutonomaOrangeContainer) {
                        Icon(
                            imageVector = Icons.Default.LocalLibrary,
                            contentDescription = null,
                            tint = AutonomaOrangeDeep,
                            modifier = Modifier.padding(10.dp).size(24.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Uso del Carnet Digital", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Presenta el código QR en portería, Biblioteca Central, laboratorios de cómputo y talleres del campus.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceTab(
    payments: List<TuitionPayment>,
    onPayInstallment: (String) -> Unit
) {
    val totalPaid = remember(payments) { payments.filter { it.status == "Pagado" }.sumOf { it.amountSoles } }
    val totalPending = remember(payments) { payments.filter { it.status != "Pagado" }.sumOf { it.amountSoles } }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, AutonomaOrange.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Pagado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "S/ ${String.format(Locale.US, "%.2f", totalPaid)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = CampusEmerald
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Saldo Pendiente", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "S/ ${String.format(Locale.US, "%.2f", totalPending)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = if (totalPending > 0) AutonomaOrangeDeep else CampusEmerald
                        )
                    }
                }
            }
        }

        if (payments.isEmpty()) {
            item {
                AcademicEmptyState(
                    title = "Sin cronograma de pagos",
                    subtitle = "Tus cuotas académicas aparecerán aquí."
                )
            }
        } else {
            items(payments, key = { it.id }) { pay ->
                val isPaid = pay.status == "Pagado"
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
                            Text(pay.concept, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            StatusPill(
                                text = pay.status,
                                containerColor = if (isPaid) CampusEmeraldContainer else AutonomaOrangeContainer,
                                contentColor = if (isPaid) CampusEmerald else AutonomaOrangeDeep
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Vence: ${pay.dueDateLabel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "S/ ${String.format(Locale.US, "%.2f", pay.amountSoles)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isPaid && !pay.receiptCode.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CampusEmerald, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Comprobante: ${pay.receiptCode}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = CampusEmerald
                                )
                            }
                        } else if (!isPaid) {
                            Button(
                                onClick = { onPayInstallment(pay.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pay_installment_${pay.id}")
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text("Registrar Pago en Línea")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationsTab(
    reservations: List<SpaceReservation>,
    onCreateReservation: (String, String, String, String) -> Unit,
    onCancelReservation: (String) -> Unit
) {
    var showNewDialog by remember { mutableStateOf(false) }

    if (showNewDialog) {
        NewReservationDialog(
            onDismiss = { showNewDialog = false },
            onConfirm = { space, loc, date, slot ->
                onCreateReservation(space, loc, date, slot)
                showNewDialog = false
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = { showNewDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_reservation_button")
            ) {
                Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Reservar Ambiente del Campus")
            }
        }

        if (reservations.isEmpty()) {
            item {
                AcademicEmptyState(
                    title = "Sin reservas activas",
                    subtitle = "Reserva cubículos de biblioteca, laboratorios o salas de estudio."
                )
            }
        } else {
            items(reservations, key = { it.id }) { res ->
                val active = res.status == "Confirmada"
                Card(
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
                            Text(res.spaceName, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            StatusPill(
                                text = res.status,
                                containerColor = if (active) CampusEmeraldContainer else CampusCrimsonContainer,
                                contentColor = if (active) CampusEmerald else CampusCrimson
                            )
                        }
                        Text(res.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${res.dateLabel} · ${res.timeSlot}", style = MaterialTheme.typography.labelMedium, color = AutonomaOrangeDeep)
                        if (active) {
                            OutlinedButton(
                                onClick = { onCancelReservation(res.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.size(6.dp))
                                Text("Cancelar Reserva")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessagesTab(
    messages: List<CampusMessage>,
    onSendMessage: (String, String, String, String) -> Unit
) {
    var showCompose by remember { mutableStateOf(false) }

    if (showCompose) {
        NewMessageDialog(
            onDismiss = { showCompose = false },
            onConfirm = { recipient, category, subject, body ->
                onSendMessage(recipient, category, subject, body)
                showCompose = false
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = { showCompose = true },
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_message_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Nuevo Mensaje o Trámite")
            }
        }

        if (messages.isEmpty()) {
            item {
                AcademicEmptyState(
                    title = "Bandeja vacía",
                    subtitle = "Envía consultas a tus docentes o solicita trámites académicos."
                )
            }
        } else {
            items(messages, key = { it.id }) { msg ->
                val answered = msg.status == "Respondido"
                Card(
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
                                StatusPill(msg.category, AutonomaOrangeContainer, AutonomaOrangeDeep)
                                StatusPill(
                                    msg.status,
                                    if (answered) CampusEmeraldContainer else CampusCobaltContainer,
                                    if (answered) CampusEmerald else CampusCobalt
                                )
                            }
                            Icon(Icons.Default.Mail, contentDescription = null, tint = AutonomaOrange, modifier = Modifier.size(18.dp))
                        }
                        Text(msg.subject, style = MaterialTheme.typography.titleSmall)
                        Text("Para: ${msg.recipient}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(msg.body, style = MaterialTheme.typography.bodySmall)
                        if (!msg.replyText.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CampusEmeraldContainer
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Respuesta institucional:", style = MaterialTheme.typography.labelSmall, color = CampusEmerald)
                                    Text(msg.replyText, style = MaterialTheme.typography.bodySmall, color = CampusEmerald)
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
private fun NewReservationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    val spaces = listOf(
        "Cubículo de Estudio 02" to "Biblioteca Central · Piso 2",
        "Laboratorio de Cómputo 01" to "Pabellón Ingeniería · Piso 3",
        "Sala Audiovisual B" to "Pabellón A · Piso 1",
        "Mesa Colaborativa Maker" to "Centro de Innovación"
    )
    var selectedSpace by remember { mutableStateOf(spaces.first()) }
    var dateLabel by remember { mutableStateOf("29 Nov 2026") }
    var timeSlot by remember { mutableStateOf("15:00 - 17:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Reserva") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Selecciona ambiente:", style = MaterialTheme.typography.labelMedium)
                spaces.forEach { pair ->
                    FilterChip(
                        selected = selectedSpace == pair,
                        onClick = { selectedSpace = pair },
                        label = { Text(pair.first) }
                    )
                }
                OutlinedTextField(value = dateLabel, onValueChange = { dateLabel = it }, label = { Text("Fecha") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = timeSlot, onValueChange = { timeSlot = it }, label = { Text("Horario") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedSpace.first, selectedSpace.second, dateLabel, timeSlot) },
                enabled = dateLabel.trim().length >= 2 && timeSlot.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange)
            ) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun NewMessageDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var category by remember { mutableStateOf("Trámite") }
    var recipient by remember { mutableStateOf("Servicios Académicos") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Mensaje / Trámite") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Trámite", "Docente", "Soporte").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = {
                                category = cat
                                recipient = when (cat) {
                                    "Docente" -> "Mg. Carlos Paredes Vela"
                                    "Soporte" -> "Mesa de Ayuda Aula Virtual"
                                    else -> "Servicios Académicos"
                                }
                            },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(value = recipient, onValueChange = { recipient = it }, label = { Text("Destinatario") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Asunto") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Detalle de solicitud") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(recipient, category, subject, body) },
                enabled = subject.trim().length >= 2 && body.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = AutonomaOrange)
            ) { Text("Enviar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
