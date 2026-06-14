package com.leona.controlepagamentos.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leona.controlepagamentos.data.model.NotificationSourceEntity
import com.leona.controlepagamentos.ui.viewmodel.PaymentsUiState

@Composable
fun SettingsScreen(
    uiState: PaymentsUiState,
    onCaptureEnabled: (Boolean) -> Unit,
    onSourceEnabled: (NotificationSourceEntity, Boolean) -> Unit,
    onAddSource: (String, String) -> Unit,
    onExportJson: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSourceDialog by rememberSaveable { mutableStateOf(false) }
    val permissionEnabled = isNotificationListenerEnabled(context)

    Column(modifier = modifier) {
        ImmersiveHeader(title = "Ajustes")
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 72.dp)
        ) {
        item { SectionTitle("Captura") }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Captura habilitada", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (uiState.settings.captureEnabled) "Ativa" else "Inativa",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.settings.captureEnabled,
                            onCheckedChange = onCaptureEnabled
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Permissao do Android", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (permissionEnabled) "Concedida" else "Pendente",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                            Text("Abrir")
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Apps monitorados")
                FilledTonalButton(onClick = { showSourceDialog = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text("Adicionar")
                }
            }
        }

        items(uiState.sources, key = { it.packageName }) { source ->
            SourceRow(source = source, onEnabled = { enabled -> onSourceEnabled(source, enabled) })
        }

        item { SectionTitle("Backup") }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Exportacao JSON", fontWeight = FontWeight.SemiBold)
                        Text("Backup local", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(onClick = onExportJson) {
                        Text("Exportar")
                    }
                }
            }
        }
        }  // LazyColumn
    }  // Column

    if (showSourceDialog) {
        AddSourceDialog(
            onDismiss = { showSourceDialog = false },
            onAdd = { packageName, appName ->
                onAddSource(packageName, appName)
                showSourceDialog = false
            }
        )
    }
}

@Composable
private fun SourceRow(
    source: NotificationSourceEntity,
    onEnabled: (Boolean) -> Unit
) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(source.appName, fontWeight = FontWeight.SemiBold)
                Text(source.packageName, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = source.isEnabled, onCheckedChange = onEnabled)
        }
    }
}

@Composable
private fun AddSourceDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var packageName by rememberSaveable { mutableStateOf("") }
    var appName by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar app") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Pacote") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(packageName, appName) }) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    ).orEmpty()
    return enabledListeners.contains(context.packageName, ignoreCase = true)
}
