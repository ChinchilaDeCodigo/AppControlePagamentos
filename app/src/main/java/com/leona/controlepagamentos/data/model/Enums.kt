package com.leona.controlepagamentos.data.model

enum class PaymentType {
    SINGLE,
    RECURRING,
    INSTALLMENT,
    CAPTURED
}

enum class PaymentStatus {
    PENDING,
    PAID,
    OVERDUE,
    CANCELLED,
    IGNORED
}

enum class PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    PIX,
    BANK_SLIP,
    TRANSFER,
    OTHER
}

enum class PaymentSource {
    MANUAL,
    CAPTURED_NOTIFICATION,
    RECURRING_RULE,
    INSTALLMENT_GROUP
}

enum class CaptureStatus {
    PENDING_REVIEW,
    CONFIRMED,
    IGNORED,
    DUPLICATE,
    PARSE_FAILED
}

enum class ParseConfidence {
    HIGH,
    MEDIUM,
    LOW,
    FAILED
}

enum class TransactionType {
    CARD_PURCHASE,
    PIX,
    BANK_TRANSFER,
    BANK_SLIP,
    UNKNOWN
}
