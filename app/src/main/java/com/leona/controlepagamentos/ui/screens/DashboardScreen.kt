package com.leona.controlepagamentos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.leona.controlepagamentos.ui.components.ImmersiveHeader
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import com.leona.controlepagamentos.ui.theme.Alert
import com.leona.controlepagamentos.ui.theme.Attention
import com.leona.controlepagamentos.ui.theme.Success
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.domain.budget.BudgetHealth
import com.leona.controlepagamentos.domain.budget.BudgetProgress
import com.leona.controlepagamentos.domain.money.MoneyFormatter
import com.leona.controlepagamentos.domain.recurrence.RecurringOccurrence
import com.leona.controlepagamentos.ui.components.AmountText
import com.leona.controlepagamentos.ui.components.shortDate
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
    Column(modifier = modifier) {
        ImmersiveHeader(title = "Início") {
            MonthHeader(
                label = uiState.monthLabel,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, top = 16.dp, bottom = 72.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        SummaryTile("Previsto", uiState.summary.plannedInCents, Modifier.weight(1f))
                        SummaryTile("Pago", uiState.summary.paidInCents, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        SummaryTile("Pendente", uiState.summary.pendingInCents, Modifier.weight(1f))
                        SummaryTile("Vencido", uiState.summary.overdueInCents, Modifier.weight(1f))
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
            item {
                SectionTitle("Proximos vencimentos")
            }
            if (uiState.upcomingPayments.isEmpty()) {
                item { EmptyText("Nada pendente nos proximos dias.") }
            } else {
                items(uiState.upcomingPayments, key = { it.id }) { payment ->
                    UpcomingPaymentRow(payment = payment, onMarkPaid = onMarkPaid)
                }
            }
            item {
                SectionTitle("Recorrencias previstas")
            }
            if (uiState.recurringOccurrences.isEmpty()) {
                item { EmptyText("Sem recorrencias previstas para este mes.") }
            } else {
                items(uiState.recurringOccurrences, key = { it.ruleId + it.dueDate }) { occurrence ->
                    RecurringOccurrenceRow(occurrence = occurrence, onMarkPaid = onMarkRecurringPaid)
                }
            }
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
                    Text("Teto mensal", style = MaterialTheme.typography.labelLarge)
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
                "${MoneyFormatter.format(budget.spentInCents)} de ${MoneyFormatter.format(budget.limitInCents)}",
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
            Icon(Icons.Outlined.ChevronLeft, contentDescription = "Mes anterior")
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Proximo mes")
        }
    }
}

@Composable
private fun SummaryTile(
    title: String,
    amountInCents: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                MoneyFormatter.format(amountInCents),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
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
                Text("Capturas pendentes", style = MaterialTheme.typography.labelLarge)
                Text("$count aguardando revisao", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
                Icon(Icons.Outlined.CheckCircle, contentDescription = "Marcar pago")
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
                Text("Pagar")
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
