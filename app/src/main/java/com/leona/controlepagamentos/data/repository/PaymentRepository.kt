package com.leona.controlepagamentos.data.repository

import com.leona.controlepagamentos.data.dao.BudgetDao
import com.leona.controlepagamentos.data.dao.CapturedTransactionDao
import com.leona.controlepagamentos.data.dao.CategoryDao
import com.leona.controlepagamentos.data.dao.InstallmentGroupDao
import com.leona.controlepagamentos.data.dao.NotificationSourceDao
import com.leona.controlepagamentos.data.dao.PaymentDao
import com.leona.controlepagamentos.data.dao.RecurringPaymentRuleDao
import com.leona.controlepagamentos.data.model.BudgetEntity
import com.leona.controlepagamentos.data.model.CaptureStatus
import com.leona.controlepagamentos.data.model.CapturedTransactionEntity
import com.leona.controlepagamentos.data.model.InstallmentGroupEntity
import com.leona.controlepagamentos.data.model.NotificationSourceEntity
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.data.model.PaymentMethod
import com.leona.controlepagamentos.data.model.PaymentSource
import com.leona.controlepagamentos.data.model.PaymentStatus
import com.leona.controlepagamentos.data.model.PaymentType
import com.leona.controlepagamentos.data.model.RecurringPaymentRuleEntity
import com.leona.controlepagamentos.data.model.SeedData
import com.leona.controlepagamentos.domain.backup.BackupJsonExporter
import com.leona.controlepagamentos.domain.installment.InstallmentService
import com.leona.controlepagamentos.domain.recurrence.RecurringOccurrence
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val budgetDao: BudgetDao,
    private val capturedTransactionDao: CapturedTransactionDao,
    private val categoryDao: CategoryDao,
    private val notificationSourceDao: NotificationSourceDao,
    private val recurringPaymentRuleDao: RecurringPaymentRuleDao,
    private val installmentGroupDao: InstallmentGroupDao,
    private val installmentService: InstallmentService,
    private val backupJsonExporter: BackupJsonExporter
) {
    fun observePayments(month: YearMonth): Flow<List<PaymentEntity>> =
        paymentDao.observeBetween(month.atDay(1), month.atEndOfMonth())

    fun observeBudgets(month: YearMonth): Flow<List<BudgetEntity>> =
        budgetDao.observeActiveForMonth(month.atDay(1))

    fun observePendingCaptures(): Flow<List<CapturedTransactionEntity>> =
        capturedTransactionDao.observeByStatus(CaptureStatus.PENDING_REVIEW)

    fun observeCategories() = categoryDao.observeAll()

    fun observeSources() = notificationSourceDao.observeAll()

    fun observeRecurringRules() = recurringPaymentRuleDao.observeActive()

    suspend fun ensureSeedData() {
        val now = LocalDateTime.now()
        if (categoryDao.count() == 0) {
            categoryDao.insertAll(SeedData.defaultCategories(now))
        }
        if (notificationSourceDao.count() == 0) {
            notificationSourceDao.insertAll(SeedData.defaultNotificationSources(now))
        }
    }

    suspend fun addSinglePayment(
        title: String,
        amountInCents: Long,
        dueDate: LocalDate,
        categoryId: String?,
        paymentMethod: PaymentMethod?,
        notes: String?
    ) {
        val now = LocalDateTime.now()
        paymentDao.insert(
            PaymentEntity(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                amountInCents = amountInCents,
                categoryId = categoryId,
                dueDate = dueDate,
                paidAt = null,
                status = PaymentStatus.PENDING,
                type = PaymentType.SINGLE,
                paymentMethod = paymentMethod,
                source = PaymentSource.MANUAL,
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                capturedTransactionId = null,
                installmentGroupId = null,
                installmentNumber = null,
                totalInstallments = null,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun addRecurringRule(
        title: String,
        amountInCents: Long,
        dayOfMonth: Int,
        startDate: LocalDate,
        categoryId: String?,
        paymentMethod: PaymentMethod?,
        notes: String?
    ) {
        val now = LocalDateTime.now()
        recurringPaymentRuleDao.insert(
            RecurringPaymentRuleEntity(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                amountInCents = amountInCents,
                categoryId = categoryId,
                dayOfMonth = dayOfMonth.coerceIn(1, 31),
                startDate = startDate,
                endDate = null,
                paymentMethod = paymentMethod,
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun addBudget(
        name: String,
        month: YearMonth,
        categoryId: String?,
        limitInCents: Long,
        alertAtPercent: Int
    ) {
        val now = LocalDateTime.now()
        budgetDao.insert(
            BudgetEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                monthStart = month.atDay(1),
                categoryId = categoryId,
                limitInCents = limitInCents,
                alertAtPercent = alertAtPercent.coerceIn(1, 100),
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun deactivateBudget(budgetId: String) {
        budgetDao.deactivate(budgetId, LocalDateTime.now())
    }

    suspend fun addInstallmentGroup(
        title: String,
        totalAmountInCents: Long,
        totalInstallments: Int,
        firstDueDate: LocalDate,
        categoryId: String?,
        paymentMethod: PaymentMethod?,
        notes: String?
    ) {
        val now = LocalDateTime.now()
        val group = InstallmentGroupEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            totalAmountInCents = totalAmountInCents,
            totalInstallments = totalInstallments.coerceAtLeast(1),
            categoryId = categoryId,
            firstDueDate = firstDueDate,
            paymentMethod = paymentMethod,
            notes = notes?.trim()?.takeIf { it.isNotBlank() },
            createdAt = now
        )
        installmentGroupDao.insert(group)
        paymentDao.insertAll(installmentService.createPayments(group, now))
    }

    suspend fun markPaid(paymentId: String) {
        val now = LocalDateTime.now()
        paymentDao.updateStatus(paymentId, PaymentStatus.PAID, now, now)
    }

    suspend fun markRecurringOccurrencePaid(occurrence: RecurringOccurrence) {
        val now = LocalDateTime.now()
        paymentDao.insert(
            PaymentEntity(
                id = UUID.randomUUID().toString(),
                title = occurrence.title,
                amountInCents = occurrence.amountInCents,
                categoryId = occurrence.categoryId,
                dueDate = occurrence.dueDate,
                paidAt = now,
                status = PaymentStatus.PAID,
                type = PaymentType.RECURRING,
                paymentMethod = occurrence.paymentMethod,
                source = PaymentSource.RECURRING_RULE,
                notes = occurrence.notes,
                capturedTransactionId = null,
                installmentGroupId = null,
                installmentNumber = null,
                totalInstallments = null,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun cancelPayment(paymentId: String) {
        paymentDao.updateStatus(paymentId, PaymentStatus.CANCELLED, null, LocalDateTime.now())
    }

    suspend fun ignoreCapture(captureId: String) {
        capturedTransactionDao.updateStatus(captureId, CaptureStatus.IGNORED)
    }

    suspend fun confirmCapture(
        captureId: String,
        title: String?,
        amountInCents: Long?,
        categoryId: String?,
        notes: String?,
        paymentMethod: PaymentMethod? = null
    ) {
        val capture = capturedTransactionDao.getById(captureId) ?: return
        val confirmedAmount = amountInCents ?: capture.amountInCents ?: return
        val now = LocalDateTime.now()
        val occurredDate = capture.occurredAt.toLocalDate()
        paymentDao.insert(
            PaymentEntity(
                id = UUID.randomUUID().toString(),
                title = title?.trim()?.takeIf { it.isNotBlank() }
                    ?: capture.merchant
                    ?: "Gasto capturado",
                amountInCents = confirmedAmount,
                categoryId = categoryId ?: capture.suggestedCategoryId,
                dueDate = occurredDate,
                paidAt = capture.occurredAt,
                status = PaymentStatus.PAID,
                type = PaymentType.CAPTURED,
                paymentMethod = paymentMethod,
                source = PaymentSource.CAPTURED_NOTIFICATION,
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                capturedTransactionId = capture.id,
                installmentGroupId = null,
                installmentNumber = null,
                totalInstallments = null,
                createdAt = now,
                updatedAt = now
            )
        )
        capturedTransactionDao.updateStatus(captureId, CaptureStatus.CONFIRMED)
    }

    suspend fun updatePayment(payment: PaymentEntity) {
        paymentDao.update(payment.copy(updatedAt = LocalDateTime.now()))
    }

    suspend fun deletePayment(id: String) {
        paymentDao.delete(id)
    }

    suspend fun updateRecurringRule(rule: RecurringPaymentRuleEntity) {
        recurringPaymentRuleDao.update(rule.copy(updatedAt = LocalDateTime.now()))
    }

    suspend fun deleteRecurringRule(id: String) {
        recurringPaymentRuleDao.delete(id)
    }

    suspend fun setSourceEnabled(source: NotificationSourceEntity, enabled: Boolean) {
        notificationSourceDao.update(
            source.copy(isEnabled = enabled, updatedAt = LocalDateTime.now())
        )
    }

    suspend fun upsertSource(packageName: String, appName: String, enabled: Boolean) {
        val now = LocalDateTime.now()
        val existing = notificationSourceDao.getByPackage(packageName)
        notificationSourceDao.upsert(
            NotificationSourceEntity(
                packageName = packageName.trim(),
                appName = appName.trim().ifBlank { packageName.trim() },
                isEnabled = enabled,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
    }

    suspend fun exportBackupJson(): String =
        backupJsonExporter.export(
            payments = paymentDao.getAll(),
            budgets = budgetDao.getAll(),
            categories = categoryDao.getAll(),
            capturedTransactions = capturedTransactionDao.getAll(),
            recurringRules = recurringPaymentRuleDao.getAll(),
            installmentGroups = installmentGroupDao.getAll()
        )
}
