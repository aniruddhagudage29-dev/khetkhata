package com.example.model

import androidx.compose.ui.graphics.Color

enum class CategoryItem(
    val key: String,
    val nameMr: String,
    val nameHi: String,
    val nameEn: String,
    val isSale: Boolean,
    val iconEmoji: String,
    val badgeColor: Color
) {
    SEED("seed", "बियाणे", "बीज", "Seed", false, "🌰", Color(0xFFB45309)),
    FERTILIZER("fertilizer", "खत", "खाद", "Fertilizer", false, "🧪", Color(0xFF15803D)),
    PESTICIDE("pesticide", "फवारणी", "दवाई", "Spray", false, "🧴", Color(0xFF0284C7)),
    LABOUR("labour", "मजुरी", "मज़दूरी", "Labour", false, "👷", Color(0xFFEA580C)),
    DIESEL("diesel", "डिझेल", "डीज़ल", "Diesel", false, "⛽", Color(0xFFDC2626)),
    WATER("water", "पाणी", "पानी", "Water", false, "💧", Color(0xFF0284C7)),
    TRANSPORT("transport", "वाहतूक", "भाड़ा", "Transport", false, "🚜", Color(0xFF2563EB)),
    OTHER("other", "इतर", "अन्य", "Other", false, "📦", Color(0xFF475569)),
    SALE("sale", "विक्री", "बिक्री", "Sale", true, "💰", Color(0xFF15803D));

    fun getDisplayName(language: Language): String = when (language) {
        Language.MR -> nameMr
        Language.HI -> nameHi
        Language.EN -> nameEn
    }

    fun getActionSubtitle(language: Language): String = if (isSale) {
        when (language) {
            Language.MR -> "पैसे आले (उत्पन्न)"
            Language.HI -> "पैसे आए (आमदनी)"
            Language.EN -> "Money In (Income)"
        }
    } else {
        when (language) {
            Language.MR -> "पैसे गेले (खर्च)"
            Language.HI -> "पैसे गए (खर्च)"
            Language.EN -> "Money Out (Expense)"
        }
    }

    companion object {
        fun fromKey(key: String): CategoryItem {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: OTHER
        }
    }
}
