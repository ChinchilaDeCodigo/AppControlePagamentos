package com.leona.controlepagamentos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leona.controlepagamentos.ui.screens.CapturesScreen
import com.leona.controlepagamentos.ui.screens.DashboardScreen
import com.leona.controlepagamentos.ui.screens.PaymentsScreen
import com.leona.controlepagamentos.ui.screens.ReportsScreen
import com.leona.controlepagamentos.ui.screens.SettingsScreen
import com.leona.controlepagamentos.ui.theme.ControlePagamentosTheme
import com.leona.controlepagamentos.ui.viewmodel.PaymentsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as PaymentsApplication

        setContent {
            val viewModel: PaymentsViewModel = viewModel(
                factory = PaymentsViewModel.Factory(
                    repository = app.container.repository,
                    settingsDataStore = app.container.settingsDataStore,
                    recurrenceService = app.container.recurrenceService,
                    budgetService = app.container.budgetService
                )
            )
            ControlePagamentosTheme {
                PaymentsApp(viewModel = viewModel)
            }
        }
    }
}

private enum class MainTab(val label: String) {
    DASHBOARD("Dashboard"),
    PAYMENTS("Pagamentos"),
    CAPTURES("Capturados"),
    REPORTS("Relatorios"),
    SETTINGS("Ajustes")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentsApp(viewModel: PaymentsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.DASHBOARD) }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.dismissError()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Controle de Pagamentos") })
        },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon(), contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        val contentModifier = Modifier
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxSize()

        when (selectedTab) {
            MainTab.DASHBOARD -> DashboardScreen(
                uiState = uiState,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth,
                onMarkPaid = viewModel::markPaid,
                onMarkRecurringPaid = viewModel::markRecurringPaid,
                modifier = contentModifier
            )
            MainTab.PAYMENTS -> PaymentsScreen(
                uiState = uiState,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth,
                onFilterChanged = viewModel::setFilter,
                onMarkPaid = viewModel::markPaid,
                onMarkRecurringPaid = viewModel::markRecurringPaid,
                onAddSingle = viewModel::addSinglePayment,
                onAddInstallment = viewModel::addInstallmentPayment,
                onAddRecurring = viewModel::addRecurringPayment,
                modifier = contentModifier
            )
            MainTab.CAPTURES -> CapturesScreen(
                uiState = uiState,
                onConfirm = viewModel::confirmCapture,
                onIgnore = viewModel::ignoreCapture,
                modifier = contentModifier
            )
            MainTab.REPORTS -> ReportsScreen(
                uiState = uiState,
                onAddBudget = viewModel::addBudget,
                onDeactivateBudget = viewModel::deactivateBudget,
                modifier = contentModifier
            )
            MainTab.SETTINGS -> SettingsScreen(
                uiState = uiState,
                onCaptureEnabled = viewModel::setCaptureEnabled,
                onSourceEnabled = viewModel::setSourceEnabled,
                onAddSource = viewModel::addSource,
                onExportJson = {
                    viewModel.exportBackup { json ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("controle-pagamentos-backup", json))
                    }
                },
                modifier = contentModifier
            )
        }
    }
}

@Composable
private fun MainTab.icon() = when (this) {
    MainTab.DASHBOARD -> Icons.Outlined.Home
    MainTab.PAYMENTS -> Icons.AutoMirrored.Outlined.List
    MainTab.CAPTURES -> Icons.Outlined.Notifications
    MainTab.REPORTS -> Icons.Outlined.BarChart
    MainTab.SETTINGS -> Icons.Outlined.Settings
}
