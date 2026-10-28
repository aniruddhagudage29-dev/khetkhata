package com.example.model

import androidx.compose.ui.graphics.Color

enum class CropItem(
    val key: String,
    val nameMr: String,
    val nameHi: String,
    val nameEn: String,
    val accentColor: Color,
    val cardTint: Color,
    val iconEmoji: String
) {
    ONION(
        key = "onion",
        nameMr = "कांदा",
        nameHi = "प्याज",
        nameEn = "Onion",
        accentColor = Color(0xFF9D174D),
        cardTint = Color(0xFFFFF1F2),
        iconEmoji = "🧅"
    ),
    SOYBEAN(
        key = "soybean",
        nameMr = "सोयाबीन",
        nameHi = "सोयाबीन",
        nameEn = "Soybean",
        accentColor = Color(0xFF3F6212),
        cardTint = Color(0xFFF7FEE7),
        iconEmoji = "🌱"
    ),
    COTTON(
        key = "cotton",
        nameMr = "कापूस",
        nameHi = "कपास",
        nameEn = "Cotton",
        accentColor = Color(0xFF78350F),
        cardTint = Color(0xFFFFFBEB),
        iconEmoji = "☁️"
    ),
    SUGARCANE(
        key = "sugarcane",
        nameMr = "ऊस",
        nameHi = "गन्ना",
        nameEn = "Sugarcane",
        accentColor = Color(0xFF14532D),
        cardTint = Color(0xFFECFDF5),
        iconEmoji = "🎋"
    ),
    TUR(
        key = "tur",
        nameMr = "तूर",
        nameHi = "तूर",
        nameEn = "Tur",
        accentColor = Color(0xFFB45309),
        cardTint = Color(0xFFFEF3C7),
        iconEmoji = "🌾"
    ),
    OTHER(
        key = "other",
        nameMr = "इतर",
        nameHi = "अन्य",
        nameEn = "Other",
        accentColor = Color(0xFF1E40AF),
        cardTint = Color(0xFFEFF6FF),
        iconEmoji = "🌿"
    );

    fun getDisplayName(language: Language): String = when (language) {
        Language.MR -> nameMr
        Language.HI -> nameHi
        Language.EN -> nameEn
    }

    companion object {
        fun fromKey(key: String): CropItem {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: OTHER
        }
    }
}
