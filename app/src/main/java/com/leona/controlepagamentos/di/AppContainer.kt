package com.leona.controlepagamentos.di

import android.content.Context
import com.leona.controlepagamentos.data.db.AppDatabase
import com.leona.controlepagamentos.data.preferences.SettingsDataStore
import com.leona.controlepagamentos.data.repository.PaymentRepository
import com.leona.controlepagamentos.domain.budget.BudgetService
import com.leona.controlepagamentos.domain.capture.DuplicateDetectionService
import com.leona.controlepagamentos.domain.capture.GenericNotificationParser
import com.leona.controlepagamentos.domain.capture.NotificationCaptureProcessor
import com.leona.controlepagamentos.domain.backup.BackupJsonExporter
import com.leona.controlepagamentos.domain.category.CategorySuggestionService
import com.leona.controlepagamentos.domain.installment.InstallmentService
import com.leona.controlepagamentos.domain.recurrence.RecurrenceService

class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.create(context)
    val settingsDataStore = SettingsDataStore(context)
    val recurrenceService = RecurrenceService()
    val budgetService = BudgetService()

    private val installmentService = InstallmentService()
    private val backupJsonExporter = BackupJsonExporter()
    private val parser = GenericNotificationParser()
    private val categorySuggestionService = CategorySuggestionService()
    private val duplicateDetectionService = DuplicateDetectionService()

    val repository = PaymentRepository(
        paymentDao = database.paymentDao(),
        budgetDao = database.budgetDao(),
        capturedTransactionDao = database.capturedTransactionDao(),
        categoryDao = database.categoryDao(),
        notificationSourceDao = database.notificationSourceDao(),
        recurringPaymentRuleDao = database.recurringPaymentRuleDao(),
        installmentGroupDao = database.installmentGroupDao(),
        installmentService = installmentService,
        backupJsonExporter = backupJsonExporter
    )

    val captureProcessor = NotificationCaptureProcessor(
        sourceDao = database.notificationSourceDao(),
        capturedTransactionDao = database.capturedTransactionDao(),
        parser = parser,
        categorySuggestionService = categorySuggestionService,
        duplicateDetectionService = duplicateDetectionService
    )
}
