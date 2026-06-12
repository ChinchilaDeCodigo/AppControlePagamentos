package com.leona.controlepagamentos.domain.category

class CategorySuggestionService {
    private val keywords = mapOf(
        "alimentacao" to listOf("PADARIA", "IFOOD", "RESTAURANTE", "LANCHONETE", "CAFE"),
        "transporte" to listOf("UBER", "99", "POSTO", "GASOLINA", "METRO", "ONIBUS"),
        "saude" to listOf("DROGARIA", "FARMACIA", "HOSPITAL", "CLINICA"),
        "assinaturas" to listOf("NETFLIX", "SPOTIFY", "YOUTUBE", "AMAZON PRIME", "DISNEY"),
        "mercado" to listOf("MERCADO", "SUPERMERCADO", "ATACADAO", "CARREFOUR", "ASSAI")
    )

    fun suggestCategoryId(merchant: String?, rawText: String?): String? {
        val haystack = listOfNotNull(merchant, rawText).joinToString(" ").uppercase()
        if (haystack.isBlank()) return null

        return keywords.entries.firstOrNull { (_, words) ->
            words.any { word -> haystack.contains(word) }
        }?.key
    }
}
