package com.leona.controlepagamentos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leona.controlepagamentos.R
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.domain.budget.BudgetHealth
import com.leona.controlepagamentos.domain.budget.BudgetProgress
import com.leona.controlepagamentos.domain.money.MoneyFormatter
import com.leona.controlepagamentos.domain.recurrence.RecurringOccurrence
import com.leona.controlepagamentos.ui.components.AmountText
import com.leona.controlepagamentos.ui.components.ImmersiveHeader
import com.leona.controlepagamentos.ui.components.shortDate
import com.leona.controlepagamentos.ui.theme.Alert
import com.leona.controlepagamentos.ui.theme.Attention
import com.leona.controlepagamentos.ui.theme.Blue
import com.leona.controlepagamentos.ui.theme.Success
import com.leona.controlepagamentos.ui.viewmodel.CategoryTotal
import com.leona.controlepagamentos.ui.viewmodel.PaymentsUiState

@Composable
fun DashboardScreen(
    uiState: PaymentsUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMarkPaid: (String) -> Unit,
    onMarkRecurringPaid: (RecurringOccurrence) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryMaxAmount = uiState.categoryTotals.maxOfOrNull { it.amountInCents }?.takeIf { it > 0 } ?: 1L
    val categoryColorMap = uiState.categories.associate { it.id to it.colorHex }

    Column(modifier = modifier) {
        ImmersiveHeader(title = stringResource(R.string.screen_dashboard)) {
            MonthHeader(
                label = uiState.monthLabel,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = MoneyFormatter.format(uiState.summary.paidInCents),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.dashboard_spent_in_month, uiState.monthLabel),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 72.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        StatTile(stringResource(R.string.label_planned), uiState.summary.plannedInCents, Blue, Modifier.weight(1f))
                        StatTile(stringResource(R.string.label_paid), uiState.summary.paidInCents, Success, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        StatTile(stringResource(R.string.label_pending), uiState.summary.pendingInCents, Attention, Modifier.weight(1f))
                        StatTile(stringResource(R.string.label_overdue), uiState.summary.overdueInCents, Alert, Modifier.weight(1f))
                    }
                }
            }
            uiState.budgetProgress.firstOrNull { it.categoryId == null }?.let { budget ->
                item {
                    BudgetOverviewCard(budget = budget)
                }
            }
            item {
                CaptureSummary(uiState.summary.pendingCaptureCount)
            }
            if (uiState.categoryTotals.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.section_spending_by_category)) }
                items(uiState.categoryTotals.take(5), key = { "cat_${it.categoryId ?: "none"}" }) { total ->
                    CategoryBar(
                        total = total,
                        maxAmount = categoryMaxAmount,
                        colorHex = categoryColorMap[total.categoryId]
                    )
                }
            }
            item {
                SectionTitle(stringResource(R.string.section_upcoming_payments))
            }
            if (uiState.upcomingPayments.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.CheckCircle,
                        title = stringResource(R.string.empty_upcoming_title),
                        description = stringResource(R.string.empty_upcoming_desc)
                    )
                }
            } else {
                items(uiState.upcomingPayments, key = { it.id }) { payment ->
                    UpcomingPaymentRow(payment = payment, onMarkPaid = onMarkPaid)
                }
            }
            item {
                SectionTitle(stringResource(R.string.section_recurring))
            }
            if (uiState.recurringOccurrences.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Schedule,
                        title = stringResource(R.string.empty_recurring_title),
                        description = stringResource(R.string.empty_recurring_desc)
                    )
                }
            } else {
                items(uiState.recurringOccurrences, key = { it.ruleId + it.dueDate }) { occurrence ->
                    RecurringOccurrenceRow(occurrence = occurrence, onMarkPaid = onMarkRecurringPaid)
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    title: String,
    amountInCents: Long,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.small) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = MoneyFormatter.format(amountInCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CategoryBar(
    total: CategoryTotal,
    maxAmount: Long,
    colorHex: String?
) {
    val barColor = colorHex?.let {
        runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
    } ?: MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(barColor, shape = CircleShape)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(total.categoryName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    MoneyFormatter.format(total.amountInCents),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            LinearProgressIndicator(
                progress = { total.amountInCents.toFloat() / maxAmount.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = barColor,
                trackColor = barColor.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
private fun BudgetOverviewCard(budget: BudgetProgress) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.label_monthly_budget), style = MaterialTheme.typography.labelLarge)
                    Text(budget.name, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "${budget.percentUsed}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = budget.health.color()
                )
            }
            LinearProgressIndicator(
                progress = { (budget.percentUsed / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = budget.health.color()
            )
            Text(
                stringResource(
                    R.string.label_spent_of_limit,
                    MoneyFormatter.format(budget.spentInCents),
                    MoneyFormatter.format(budget.limitInCents)
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BudgetHealth.color() = when (this) {
    BudgetHealth.HEALTHY -> Success
    BudgetHealth.ATTENTION -> Attention
    BudgetHealth.CRITICAL -> Alert
    BudgetHealth.EXCEEDED -> MaterialTheme.colorScheme.error
}

@Composable
fun MonthHeader(
    label: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Outlined.ChevronLeft, contentDescription = stringResource(R.string.nav_previous_month))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = stringResource(R.string.nav_next_month))
        }
    }
}

@Composable
private fun CaptureSummary(count: Int) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Schedule, contentDescription = null)
            Column {
                Text(stringResource(R.string.captures_pending_title), style = MaterialTheme.typography.labelLarge)
                Text(
                    stringResource(R.string.captures_pending_count, count),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun UpcomingPaymentRow(payment: PaymentEntity, onMarkPaid: (String) -> Unit) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(payment.title, fontWeight = FontWeight.SemiBold)
                Text(payment.dueDate.shortDate(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AmountText(payment.amountInCents)
            IconButton(onClick = { onMarkPaid(payment.id) }) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = stringResource(R.string.action_mark_paid))
            }
        }
    }
}

@Composable
private fun RecurringOccurrenceRow(
    occurrence: RecurringOccurrence,
    onMarkPaid: (RecurringOccurrence) -> Unit
) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(occurrence.title, fontWeight = FontWeight.SemiBold)
                Text(occurrence.dueDate.shortDate(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AmountText(occurrence.amountInCents)
            OutlinedButton(onClick = { onMarkPaid(occurrence) }) {
                Text(stringResource(R.string.action_pay))
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun EmptyText(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )
    }
}
