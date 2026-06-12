package com.leona.controlepagamentos.data.model

import java.time.LocalDateTime

object SeedData {
    fun defaultCategories(now: LocalDateTime): List<CategoryEntity> = listOf(
        CategoryEntity("moradia", "Moradia", "#0F766E", "home", true, now),
        CategoryEntity("alimentacao", "Alimentacao", "#DC2626", "restaurant", true, now),
        CategoryEntity("transporte", "Transporte", "#2563EB", "directions_car", true, now),
        CategoryEntity("saude", "Saude", "#16A34A", "local_hospital", true, now),
        CategoryEntity("educacao", "Educacao", "#7C3AED", "school", true, now),
        CategoryEntity("assinaturas", "Assinaturas", "#9333EA", "subscriptions", true, now),
        CategoryEntity("lazer", "Lazer", "#EA580C", "sports_esports", true, now),
        CategoryEntity("mercado", "Mercado", "#0891B2", "shopping_cart", true, now),
        CategoryEntity("pets", "Pets", "#DB2777", "pets", true, now),
        CategoryEntity("investimentos", "Investimentos", "#65A30D", "trending_up", true, now),
        CategoryEntity("impostos", "Impostos", "#475569", "receipt_long", true, now),
        CategoryEntity("outros", "Outros", "#64748B", "category", true, now)
    )

    fun defaultNotificationSources(now: LocalDateTime): List<NotificationSourceEntity> = listOf(
        NotificationSourceEntity("com.nu.production", "Nubank", false, now, now),
        NotificationSourceEntity("br.com.bradesco", "Bradesco", false, now, now),
        NotificationSourceEntity("com.picpay", "PicPay", false, now, now),
        NotificationSourceEntity("br.com.intermedium", "Inter", false, now, now)
    )
}
