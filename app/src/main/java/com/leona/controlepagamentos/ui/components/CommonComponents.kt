package com.leona.controlepagamentos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leona.controlepagamentos.R
import com.leona.controlepagamentos.data.model.CategoryEntity
import com.leona.controlepagamentos.data.model.PaymentMethod
import com.leona.controlepagamentos.domain.money.MoneyFormatter

@Composable
fun AmountText(
    amountInCents: Long,
    modifier: Modifier = Modifier
) {
    Text(
        text = MoneyFormatter.format(amountInCents),
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun CategorySelector(
    categories: List<CategoryEntity>,
    selectedCategoryId: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.form_category),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp),
            modifier = Modifier.heightIn(min = 44.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { onSelected(null) },
                    label = { Text(stringResource(R.string.label_no_category)) }
                )
            }
            items(categories, key = { it.id }) { category ->
                FilterChip(
                    selected = selectedCategoryId == category.id,
                    onClick = { onSelected(category.id) },
                    label = { Text(category.name) }
                )
            }
        }
    }
}

@Composable
fun PaymentMethodSelector(
    selected: PaymentMethod?,
    onSelected: (PaymentMethod?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.form_payment_method),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp),
            modifier = Modifier.heightIn(min = 44.dp)
        ) {
            item {
                AssistChip(
                    onClick = { onSelected(null) },
                    label = {
                        Text(
                            if (selected == null) stringResource(R.string.form_not_defined)
                            else stringResource(R.string.action_clear)
                        )
                    }
                )
            }
            items(PaymentMethod.entries, key = { it.name }) { method ->
                FilterChip(
                    selected = selected == method,
                    onClick = { onSelected(method) },
                    label = { Text(method.label()) }
                )
            }
        }
    }
}
