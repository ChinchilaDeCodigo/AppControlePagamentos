package com.leona.controlepagamentos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.leona.controlepagamentos.R
import com.leona.controlepagamentos.data.model.CapturedTransactionEntity
import com.leona.controlepagamentos.data.model.CategoryEntity
import com.leona.controlepagamentos.data.model.ParseConfidence
import com.leona.controlepagamentos.domain.money.MoneyFormatter
import com.leona.controlepagamentos.ui.components.formatMoney
import com.leona.controlepagamentos.ui.components.shortDateTime
import com.leona.controlepagamentos.ui.components.CategorySelector
import com.leona.controlepagamentos.ui.components.HeaderPill
import com.leona.controlepagamentos.ui.components.ImmersiveHeader
import com.leona.controlepagamentos.ui.theme.Alert
import com.leona.controlepagamentos.ui.theme.Attention
import com.leona.controlepagamentos.ui.theme.Success
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
    val pendingCount = uiState.pendingCaptures.size

    Column(modifier = modifier) {
        ImmersiveHeader(
            title = stringResource(R.string.screen_captures),
            trailing = {
                if (pendingCount > 0) HeaderPill(stringResource(R.string.captures_in_queue, pendingCount))
            }
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 72.dp)
        ) {
            if (uiState.pendingCaptures.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.NotificationsOff,
                        title = stringResource(R.string.empty_captures_title),
                        description = stringResource(R.string.empty_captures_desc)
                    )
                }
            } else {
                items(uiState.pendingCaptures, key = { it.id }) { capture ->
                    ReviewCard(
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
private fun ReviewCard(
    capture: CapturedTransactionEntity,
    categories: List<CategoryEntity>,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    onIgnore: () -> Unit
) {
    val category = categories.firstOrNull { it.id == capture.suggestedCategoryId }
    val categoryColor = category?.colorHex?.let {
        runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
    } ?: MaterialTheme.colorScheme.onSurfaceVariant

    Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = capture.merchant ?: stringResource(R.string.capture_unknown_merchant),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = capture.sourceAppName ?: capture.sourcePackage,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = capture.capturedAt.shortDateTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
                Text(
                    text = formatMoney(capture.amountInCents),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(categoryColor, CircleShape)
                )
                Text(
                    text = category?.name ?: stringResource(R.string.label_no_category),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                ConfidenceBadge(capture.confidence)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onConfirm, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_confirm))
                }
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                TextButton(
                    onClick = onIgnore,
                    colors = ButtonDefaults.textButtonColors(contentColor = Alert)
                ) {
                    Icon(Icons.Outlined.VisibilityOff, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: ParseConfidence) {
    val (color, label) = when (confidence) {
        ParseConfidence.HIGH -> Success to stringResource(R.string.confidence_high)
        ParseConfidence.MEDIUM -> Attention to stringResource(R.string.confidence_medium)
        ParseConfidence.LOW -> Alert to stringResource(R.string.confidence_low)
        ParseConfidence.FAILED -> Alert to stringResource(R.string.confidence_failed)
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
        title = { Text(stringResource(R.string.dialog_review_capture)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.form_merchant)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.form_amount)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                CategorySelector(categories, selectedCategory, { selectedCategory = it })
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.form_notes)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, amount, selectedCategory, notes) }) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
