package com.leona.controlepagamentos.ui.components

import com.leona.controlepagamentos.data.model.CaptureStatus
import com.leona.controlepagamentos.data.model.ParseConfidence
import com.leona.controlepagamentos.data.model.PaymentMethod
import com.leona.controlepagamentos.data.model.PaymentStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")

fun LocalDate.shortDate(): String = format(dateFormatter)

fun PaymentStatus.label(): String = when (this) {
    PaymentStatus.PENDING -> "Pendente"
    PaymentStatus.PAID -> "Pago"
    PaymentStatus.OVERDUE -> "Vencido"
    PaymentStatus.CANCELLED -> "Cancelado"
    PaymentStatus.IGNORED -> "Ignorado"
}

fun CaptureStatus.label(): String = when (this) {
    CaptureStatus.PENDING_REVIEW -> "Pendente"
    CaptureStatus.CONFIRMED -> "Confirmado"
    CaptureStatus.IGNORED -> "Ignorado"
    CaptureStatus.DUPLICATE -> "Duplicado"
    CaptureStatus.PARSE_FAILED -> "Falhou"
}

fun ParseConfidence.label(): String = when (this) {
    ParseConfidence.HIGH -> "Alta"
    ParseConfidence.MEDIUM -> "Média"
    ParseConfidence.LOW -> "Baixa"
    ParseConfidence.FAILED -> "Falhou"
}

fun PaymentMethod.label(): String = when (this) {
    PaymentMethod.CASH -> "Dinheiro"
    PaymentMethod.CREDIT_CARD -> "Crédito"
    PaymentMethod.DEBIT_CARD -> "Débito"
    PaymentMethod.PIX -> "Pix"
    PaymentMethod.BANK_SLIP -> "Boleto"
    PaymentMethod.TRANSFER -> "Transferência"
    PaymentMethod.OTHER -> "Outro"
}
