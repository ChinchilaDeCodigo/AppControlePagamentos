package com.leona.controlepagamentos.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leona.controlepagamentos.data.model.CategoryEntity
import com.leona.controlepagamentos.domain.budget.BudgetHealth
import com.leona.controlepagamentos.domain.budget.BudgetProgress
import com.leona.controlepagamentos.domain.money.MoneyFormatter
import com.leona.controlepagamentos.ui.components.CategorySelector
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

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 72.dp)
    ) {
        item { SectionTitle("Resumo do mes") }
        item { InsightGrid(uiState) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Tetos de gastos")
                FilledTonalButton(onClick = { showBudgetDialog = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text("Novo")
                }
            }
        }
        if (uiState.budgetProgress.isEmpty()) {
            item { EmptyText("Nenhum teto configurado para este mes.") }
        } else {
            items(uiState.budgetProgress, key = { it.budgetId }) { budget ->
                BudgetProgressRow(
                    budget = budget,
                    onDeactivate = { onDeactivateBudget(budget.budgetId) }
                )
            }
        }

        item { SectionTitle("Gastos por categoria") }
        if (uiState.categoryTotals.isEmpty()) {
            item { EmptyText("Sem pagamentos pagos neste mes.") }
        } else {
            items(uiState.categoryTotals, key = { it.categoryId ?: "none" }) { total ->
                CategoryTotalRow(total = total, maxAmount = maxAmount)
            }
        }
    }

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
            InsightTile("Pago", MoneyFormatter.format(insight.paidInCents), Modifier.weight(1f))
            InsightTile("Media diaria", MoneyFormatter.format(insight.averageDailyInCents), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            InsightTile("Projecao", MoneyFormatter.format(insight.projectedMonthInCents), Modifier.weight(1f))
            InsightTile(
                "Maior categoria",
                insight.topCategoryName?.let { "$it (${insight.topCategorySharePercent}%)" } ?: "Sem dados",
                Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            InsightTile("Capturado", MoneyFormatter.format(insight.capturedInCents), Modifier.weight(1f))
            InsightTile("Manual", MoneyFormatter.format(insight.manualInCents), Modifier.weight(1f))
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
                    Text(
                        "${budget.categoryName} - ${budget.health.label()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${budget.percentUsed}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = budget.health.color()
                )
                IconButton(onClick = onDeactivate) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Remover teto")
                }
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
            Text(
                "Restante: ${MoneyFormatter.format(budget.remainingInCents)}",
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
        title = { Text("Novo teto") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Valor limite") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = alertAt,
                    onValueChange = { alertAt = it },
                    label = { Text("Alertar em %") },
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

private fun BudgetHealth.label(): String = when (this) {
    BudgetHealth.HEALTHY -> "Saudavel"
    BudgetHealth.ATTENTION -> "Atencao"
    BudgetHealth.CRITICAL -> "Critico"
    BudgetHealth.EXCEEDED -> "Estourado"
}

@Composable
private fun BudgetHealth.color() = when (this) {
    BudgetHealth.HEALTHY -> MaterialTheme.colorScheme.primary
    BudgetHealth.ATTENTION -> MaterialTheme.colorScheme.secondary
    BudgetHealth.CRITICAL -> MaterialTheme.colorScheme.tertiary
    BudgetHealth.EXCEEDED -> MaterialTheme.colorScheme.error
}
