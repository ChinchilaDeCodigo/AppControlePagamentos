package com.leona.controlepagamentos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.leona.controlepagamentos.data.model.BudgetEntity
import com.leona.controlepagamentos.data.model.CapturedTransactionEntity
import com.leona.controlepagamentos.data.model.CategoryEntity
import com.leona.controlepagamentos.data.model.NotificationSourceEntity
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.data.model.PaymentMethod
import com.leona.controlepagamentos.data.model.PaymentStatus
import com.leona.controlepagamentos.data.preferences.AppSettings
import com.leona.controlepagamentos.data.preferences.SettingsDataStore
import com.leona.controlepagamentos.data.repository.PaymentRepository
import com.leona.controlepagamentos.domain.budget.BudgetProgress
import com.leona.controlepagamentos.domain.budget.BudgetService
import com.leona.controlepagamentos.domain.budget.SpendingInsight
import com.leona.controlepagamentos.domain.money.MoneyFormatter
import com.leona.controlepagamentos.domain.recurrence.RecurrenceService
import com.leona.controlepagamentos.domain.recurrence.RecurringOccurrence
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PaymentFilter {
    ALL,
    PENDING,
    PAID,
    OVERDUE
}

data class DashboardSummary(
    val plannedInCents: Long = 0,
    val paidInCents: Long = 0,
    val pendingInCents: Long = 0,
    val overdueInCents: Long = 0,
    val pendingCaptureCount: Int = 0
)

data class CategoryTotal(
    val categoryId: String?,
    val categoryName: String,
    val amountInCents: Long
)

data class PaymentsUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val monthLabel: String = "",
    val payments: List<PaymentEntity> = emptyList(),
    val recurringOccurrences: List<RecurringOccurrence> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val pendingCaptures: List<CapturedTransactionEntity> = emptyList(),
    val sources: List<NotificationSourceEntity> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val filter: PaymentFilter = PaymentFilter.ALL,
    val summary: DashboardSummary = DashboardSummary(),
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val budgetProgress: List<BudgetProgress> = emptyList(),
    val spendingInsight: SpendingInsight = SpendingInsight(
        paidInCents = 0,
        capturedInCents = 0,
        manualInCents = 0,
        averageDailyInCents = 0,
        projectedMonthInCents = 0,
        topCategoryName = null,
        topCategoryInCents = 0,
        topCategorySharePercent = 0
    ),
    val upcomingPayments: List<PaymentEntity> = emptyList(),
    val errorMessage: String? = null
)

private data class BaseState(
    val selectedMonth: YearMonth,
    val payments: List<PaymentEntity>,
    val budgets: List<BudgetEntity>,
    val recurringOccurrences: List<RecurringOccurrence>,
    val categories: List<CategoryEntity>,
    val pendingCaptures: List<CapturedTransactionEntity>,
    val sources: List<NotificationSourceEntity>
)

