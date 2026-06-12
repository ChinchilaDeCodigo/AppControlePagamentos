package com.leona.controlepagamentos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leona.controlepagamentos.data.model.CapturedTransactionEntity
import com.leona.controlepagamentos.data.model.CategoryEntity
import com.leona.controlepagamentos.domain.money.MoneyFormatter
import com.leona.controlepagamentos.ui.components.CategorySelector
import com.leona.controlepagamentos.ui.components.label
import com.leona.controlepagamentos.ui.viewmodel.PaymentsUiState

@Composable
fun CapturesScreen(
    uiState: PaymentsUiState,
    onConfirm: (CapturedTransactionEntity, String, String, String?, String?) -> Unit,
    onIgnore: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingCapture by rememberSaveable { mutableStateOf<String?>(null) }
    val captureToEdit = uiState.pendingCaptures.firstOrNull { it.id == editingCapture }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 72.dp)
    ) {
        item { SectionTitle("Capturas pendentes") }
        if (uiState.pendingCaptures.isEmpty()) {
            item { EmptyText("Nenhuma captura aguardando revisao.") }
        } else {
            items(uiState.pendingCaptures, key = { it.id }) { capture ->
                CaptureRow(
                    capture = capture,
                    categories = uiState.categories,
                    onEdit = { editingCapture = capture.id },
                    onConfirm = {
                        onConfirm(
                            capture,
                            capture.merchant.orEmpty(),
                            capture.amountInCents?.let(MoneyFormatter::format).orEmpty(),
                            capture.suggestedCategoryId,
                            null
                        )
                    },
                    onIgnore = { onIgnore(capture.id) }
                )
            }
        }
    }

    if (captureToEdit != null) {
        CaptureReviewDialog(
            capture = captureToEdit,
            categories = uiState.categories,
            onDismiss = { editingCapture = null },
            onConfirm = { title, amount, categoryId, notes ->
                onConfirm(captureToEdit, title, amount, categoryId, notes)
                editingCapture = null
            }
        )
    }
}

@Composable
private fun CaptureRow(
    capture: CapturedTransactionEntity,
    categories: List<CategoryEntity>,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    onIgnore: () -> Unit
) {
    val categoryName = categories.firstOrNull { it.id == capture.suggestedCategoryId }?.name ?: "Sem categoria"

    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        capture.merchant ?: "Captura sem estabelecimento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        capture.sourceAppName ?: capture.sourcePackage,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    capture.amountInCents?.let(MoneyFormatter::format) ?: "--",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                "Categoria: $categoryName - Confianca: ${capture.confidence.label()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilledTonalButton(onClick = onConfirm) {
                        Icon(Icons.Outlined.Check, contentDescription = null)
                        Text("Confirmar")
                    }
                }
                item {
                    OutlinedButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = null)
                        Text("Editar")
                    }
                }
                item {
                    OutlinedButton(onClick = onIgnore) {
                        Icon(Icons.Outlined.VisibilityOff, contentDescription = null)
                        Text("Ignorar")
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureReviewDialog(
    capture: CapturedTransactionEntity,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?, String?) -> Unit
) {
    var title by rememberSaveable(capture.id) { mutableStateOf(capture.merchant.orEmpty()) }
    var amount by rememberSaveable(capture.id) {
        mutableStateOf(capture.amountInCents?.let(MoneyFormatter::format).orEmpty())
    }
    var selectedCategory by rememberSaveable(capture.id) { mutableStateOf(capture.suggestedCategoryId) }
    var notes by rememberSaveable(capture.id) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Revisar captura") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Estabelecimento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Valor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                CategorySelector(categories, selectedCategory, { selectedCategory = it })
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observacoes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, amount, selectedCategory, notes) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
