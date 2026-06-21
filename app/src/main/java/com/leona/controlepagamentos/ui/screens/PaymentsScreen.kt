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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.leona.controlepagamentos.R
import com.leona.controlepagamentos.data.model.CategoryEntity
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.data.model.PaymentMethod
import com.leona.controlepagamentos.data.model.PaymentStatus
import com.leona.controlepagamentos.domain.money.MoneyFormatter
import com.leona.controlepagamentos.ui.components.formatMoney
import com.leona.controlepagamentos.ui.components.shortTime
import com.leona.controlepagamentos.domain.recurrence.RecurringOccurrence
import com.leona.controlepagamentos.ui.components.AmountText
import com.leona.controlepagamentos.ui.components.CategorySelector
import com.leona.controlepagamentos.ui.components.ImmersiveHeader
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
            ImmersiveHeader(title = stringResource(R.string.screen_payments)) {
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
                item { EmptyText(stringResource(R.string.empty_payments)) }
            } else {
                grouped.forEach { (date, payments) ->
                    val dailyTotal = payments.sumOf { it.amountInCents }
                    item(key = "header_$date") {
                        DayHeader(date = date, totalInCents = dailyTotal)
                    }
                    items(payments, key = { it.id }) { payment ->
                        PaymentRow(payment = payment, onMarkPaid = onMarkPaid)
                    }
                }

                if (uiState.recurringOccurrences.isNotEmpty()) {
                    item { SectionTitle(stringResource(R.string.section_recurring_payments)) }
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
            text = { Text(stringResource(R.string.action_new)) },
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
        PaymentFilter.ALL to stringResource(R.string.filter_all),
        PaymentFilter.PENDING to stringResource(R.string.filter_pending),
        PaymentFilter.PAID to stringResource(R.string.filter_paid),
        PaymentFilter.OVERDUE to stringResource(R.string.filter_overdue)
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
                val timeInfo = if (payment.paidAt != null) " · ${payment.paidAt.shortTime()}" else ""
                Text(
                    "${payment.dueDate.shortDate()} - ${displayStatus.label()}$timeInfo",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AmountText(payment.amountInCents)
            if (payment.status == PaymentStatus.PENDING) {
                IconButton(onClick = { onMarkPaid(payment.id) }) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = stringResource(R.string.action_mark_paid))
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
                Text(
                    "${occurrence.dueDate.shortDate()} - ${stringResource(R.string.label_recurring)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AmountText(occurrence.amountInCents)
            OutlinedButton(onClick = { onMarkPaid(occurrence) }) {
                Text(stringResource(R.string.action_pay))
            }
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate, totalInCents: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date.shortDate(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = formatMoney(totalInCents),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        title = { Text(stringResource(R.string.dialog_new_payment)) },
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
                            label = { Text(stringResource(R.string.payment_mode_single)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = mode == PaymentFormMode.INSTALLMENT,
                            onClick = { mode = PaymentFormMode.INSTALLMENT },
                            label = { Text(stringResource(R.string.payment_mode_installment)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = mode == PaymentFormMode.RECURRING,
                            onClick = { mode = PaymentFormMode.RECURRING },
                            label = { Text(stringResource(R.string.payment_mode_recurring)) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.form_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = {
                        Text(
                            if (mode == PaymentFormMode.INSTALLMENT) stringResource(R.string.form_total_amount)
                            else stringResource(R.string.form_amount)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                when (mode) {
                    PaymentFormMode.SINGLE -> {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text(stringResource(R.string.form_due_date)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PaymentFormMode.INSTALLMENT -> {
                        OutlinedTextField(
                            value = installments,
                            onValueChange = { installments = it },
                            label = { Text(stringResource(R.string.form_installments)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text(stringResource(R.string.form_first_installment)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PaymentFormMode.RECURRING -> {
                        OutlinedTextField(
                            value = dayOfMonth,
                            onValueChange = { dayOfMonth = it },
                            label = { Text(stringResource(R.string.form_day_of_month)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text(stringResource(R.string.form_start_date)) },
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
                    label = { Text(stringResource(R.string.form_notes)) },
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
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
