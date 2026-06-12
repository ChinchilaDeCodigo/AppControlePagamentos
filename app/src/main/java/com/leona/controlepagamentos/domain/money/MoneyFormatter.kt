package com.leona.controlepagamentos.domain.money

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object MoneyFormatter {
    private val brCurrency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))

    fun format(cents: Long): String = brCurrency.format(cents / 100.0)

    fun parseToCents(input: String): Long? {
        val normalized = input
            .trim()
            .replace("R$", "", ignoreCase = true)
            .replace(".", "")
            .replace(",", ".")
            .filter { it.isDigit() || it == '.' || it == '-' }

        if (normalized.isBlank() || normalized == "-") return null
        return normalized.toBigDecimalOrNull()
            ?.movePointRight(2)
            ?.setScale(0)
            ?.toLong()
            ?.let { if (abs(it) == 0L) null else it }
    }
}
