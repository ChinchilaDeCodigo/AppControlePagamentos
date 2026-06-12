package com.leona.controlepagamentos.domain

import com.leona.controlepagamentos.domain.category.CategorySuggestionService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CategorySuggestionServiceTest {
    private val service = CategorySuggestionService()

    @Test
    fun suggestsFoodForBakery() {
        assertEquals("alimentacao", service.suggestCategoryId("PADARIA XPTO", null))
    }

    @Test
    fun suggestsTransportForUber() {
        assertEquals("transporte", service.suggestCategoryId("UBER TRIP", null))
    }

    @Test
    fun returnsNullForUnknownMerchant() {
        assertNull(service.suggestCategoryId("LOJA ALEATORIA", null))
    }
}
