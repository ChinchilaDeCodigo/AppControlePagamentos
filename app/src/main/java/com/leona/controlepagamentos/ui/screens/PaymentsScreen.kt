package com.leona.controlepagamentos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.leona.controlepagamentos.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import com.leona.controlepagamentos.data.model.CategoryEntity
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.data.model.PaymentMethod
import com.leona.controlepagamentos.data.model.PaymentStatus
import com.leona.controlepagamentos.data.model.RecurringPaymentRuleEntity
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

enum class PaymentSort { DATE_ASC, DATE_DESC, AMOUNT_ASC, AMOUNT_DESC, DAILY_TOTAL_ASC, DAILY_TOTAL_DESC }

@OptIn(ExperimentalMaterial3Api::class)
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
    onUpdatePayment: (PaymentEntity) -> Unit = {},
    onDeletePayment: (String) -> Unit = {},
    onUpdateRecurring: (RecurringPaymentRuleEntity) -> Unit = {},
    onDeleteRecurring: (String) -> Unit = {},
    categoryFilter: String? = null,
    onSetCategoryFilter: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf<PaymentEntity?>(null) }
    var sortOrder by rememberSaveable { mutableStateOf(PaymentSort.DATE_ASC) }
    var showRecurringSheet by remember { mutableStateOf(false) }

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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PaymentFilterRow(uiState.filter, onFilterChanged)
                        }
                        var showSortMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    Icons.Outlined.SwapVert,
                                    contentDescription = null,
                                    tint = if (sortOrder != PaymentSort.DATE_ASC)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        LocalContentColor.current
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                val checkIcon: @Composable (PaymentSort) -> Unit = { s ->
                                    if (sortOrder == s) Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                                }
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_date_asc)) },
                                    leadingIcon = { checkIcon(PaymentSort.DATE_ASC) },
                                    onClick = { sortOrder = PaymentSort.DATE_ASC; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_date_desc)) },
                                    leadingIcon = { checkIcon(PaymentSort.DATE_DESC) },
                                    onClick = { sortOrder = PaymentSort.DATE_DESC; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_amount_asc)) },
                                    leadingIcon = { checkIcon(PaymentSort.AMOUNT_ASC) },
                                    onClick = { sortOrder = PaymentSort.AMOUNT_ASC; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_amount_desc)) },
                                    leadingIcon = { checkIcon(PaymentSort.AMOUNT_DESC) },
                                    onClick = { sortOrder = PaymentSort.AMOUNT_DESC; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_daily_total_asc)) },
                                    leadingIcon = { checkIcon(PaymentSort.DAILY_TOTAL_ASC) },
                                    onClick = { sortOrder = PaymentSort.DAILY_TOTAL_ASC; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_daily_total_desc)) },
                                    leadingIcon = { checkIcon(PaymentSort.DAILY_TOTAL_DESC) },
                                    onClick = { sortOrder = PaymentSort.DAILY_TOTAL_DESC; showSortMenu = false }
                                )
                            }
                        }
                    }
                    if (uiState.categories.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = categoryFilter == null,
                                    onClick = { onSetCategoryFilter(null) },
                                    label = { Text(stringResource(R.string.filter_all)) }
                                )
                            }
                            items(uiState.categories, key = { it.id }) { category ->
                                FilterChip(
                                    selected = categoryFilter == category.id,
                                    onClick = {
                                        onSetCategoryFilter(
                                            if (categoryFilter == category.id) null else category.id
                                        )
                                    },
                                    label = { Text(category.name) }
                                )
                            }
                        }
                    }
                }
            }

            val basePayments = if (categoryFilter != null)
                uiState.payments.filter { it.categoryId == categoryFilter }
            else
                uiState.payments
            val grouped: Map<LocalDate, List<PaymentEntity>> = run {
                val base = basePayments.groupBy { it.dueDate }
                when (sortOrder) {
                    PaymentSort.DATE_ASC -> base.toSortedMap()
                    PaymentSort.DATE_DESC -> base.entries.sortedByDescending { it.key }.associate { it.key to it.value }
                    PaymentSort.AMOUNT_ASC -> base.toSortedMap().mapValues { (_, v) -> v.sortedBy { it.amountInCents } }
                    PaymentSort.AMOUNT_DESC -> base.toSortedMap().mapValues { (_, v) -> v.sortedByDescending { it.amountInCents } }
                    PaymentSort.DAILY_TOTAL_ASC -> base.entries
                        .sortedBy { (_, v) -> v.sumOf { it.amountInCents } }
                        .associate { it.key to it.value }
                    PaymentSort.DAILY_TOTAL_DESC -> base.entries
                        .sortedByDescending { (_, v) -> v.sumOf { it.amountInCents } }
                        .associate { it.key to it.value }
                }
            }
            if (grouped.isEmpty() && uiState.recurringOccurrences.isEmpty()) {
                item { EmptyText(stringResource(R.string.empty_payments)) }
            } else {
                grouped.forEach { (date, payments) ->
                    val dailyTotal = payments.sumOf { it.amountInCents }
                    item(key = "header_$date") {
                        DayHeader(date = date, totalInCents = dailyTotal)
                    }
                    items(payments, key = { it.id }) { payment ->
                        PaymentRow(
                            payment = payment,
                            onMarkPaid = onMarkPaid,
                            onClick = { selectedPayment = payment }
                        )
                    }
                }

                if (uiState.recurringOccurrences.isNotEmpty()) {
                    item { SectionTitle(stringResource(R.string.section_recurring_payments)) }
                    items(uiState.recurringOccurrences, key = { it.ruleId + it.dueDate }) { occurrence ->
                        RecurringPaymentRow(occurrence = occurrence, onMarkPaid = onMarkRecurringPaid)
                    }
                }
                if (uiState.recurringRules.isNotEmpty()) {
                    item {
                        TextButton(
                            onClick = { showRecurringSheet = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Repeat, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.action_manage_recurring))
                        }
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

    selectedPayment?.let { payment ->
        PaymentDetailSheet(
            payment = payment,
            categories = uiState.categories,
            onDismiss = { selectedPayment = null },
            onSave = { updated -> onUpdatePayment(updated); selectedPayment = null },
            onMarkPaid = { id -> onMarkPaid(id); selectedPayment = null },
            onDelete = { id -> onDeletePayment(id); selectedPayment = null }
        )
    }

    if (showRecurringSheet) {
        RecurringRulesSheet(
            rules = uiState.recurringRules,
            categories = uiState.categories,
            onDismiss = { showRecurringSheet = false },
            onUpdate = onUpdateRecurring,
            onDelete = onDeleteRecurring
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
private fun PaymentRow(payment: PaymentEntity, onMarkPaid: (String) -> Unit, onClick: () -> Unit = {}) {
    val today = LocalDate.now()
    val displayStatus = if (payment.status == PaymentStatus.PENDING && payment.dueDate.isBefore(today)) {
        PaymentStatus.OVERDUE
    } else {
        payment.status
    }

    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(payment.title, fontWeight = FontWeight.SemiBold)
                val timeInfo = if (payment.paidAt != null) " · ${payment.paidAt.shortTime()}" else ""
                Text(
                    "${payment.dueDate.shortDate()}$timeInfo - ${displayStatus.label()}",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDetailSheet(
    payment: PaymentEntity,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (PaymentEntity) -> Unit,
    onMarkPaid: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = LocalDate.now()
    val displayStatus = if (payment.status == PaymentStatus.PENDING && payment.dueDate.isBefore(today))
        PaymentStatus.OVERDUE else payment.status

    val locale = Locale.getDefault()
    val isDayFirst = remember {
        val test = LocalDate.of(2000, 12, 31)
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale).format(test).startsWith("31")
    }

    var title by remember { mutableStateOf(payment.title) }
    var amountDigits by remember { mutableStateOf(payment.amountInCents.toString()) }
    var dateDigits by remember {
        mutableStateOf(
            if (isDayFirst) "%02d%02d%04d".format(payment.dueDate.dayOfMonth, payment.dueDate.monthValue, payment.dueDate.year)
            else "%02d%02d%04d".format(payment.dueDate.monthValue, payment.dueDate.dayOfMonth, payment.dueDate.year)
        )
    }
    var selectedCategory by remember { mutableStateOf(payment.categoryId) }
    var method by remember { mutableStateOf(payment.paymentMethod) }
    var notes by remember { mutableStateOf(payment.notes.orEmpty()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.dialog_delete_payment_title)) },
            text = { Text(stringResource(R.string.dialog_delete_payment_desc)) },
            confirmButton = {
                TextButton(
                    onClick = { onDelete(payment.id) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val timeInfo = if (payment.paidAt != null) " · ${payment.paidAt.shortTime()}" else ""
                Text(
                    "${displayStatus.label()}$timeInfo",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AmountText(payment.amountInCents)
            }
            HorizontalDivider()
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.form_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            val amountDisplay = formatAmountInput(amountDigits)
            OutlinedTextField(
                value = TextFieldValue(amountDisplay, selection = TextRange(amountDisplay.length)),
                onValueChange = { amountDigits = it.text.filter { c -> c.isDigit() }.take(10) },
                label = { Text(stringResource(R.string.form_amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formatDateInput(dateDigits),
                onValueChange = { dateDigits = it.filter { c -> c.isDigit() }.take(8) },
                label = { Text(stringResource(R.string.form_due_date)) },
                placeholder = { Text(if (isDayFirst) "DD/MM/AAAA" else "MM/DD/AAAA") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            CategorySelector(categories, selectedCategory, { selectedCategory = it })
            PaymentMethodSelector(selected = method, onSelected = { method = it })
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.form_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (payment.status == PaymentStatus.PENDING) {
                    OutlinedButton(
                        onClick = { onMarkPaid(payment.id) },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.action_mark_paid)) }
                }
                Button(
                    onClick = {
                        val newDate = runCatching {
                            LocalDate.parse(dateInputToIso(dateDigits, isDayFirst))
                        }.getOrElse { payment.dueDate }
                        val newAmount = MoneyFormatter.parseToCents(formatAmountInput(amountDigits))
                            ?: payment.amountInCents
                        onSave(payment.copy(
                            title = title.trim().ifBlank { payment.title },
                            amountInCents = newAmount,
                            dueDate = newDate,
                            categoryId = selectedCategory,
                            paymentMethod = method,
                            notes = notes.trim().ifBlank { null }
                        ))
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.action_save)) }
            }
            TextButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text(stringResource(R.string.action_delete)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringRulesSheet(
    rules: List<RecurringPaymentRuleEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onUpdate: (RecurringPaymentRuleEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRule by remember { mutableStateOf<RecurringPaymentRuleEntity?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.screen_recurring_rules),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            HorizontalDivider()
            if (rules.isEmpty()) {
                EmptyText(stringResource(R.string.empty_recurring_desc))
            } else {
                rules.forEach { rule ->
                    RecurringRuleRow(rule) { selectedRule = rule }
                }
            }
        }
    }

    selectedRule?.let { rule ->
        RecurringRuleDetailSheet(
            rule = rule,
            categories = categories,
            onDismiss = { selectedRule = null },
            onSave = { updated -> onUpdate(updated); selectedRule = null },
            onDelete = { id -> onDelete(id); selectedRule = null }
        )
    }
}

@Composable
private fun RecurringRuleRow(rule: RecurringPaymentRuleEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(rule.title, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.recurring_day_label, rule.dayOfMonth),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AmountText(rule.amountInCents)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringRuleDetailSheet(
    rule: RecurringPaymentRuleEntity,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (RecurringPaymentRuleEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(rule.title) }
    var amountDigits by remember { mutableStateOf(rule.amountInCents.toString()) }
    var dayOfMonth by remember { mutableStateOf(rule.dayOfMonth.toString()) }
    var selectedCategory by remember { mutableStateOf(rule.categoryId) }
    var method by remember { mutableStateOf(rule.paymentMethod) }
    var notes by remember { mutableStateOf(rule.notes.orEmpty()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.dialog_delete_recurring_title)) },
            text = { Text(stringResource(R.string.dialog_delete_recurring_desc)) },
            confirmButton = {
                TextButton(
                    onClick = { onDelete(rule.id) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                rule.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            HorizontalDivider()
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.form_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            val amountDisplay = formatAmountInput(amountDigits)
            OutlinedTextField(
                value = TextFieldValue(amountDisplay, selection = TextRange(amountDisplay.length)),
                onValueChange = { amountDigits = it.text.filter { c -> c.isDigit() }.take(10) },
                label = { Text(stringResource(R.string.form_amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dayOfMonth,
                onValueChange = { dayOfMonth = it.filter { c -> c.isDigit() }.take(2) },
                label = { Text(stringResource(R.string.form_day_of_month)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            CategorySelector(categories, selectedCategory, { selectedCategory = it })
            PaymentMethodSelector(selected = method, onSelected = { method = it })
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.form_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Button(
                onClick = {
                    val newAmount = MoneyFormatter.parseToCents(formatAmountInput(amountDigits))
                        ?: rule.amountInCents
                    val newDay = dayOfMonth.toIntOrNull()?.coerceIn(1, 31) ?: rule.dayOfMonth
                    onSave(rule.copy(
                        title = title.trim().ifBlank { rule.title },
                        amountInCents = newAmount,
                        dayOfMonth = newDay,
                        categoryId = selectedCategory,
                        paymentMethod = method,
                        notes = notes.trim().ifBlank { null }
                    ))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.action_save)) }
            TextButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text(stringResource(R.string.action_delete)) }
        }
    }
}

private fun formatAmountInput(digits: String): String {
    if (digits.isEmpty()) return ""
    val padded = digits.padStart(3, '0')
    val intPart = padded.dropLast(2).trimStart('0').ifEmpty { "0" }
    return "$intPart,${padded.takeLast(2)}"
}

private fun formatDateInput(digits: String): String = buildString {
    digits.take(8).forEachIndexed { i, c ->
        if (i == 2 || i == 4) append('/')
        append(c)
    }
}

private fun dateInputToIso(digits: String, isDayFirst: Boolean): String {
    if (digits.length < 8) return digits
    return try {
        val pattern = if (isDayFirst) "dd/MM/yyyy" else "MM/dd/yyyy"
        LocalDate.parse(formatDateInput(digits), DateTimeFormatter.ofPattern(pattern)).toString()
    } catch (e: Exception) { digits }
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
    val locale = Locale.getDefault()
    val isDayFirst = remember {
        val test = LocalDate.of(2000, 12, 31)
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale).format(test).startsWith("31")
    }
    var mode by remember { mutableStateOf(PaymentFormMode.SINGLE) }
    var title by rememberSaveable { mutableStateOf("") }
    var amountDigits by rememberSaveable { mutableStateOf("") }
    var dateDigits by rememberSaveable {
        val t = LocalDate.now()
        mutableStateOf(
            if (isDayFirst) "%02d%02d%04d".format(t.dayOfMonth, t.monthValue, t.year)
            else "%02d%02d%04d".format(t.monthValue, t.dayOfMonth, t.year)
        )
    }
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
                val amountDisplay = formatAmountInput(amountDigits)
                OutlinedTextField(
                    value = TextFieldValue(amountDisplay, selection = TextRange(amountDisplay.length)),
                    onValueChange = { amountDigits = it.text.filter { c -> c.isDigit() }.take(10) },
                    label = {
                        Text(
                            if (mode == PaymentFormMode.INSTALLMENT) stringResource(R.string.form_total_amount)
                            else stringResource(R.string.form_amount)
                        )
                    },
                    placeholder = { Text("0,00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                when (mode) {
                    PaymentFormMode.SINGLE -> {
                        OutlinedTextField(
                            value = formatDateInput(dateDigits),
                            onValueChange = { dateDigits = it.filter { c -> c.isDigit() }.take(8) },
                            label = { Text(stringResource(R.string.form_due_date)) },
                            placeholder = { Text(if (isDayFirst) "DD/MM/AAAA" else "MM/DD/AAAA") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            value = formatDateInput(dateDigits),
                            onValueChange = { dateDigits = it.filter { c -> c.isDigit() }.take(8) },
                            label = { Text(stringResource(R.string.form_first_installment)) },
                            placeholder = { Text(if (isDayFirst) "DD/MM/AAAA" else "MM/DD/AAAA") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            value = formatDateInput(dateDigits),
                            onValueChange = { dateDigits = it.filter { c -> c.isDigit() }.take(8) },
                            label = { Text(stringResource(R.string.form_start_date)) },
                            placeholder = { Text(if (isDayFirst) "DD/MM/AAAA" else "MM/DD/AAAA") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    val isoDate = dateInputToIso(dateDigits, isDayFirst)
                    val formattedAmount = formatAmountInput(amountDigits)
                    when (mode) {
                        PaymentFormMode.SINGLE -> onAddSingle(title, formattedAmount, isoDate, selectedCategory, method, notes)
                        PaymentFormMode.INSTALLMENT -> onAddInstallment(title, formattedAmount, installments, isoDate, selectedCategory, method, notes)
                        PaymentFormMode.RECURRING -> onAddRecurring(title, formattedAmount, dayOfMonth, isoDate, selectedCategory, method, notes)
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
