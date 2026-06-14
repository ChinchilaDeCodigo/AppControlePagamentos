package com.leona.controlepagamentos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leona.controlepagamentos.R
import com.leona.controlepagamentos.data.model.CategoryEntity
import com.leona.controlepagamentos.domain.budget.BudgetHealth
import com.leona.controlepagamentos.domain.budget.BudgetProgress
import com.leona.controlepagamentos.domain.money.MoneyFormatter
import com.leona.controlepagamentos.ui.components.CategorySelector
import com.leona.controlepagamentos.ui.components.ImmersiveHeader
import com.leona.controlepagamentos.ui.theme.Alert
import com.leona.controlepagamentos.ui.theme.Attention
import com.leona.controlepagamentos.ui.theme.Success
import com.leona.controlepagamentos.ui.viewmodel.CategoryTotal
import com.leona.controlepagamentos.ui.viewmodel.PaymentsUiState

@Composable
fun ReportsScreen(
    uiState: PaymentsUiState,
    onAddBudget: (String, String, String?, String) -> Unit,
    onDeactivateBudget: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBudgetDialog by rememberSaveable { mutableStateOf(false) }
    val maxAmount = uiState.categoryTotals.maxOfOrNull { it.amountInCents }?.takeIf { it > 0 } ?: 1L

    Column(modifier = modifier) {
        ImmersiveHeader(title = stringResource(R.string.screen_reports))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 72.dp)
        ) {
        item { SectionTitle(stringResource(R.string.section_monthly_summary)) }
        item { InsightGrid(uiState) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(stringResource(R.string.section_spending_caps))
                FilledTonalButton(onClick = { showBudgetDialog = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text(stringResource(R.string.action_new))
                }
            }
        }
        if (uiState.budgetProgress.isEmpty()) {
            item { EmptyText(stringResource(R.string.empty_budgets)) }
        } else {
            items(uiState.budgetProgress, key = { it.budgetId }) { budget ->
                BudgetProgressRow(
                    budget = budget,
                    onDeactivate = { onDeactivateBudget(budget.budgetId) }
                )
            }
        }

        item { SectionTitle(stringResource(R.string.section_spending_by_category)) }
        if (uiState.categoryTotals.isEmpty()) {
            item { EmptyText(stringResource(R.string.empty_category_totals)) }
        } else {
            items(uiState.categoryTotals, key = { it.categoryId ?: "none" }) { total ->
                CategoryTotalRow(total = total, maxAmount = maxAmount)
            }
        }
        }  // LazyColumn
    }  // Column

    if (showBudgetDialog) {
        AddBudgetDialog(
            categories = uiState.categories,
            onDismiss = { showBudgetDialog = false },
            onSave = { name, amount, categoryId, alertAt ->
                onAddBudget(name, amount, categoryId, alertAt)
                showBudgetDialog = false
            }
        )
    }
}

@Composable
private fun InsightGrid(uiState: PaymentsUiState) {
    val insight = uiState.spendingInsight
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            InsightTile(stringResource(R.string.label_paid), MoneyFormatter.format(insight.paidInCents), Modifier.weight(1f))
            InsightTile(stringResource(R.string.insight_daily_average), MoneyFormatter.format(insight.averageDailyInCents), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            InsightTile(stringResource(R.string.insight_projection), MoneyFormatter.format(insight.projectedMonthInCents), Modifier.weight(1f))
            InsightTile(
                stringResource(R.string.insight_top_category),
                insight.topCategoryName?.let {
                    stringResource(R.string.label_top_category_with_share, it, insight.topCategorySharePercent)
                } ?: stringResource(R.string.label_no_data),
                Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            InsightTile(stringResource(R.string.label_captured), MoneyFormatter.format(insight.capturedInCents), Modifier.weight(1f))
            InsightTile(stringResource(R.string.label_manual), MoneyFormatter.format(insight.manualInCents), Modifier.weight(1f))
        }
    }
}

@Composable
private fun InsightTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun BudgetProgressRow(
    budget: BudgetProgress,
    onDeactivate: () -> Unit
) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(budget.name, fontWeight = FontWeight.SemiBold)
                    Text(budget.categoryName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                BudgetHealthBadge(budget.health)
                Text(
                    "${budget.percentUsed}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = budget.health.color()
                )
                IconButton(onClick = onDeactivate) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.action_remove_budget))
                }
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
            Text(
                stringResource(R.string.label_remaining_amount, MoneyFormatter.format(budget.remainingInCents)),
                color = budget.health.color()
            )
        }
    }
}

@Composable
private fun CategoryTotalRow(total: CategoryTotal, maxAmount: Long) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(total.categoryName, fontWeight = FontWeight.SemiBold)
                Text(MoneyFormatter.format(total.amountInCents), style = MaterialTheme.typography.titleSmall)
            }
            LinearProgressIndicator(
                progress = { total.amountInCents.toFloat() / maxAmount.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AddBudgetDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("Teto mensal") }
    var amount by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var alertAt by rememberSaveable { mutableStateOf("75") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_new_budget)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.form_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.form_limit_amount)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = alertAt,
                    onValueChange = { alertAt = it },
                    label = { Text(stringResource(R.string.form_alert_percent)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                CategorySelector(
                    categories = categories,
                    selectedCategoryId = selectedCategory,
                    onSelected = { selectedCategory = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, amount, selectedCategory, alertAt) }) {
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

@Composable
private fun BudgetHealthBadge(health: BudgetHealth) {
    val (color, label) = when (health) {
        BudgetHealth.HEALTHY -> Success to stringResource(R.string.health_healthy)
        BudgetHealth.ATTENTION -> Attention to stringResource(R.string.health_attention)
        BudgetHealth.CRITICAL -> Alert to stringResource(R.string.health_critical)
        BudgetHealth.EXCEEDED -> Alert to stringResource(R.string.health_exceeded)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), shape = RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun BudgetHealth.color() = when (this) {
    BudgetHealth.HEALTHY -> Success
    BudgetHealth.ATTENTION -> Attention
    BudgetHealth.CRITICAL -> Alert
    BudgetHealth.EXCEEDED -> MaterialTheme.colorScheme.error
}