private data class ObservedLists(
    val payments: List<PaymentEntity>,
    val budgets: List<BudgetEntity>,
    val pendingCaptures: List<CapturedTransactionEntity>,
    val categories: List<CategoryEntity>,
    val sources: List<NotificationSourceEntity>
)

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentsViewModel(
    private val repository: PaymentRepository,
    private val settingsDataStore: SettingsDataStore,
    private val recurrenceService: RecurrenceService,
    private val budgetService: BudgetService
) : ViewModel() {
    private val brazilianPortuguese = Locale.forLanguageTag("pt-BR")
    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val paymentFilter = MutableStateFlow(PaymentFilter.ALL)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState = selectedMonth
        .flatMapLatest { month ->
            combine(
                repository.observePayments(month),
                repository.observeBudgets(month),
                repository.observePendingCaptures(),
                repository.observeCategories(),
                repository.observeSources()
            ) { payments, budgets, captures, categories, sources ->
                ObservedLists(
                    payments = payments,
                    budgets = budgets,
                    pendingCaptures = captures,
                    categories = categories,
                    sources = sources
                )
            }.combine(repository.observeRecurringRules()) { observed, rules ->
                val materializedRecurring = observed.payments
                    .filter { it.source.name == "RECURRING_RULE" }
                    .map { it.title to it.dueDate }
                    .toSet()
                val occurrences = rules
                    .mapNotNull { recurrenceService.occurrenceForMonth(it, month) }
                    .filterNot { it.title to it.dueDate in materializedRecurring }

                BaseState(
                    selectedMonth = month,
                    payments = observed.payments,
                    budgets = observed.budgets,
                    recurringOccurrences = occurrences,
                    categories = observed.categories,
                    pendingCaptures = observed.pendingCaptures,
                    sources = observed.sources
                )
            }
        }
        .combine(settingsDataStore.settings) { base, settings ->
            base to settings
        }
        .combine(paymentFilter) { pair, filter ->
            Triple(pair.first, pair.second, filter)
        }
        .combine(errorMessage) { triple, error ->
            val base = triple.first
            val settings = triple.second
            val filter = triple.third
            val today = LocalDate.now()
            val categoryNames = base.categories.associate { it.id to it.name }
            val summary = buildSummary(base.payments, base.recurringOccurrences, base.pendingCaptures.size, today)
            val budgetProgress = budgetService.buildProgress(
                budgets = base.budgets,
                payments = base.payments,
                categoryNames = categoryNames
            )
            val spendingInsight = budgetService.buildInsight(
                payments = base.payments,
                categoryNames = categoryNames,
                selectedMonth = base.selectedMonth,
                today = today
            )
            val upcoming = base.payments
                .filter { it.status == PaymentStatus.PENDING && !it.dueDate.isBefore(today) }
                .sortedBy { it.dueDate }
                .take(7)

            PaymentsUiState(
                selectedMonth = base.selectedMonth,
                monthLabel = base.selectedMonth.format(DateTimeFormatter.ofPattern("MMMM/yyyy", brazilianPortuguese))
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(brazilianPortuguese) else it.toString() },
                payments = filterPayments(base.payments, filter, today),
                recurringOccurrences = base.recurringOccurrences,
                categories = base.categories,
                pendingCaptures = base.pendingCaptures,
                sources = base.sources,
                settings = settings,
                filter = filter,
                summary = summary,
                categoryTotals = buildCategoryTotals(base.payments, categoryNames),
                budgetProgress = budgetProgress,
                spendingInsight = spendingInsight,
                upcomingPayments = upcoming,
                errorMessage = error
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PaymentsUiState())

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
        }
    }

    fun previousMonth() {
        selectedMonth.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        selectedMonth.update { it.plusMonths(1) }
    }

    fun setFilter(filter: PaymentFilter) {
        paymentFilter.value = filter
    }

    fun addSinglePayment(
        title: String,
        amount: String,
        dueDate: String,
        categoryId: String?,
        method: PaymentMethod?,
        notes: String?
    ) = runValidated {
        repository.addSinglePayment(
            title = title.requireFilled("Informe o titulo."),
            amountInCents = amount.requireMoney(),
            dueDate = dueDate.requireDate(),
            categoryId = categoryId,
            paymentMethod = method,
            notes = notes
        )
    }

    fun addInstallmentPayment(
        title: String,
        totalAmount: String,
        totalInstallments: String,
        firstDueDate: String,
        categoryId: String?,
        method: PaymentMethod?,
        notes: String?
    ) = runValidated {
        repository.addInstallmentGroup(
            title = title.requireFilled("Informe o titulo."),
            totalAmountInCents = totalAmount.requireMoney(),
            totalInstallments = totalInstallments.toIntOrNull()?.takeIf { it > 0 }
                ?: error("Informe uma quantidade de parcelas valida."),
            firstDueDate = firstDueDate.requireDate(),
            categoryId = categoryId,
            paymentMethod = method,
            notes = notes
        )
    }

    fun addRecurringPayment(
        title: String,
        amount: String,
        dayOfMonth: String,
        startDate: String,
        categoryId: String?,
        method: PaymentMethod?,
        notes: String?
    ) = runValidated {
        repository.addRecurringRule(
            title = title.requireFilled("Informe o titulo."),
            amountInCents = amount.requireMoney(),
            dayOfMonth = dayOfMonth.toIntOrNull()?.takeIf { it in 1..31 }
                ?: error("Informe um dia entre 1 e 31."),
            startDate = startDate.requireDate(),
            categoryId = categoryId,
            paymentMethod = method,
            notes = notes
        )
    }

    fun addBudget(
        name: String,
        amount: String,
        categoryId: String?,
        alertAtPercent: String
    ) = runValidated {
        repository.addBudget(
            name = name.requireFilled("Informe o nome do teto."),
            month = selectedMonth.value,
            categoryId = categoryId,
            limitInCents = amount.requireMoney(),
            alertAtPercent = alertAtPercent.toIntOrNull()?.takeIf { it in 1..100 }
                ?: error("Informe um alerta entre 1 e 100.")
        )
    }

    fun deactivateBudget(budgetId: String) {
        viewModelScope.launch { repository.deactivateBudget(budgetId) }
    }

    fun markPaid(paymentId: String) {
        viewModelScope.launch { repository.markPaid(paymentId) }
    }

    fun markRecurringPaid(occurrence: RecurringOccurrence) {
        viewModelScope.launch { repository.markRecurringOccurrencePaid(occurrence) }
    }

    fun ignoreCapture(captureId: String) {
        viewModelScope.launch { repository.ignoreCapture(captureId) }
    }

    fun confirmCapture(
        capture: CapturedTransactionEntity,
        title: String,
        amount: String,
        categoryId: String?,
        notes: String?
    ) = runValidated {
        repository.confirmCapture(
            captureId = capture.id,
            title = title.ifBlank { capture.merchant.orEmpty() },
            amountInCents = amount.ifBlank { capture.amountInCents?.let(MoneyFormatter::format).orEmpty() }.requireMoney(),
            categoryId = categoryId,
            notes = notes
        )
    }

    fun setSourceEnabled(source: NotificationSourceEntity, enabled: Boolean) {
        viewModelScope.launch { repository.setSourceEnabled(source, enabled) }
    }

    fun addSource(packageName: String, appName: String) = runValidated {
        repository.upsertSource(
            packageName = packageName.requireFilled("Informe o pacote do app."),
            appName = appName.ifBlank { packageName },
            enabled = true
        )
    }

    fun setCaptureEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setCaptureEnabled(enabled) }
    }

    fun exportBackup(onExported: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.exportBackupJson() }
                .onSuccess {
                    onExported(it)
                    errorMessage.value = "Backup JSON copiado."
                }
                .onFailure { errorMessage.value = it.message ?: "Nao foi possivel exportar." }
        }
    }

    fun dismissError() {
        errorMessage.value = null
    }

    private fun runValidated(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { errorMessage.value = it.message ?: "Nao foi possivel salvar." }
        }
    }

    private fun String.requireFilled(message: String): String =
        trim().takeIf { it.isNotBlank() } ?: error(message)

    private fun String.requireMoney(): Long =
        MoneyFormatter.parseToCents(this)?.takeIf { it > 0 } ?: error("Informe um valor valido.")

    private fun String.requireDate(): LocalDate =
        runCatching { LocalDate.parse(trim()) }.getOrElse { error("Use data no formato AAAA-MM-DD.") }

    private fun buildSummary(
        payments: List<PaymentEntity>,
        recurringOccurrences: List<RecurringOccurrence>,
        pendingCaptureCount: Int,
        today: LocalDate
    ): DashboardSummary {
        val planned = payments.sumOf { it.amountInCents } + recurringOccurrences.sumOf { it.amountInCents }
        val paid = payments.filter { it.status == PaymentStatus.PAID }.sumOf { it.amountInCents }
        val pending = payments.filter { it.status == PaymentStatus.PENDING && !it.dueDate.isBefore(today) }
            .sumOf { it.amountInCents } + recurringOccurrences.sumOf { it.amountInCents }
        val overdue = payments.filter { it.status == PaymentStatus.PENDING && it.dueDate.isBefore(today) }
            .sumOf { it.amountInCents }

        return DashboardSummary(
            plannedInCents = planned,
            paidInCents = paid,
            pendingInCents = pending,
            overdueInCents = overdue,
            pendingCaptureCount = pendingCaptureCount
        )
    }

    private fun filterPayments(payments: List<PaymentEntity>, filter: PaymentFilter, today: LocalDate): List<PaymentEntity> =
        when (filter) {
            PaymentFilter.ALL -> payments
            PaymentFilter.PENDING -> payments.filter { it.status == PaymentStatus.PENDING && !it.dueDate.isBefore(today) }
            PaymentFilter.PAID -> payments.filter { it.status == PaymentStatus.PAID }
            PaymentFilter.OVERDUE -> payments.filter { it.status == PaymentStatus.PENDING && it.dueDate.isBefore(today) }
        }

    private fun buildCategoryTotals(
        payments: List<PaymentEntity>,
        categoryNames: Map<String, String>
    ): List<CategoryTotal> =
        payments
            .filter { it.status == PaymentStatus.PAID }
            .groupBy { it.categoryId }
            .map { (categoryId, items) ->
                CategoryTotal(
                    categoryId = categoryId,
                    categoryName = categoryId?.let { categoryNames[it] } ?: "Sem categoria",
                    amountInCents = items.sumOf { it.amountInCents }
                )
            }
            .sortedByDescending { it.amountInCents }

    class Factory(
        private val repository: PaymentRepository,
        private val settingsDataStore: SettingsDataStore,
        private val recurrenceService: RecurrenceService,
        private val budgetService: BudgetService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PaymentsViewModel(repository, settingsDataStore, recurrenceService, budgetService) as T
    }
}
