package com.leona.controlepagamentos.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.leona.controlepagamentos.ui.components.ImmersiveHeader
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leona.controlepagamentos.data.model.CategoryEntity
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.data.model.PaymentMethod
import com.leona.controlepagamentos.data.model.PaymentStatus
import com.leona.controlepagamentos.domain.recurrence.RecurringOccurrence
import com.leona.controlepagamentos.ui.components.AmountText
import com.leona.controlepagamentos.ui.components.CategorySelector
import com.leona.controlepagamentos.ui.components.PaymentMethodSelector
import com.leona.controlepagamentos.ui.components.label
import com.leona.controlepagamentos.ui.components.shortDate
import com.leona.controlepagamentos.ui.viewmodel.PaymentFilter
import com.leona.controlepagamentos.ui.viewmodel.PaymentsUiState
import java.time.LocalDate

@Composable
fun PaymentsScreen(
    uiState: PaymentsUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onFilterChanged: (PaymentFilter) -> Unit,
    onMarkPaid: (String) -> Unit,
    onMarkRecurringPaid: (RecurringOccurrence) -> Unit,
    onAddSingle: (String, String, String, String?, PaymentMethod?, String?) -> Unit,
    onAddInstallment: (String, String, String, String, String?, PaymentMethod?, String?) -> Unit,
    onAddRecurring: (String, String, String, String, String?, PaymentMethod?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showForm by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            ImmersiveHeader(title = "Pagamentos") {
                MonthHeader(
                    label = uiState.monthLabel,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp)
            ) {
            item {
                PaymentFilterRow(uiState.filter, onFilterChanged)
            }

            val grouped = uiState.payments.groupBy { it.dueDate }.toSortedMap()
            if (grouped.isEmpty() && uiState.recurringOccurrences.isEmpty()) {
                item { EmptyText("Nenhum pagamento neste mes.") }
            } else {
                grouped.forEach { (date, payments) ->
                    item {
                        Text(
                            text = date.shortDate(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(payments, key = { it.id }) { payment ->
                        PaymentRow(payment = payment, onMarkPaid = onMarkPaid)
                    }
                }

                if (uiState.recurringOccurrences.isNotEmpty()) {
                    item { SectionTitle("Previstos por recorrencia") }
                    items(uiState.recurringOccurrences, key = { it.ruleId + it.dueDate }) { occurrence ->
                        RecurringPaymentRow(occurrence = occurrence, onMarkPaid = onMarkRecurringPaid)
                    }
                }
            }
        }  // LazyColumn
        }  // Column

        ExtendedFloatingActionButton(
            onClick = { showForm = true },
            icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
            text = { Text("Novo") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    if (showForm) {
        AddPaymentDialog(
            categories = uiState.categories,
            onDismiss = { showForm = false },
            onAddSingle = { title, amount, dueDate, categoryId, method, notes ->
                onAddSingle(title, amount, dueDate, categoryId, method, notes)
                showForm = false
            },
            onAddInstallment = { title, totalAmount, installments, firstDueDate, categoryId, method, notes ->
                onAddInstallment(title, totalAmount, installments, firstDueDate, categoryId, method, notes)
                showForm = false
            },
            onAddRecurring = { title, amount, day, startDate, categoryId, method, notes ->
                onAddRecurring(title, amount, day, startDate, categoryId, method, notes)
                showForm = false
            }
        )
    }
}

@Composable
private fun PaymentFilterRow(selected: PaymentFilter, onSelected: (PaymentFilter) -> Unit) {
    val labels = mapOf(
        PaymentFilter.ALL to "Todos",
        PaymentFilter.PENDING to "Pendentes",
        PaymentFilter.PAID to "Pagos",
        PaymentFilter.OVERDUE to "Vencidos"
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(PaymentFilter.entries, key = { it.name }) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(labels.getValue(filter)) }
            )
        }
    }
}

@Composable
private fun PaymentRow(payment: PaymentEntity, onMarkPaid: (String) -> Unit) {
    val today = LocalDate.now()
    val displayStatus = if (payment.status == PaymentStatus.PENDING && payment.dueDate.isBefore(today)) {
        PaymentStatus.OVERDUE
    } else {
        payment.status
    }

    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(payment.title, fontWeight = FontWeight.SemiBold)
                Text(
                    "${payment.dueDate.shortDate()} - ${displayStatus.label()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AmountText(payment.amountInCents)
            if (payment.status == PaymentStatus.PENDING) {
                IconButton(onClick = { onMarkPaid(payment.id) }) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = "Marcar pago")
                }
            }
        }
    }
}

@Composable
private fun RecurringPaymentRow(
    occurrence: RecurringOccurrence,
    onMarkPaid: (RecurringOccurrence) -> Unit
) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(occurrence.title, fontWeight = FontWeight.SemiBold)
                Text("${occurrence.dueDate.shortDate()} - Recorrente", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AmountText(occurrence.amountInCents)
            OutlinedButton(onClick = { onMarkPaid(occurrence) }) {
                Text("Pagar")
            }
        }
    }
}

private enum class PaymentFormMode {
    SINGLE,
    INSTALLMENT,
    RECURRING
}

@Composable
private fun AddPaymentDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onAddSingle: (String, String, String, String?, PaymentMethod?, String?) -> Unit,
    onAddInstallment: (String, String, String, String, String?, PaymentMethod?, String?) -> Unit,
    onAddRecurring: (String, String, String, String, String?, PaymentMethod?, String?) -> Unit
) {
    var mode by remember { mutableStateOf(PaymentFormMode.SINGLE) }
    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var installments by rememberSaveable { mutableStateOf("3") }
    var dayOfMonth by rememberSaveable { mutableStateOf(LocalDate.now().dayOfMonth.toString()) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var method by remember { mutableStateOf<PaymentMethod?>(null) }
    var notes by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo pagamento") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    item {
                        FilterChip(
                            selected = mode == PaymentFormMode.SINGLE,
                            onClick = { mode = PaymentFormMode.SINGLE },
                            label = { Text("Unico") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = mode == PaymentFormMode.INSTALLMENT,
                            onClick = { mode = PaymentFormMode.INSTALLMENT },
                            label = { Text("Parcelado") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = mode == PaymentFormMode.RECURRING,
                            onClick = { mode = PaymentFormMode.RECURRING },
                            label = { Text("Recorrente") }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titulo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(if (mode == PaymentFormMode.INSTALLMENT) "Valor total" else "Valor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                when (mode) {
                    PaymentFormMode.SINGLE -> {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Vencimento AAAA-MM-DD") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PaymentFormMode.INSTALLMENT -> {
                        OutlinedTextField(
                            value = installments,
                            onValueChange = { installments = it },
                            label = { Text("Parcelas") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Primeira parcela AAAA-MM-DD") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PaymentFormMode.RECURRING -> {
                        OutlinedTextField(
                            value = dayOfMonth,
                            onValueChange = { dayOfMonth = it },
                            label = { Text("Dia do mes") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Inicio AAAA-MM-DD") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                CategorySelector(categories, selectedCategory, { selectedCategory = it })
                PaymentMethodSelector(method, { method = it })
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observacoes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (mode) {
                        PaymentFormMode.SINGLE -> onAddSingle(title, amount, date, selectedCategory, method, notes)
                        PaymentFormMode.INSTALLMENT -> onAddInstallment(title, amount, installments, date, selectedCategory, method, notes)
                        PaymentFormMode.RECURRING -> onAddRecurring(title, amount, dayOfMonth, date, selectedCategory, method, notes)
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
