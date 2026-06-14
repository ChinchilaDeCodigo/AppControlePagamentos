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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import com.leona.controlepagamentos.ui.theme.Alert
import com.leona.controlepagamentos.ui.theme.Blue
import com.leona.controlepagamentos.ui.theme.TextTertiary
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.stringResource
import com.leona.controlepagamentos.ui.components.LocalPrivacyMode
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leona.controlepagamentos.data.preferences.ThemeMode
import com.leona.controlepagamentos.ui.viewmodel.PaymentsUiState
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
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = when (uiState.settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            ControlePagamentosTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(LocalPrivacyMode provides uiState.settings.privacyMode) {
                    PaymentsApp(viewModel = viewModel, uiState = uiState)
                }
            }
        }
    }
}

private enum class MainTab {
    DASHBOARD,
    PAYMENTS,
    CAPTURES,
    REPORTS,
    SETTINGS
}

@Composable
private fun PaymentsApp(viewModel: PaymentsViewModel, uiState: PaymentsUiState) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.DASHBOARD) }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.dismissError()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    val navColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Blue,
                        selectedTextColor = Blue,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary,
                        indicatorColor = Blue.copy(alpha = 0.12f)
                    )
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            if (tab == MainTab.CAPTURES && uiState.pendingCaptures.isNotEmpty()) {
                                BadgedBox(badge = {
                                    Badge(containerColor = Alert) {
                                        Text(uiState.pendingCaptures.size.toString())
                                    }
                                }) {
                                    Icon(tab.icon(), contentDescription = tab.label())
                                }
                            } else {
                                Icon(tab.icon(), contentDescription = tab.label())
                            }
                        },
                        label = { Text(tab.label(), softWrap = false) },
                        colors = navColors
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        val contentModifier = Modifier
            .padding(padding)
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
                onThemeChanged = viewModel::setThemeMode,
                onPrivacyModeChanged = viewModel::setPrivacyMode,
                modifier = contentModifier
            )
        }
    }
}

@Composable
private fun MainTab.label() = when (this) {
    MainTab.DASHBOARD -> stringResource(R.string.nav_dashboard)
    MainTab.PAYMENTS -> stringResource(R.string.nav_payments)
    MainTab.CAPTURES -> stringResource(R.string.nav_captures)
    MainTab.REPORTS -> stringResource(R.string.nav_reports)
    MainTab.SETTINGS -> stringResource(R.string.nav_settings)
}

@Composable
private fun MainTab.icon() = when (this) {
    MainTab.DASHBOARD -> Icons.Outlined.Home
    MainTab.PAYMENTS -> Icons.AutoMirrored.Outlined.List
    MainTab.CAPTURES -> Icons.Outlined.Notifications
    MainTab.REPORTS -> Icons.Outlined.BarChart
    MainTab.SETTINGS -> Icons.Outlined.Settings
}
