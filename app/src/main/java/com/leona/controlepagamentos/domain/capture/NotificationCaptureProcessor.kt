package com.leona.controlepagamentos.domain.capture

import com.leona.controlepagamentos.data.dao.CapturedTransactionDao
import com.leona.controlepagamentos.data.dao.NotificationSourceDao
import com.leona.controlepagamentos.data.model.CaptureStatus
import com.leona.controlepagamentos.data.model.CapturedTransactionEntity
import com.leona.controlepagamentos.data.model.ParseConfidence
import com.leona.controlepagamentos.domain.category.CategorySuggestionService
import java.time.LocalDateTime
import java.util.UUID

class NotificationCaptureProcessor(
    private val sourceDao: NotificationSourceDao,
    private val capturedTransactionDao: CapturedTransactionDao,
    private val parser: NotificationParser,
    private val categorySuggestionService: CategorySuggestionService,
    private val duplicateDetectionService: DuplicateDetectionService
) {
    suspend fun handle(
        sourcePackage: String,
        sourceAppName: String?,
        text: NotificationText,
        occurredAt: LocalDateTime,
        capturedAt: LocalDateTime = LocalDateTime.now()
    ) {
        val source = sourceDao.getByPackage(sourcePackage)
        if (source?.isEnabled != true) return
        if (!parser.canParse(sourcePackage, text)) return

        val parseResult = parser.parse(text)
        val hash = duplicateDetectionService.hash(
            sourcePackage = sourcePackage,
            title = text.title,
            text = text.text ?: text.bigText,
            amountInCents = parseResult.amountInCents,
            merchant = parseResult.merchant,
            occurredAt = occurredAt
        )
        if (capturedTransactionDao.countByHash(hash) > 0) return

        val rawText = text.combined()
        val status = if (parseResult.amountInCents == null) {
            CaptureStatus.PARSE_FAILED
        } else {
            CaptureStatus.PENDING_REVIEW
        }

        capturedTransactionDao.insert(
            CapturedTransactionEntity(
                id = UUID.randomUUID().toString(),
                sourcePackage = sourcePackage,
                sourceAppName = sourceAppName ?: source.appName,
                rawTitle = text.title,
                rawText = text.text,
                rawSubText = text.subText,
                rawBigText = text.bigText,
                amountInCents = parseResult.amountInCents,
                merchant = parseResult.merchant,
                transactionType = parseResult.transactionType,
                occurredAt = occurredAt,
                capturedAt = capturedAt,
                suggestedCategoryId = categorySuggestionService.suggestCategoryId(
                    merchant = parseResult.merchant,
                    rawText = rawText
                ),
                confidence = if (parseResult.amountInCents == null) {
                    ParseConfidence.FAILED
                } else {
                    parseResult.confidence
                },
                status = status,
                notificationHash = hash
            )
        )
    }
}
