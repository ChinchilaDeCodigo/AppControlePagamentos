package com.leona.controlepagamentos.domain.capture

import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class DuplicateDetectionService {
    fun hash(
        sourcePackage: String,
        title: String?,
        text: String?,
        amountInCents: Long?,
        merchant: String?,
        occurredAt: LocalDateTime
    ): String {
        val rounded = occurredAt.truncatedTo(ChronoUnit.MINUTES)
        val raw = listOf(
            sourcePackage,
            title.normalized(),
            text.normalized(),
            amountInCents?.toString().orEmpty(),
            merchant.normalized(),
            rounded.toString()
        ).joinToString("|")
        return sha256(raw)
    }

    private fun String?.normalized(): String = this
        .orEmpty()
        .lowercase()
        .replace(Regex("""\s+"""), " ")
        .trim()

    private fun sha256(raw: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
