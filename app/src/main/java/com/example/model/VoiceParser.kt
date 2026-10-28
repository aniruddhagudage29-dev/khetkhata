package com.example.model

data class ParsedVoiceResult(
    val crop: CropItem?,
    val category: CategoryItem?,
    val amount: Long?
)

object VoiceParser {

    private val DEV_DIGITS = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')

    private val COLLOQUIAL_AMOUNTS = listOf(
        "अडीच हजार" to 2500L, "ढाई हजार" to 2500L, "ढाई हज़ार" to 2500L,
        "दीड हजार" to 1500L, "डेढ़ हजार" to 1500L, "डेढ़ हज़ार" to 1500L,
        "पावणे दोन हजार" to 1750L, "सव्वा हजार" to 1250L, "सवा हजार" to 1250L,
        "पंधराशे" to 1500L, "पंद्रह सौ" to 1500L,
        "बाराशे" to 1200L, "बारह सौ" to 1200L,
        "अकराशे" to 1100L, "ग्यारह सौ" to 1100L,
        "तेराशे" to 1300L, "चौदाशे" to 1400L, "सोळाशे" to 1600L,
        "सतराशे" to 1700L, "अठराशे" to 1800L, "एकोणीसशे" to 1900L,
        "पाचशे" to 500L, "पाँच सौ" to 500L, "पांच सौ" to 500L,
        "दोनशे" to 200L, "दो सौ" to 200L,
        "तीनशे" to 300L, "तीन सौ" to 300L,
        "चारशे" to 400L, "चार सौ" to 400L,
        "सहाशे" to 600L, "छह सौ" to 600L,
        "सातशे" to 700L, "सात सौ" to 700L,
        "आठशे" to 800L, "आठ सौ" to 800L,
        "नऊशे" to 900L, "नौ सौ" to 900L,
        "अडीचशे" to 250L, "दीडशे" to 150L,
        "पन्नास हजार" to 50000L, "दहा हजार" to 10000L, "पाच हजार" to 5000L,
        "दोन हजार" to 2000L, "एक हजार" to 1000L, "हजार" to 1000L,
        "शंभर" to 100L, "सौ" to 100L
    )

    fun parse(rawTranscript: String): ParsedVoiceResult {
        var text = rawTranscript.lowercase().trim()

        // Replace Devanagari numerals with ASCII
        for (i in DEV_DIGITS.indices) {
            text = text.replace(DEV_DIGITS[i].toString(), i.toString())
        }
        // Remove internal commas
        text = text.replace(Regex("(\\d),(\\d)"), "$1$2")

        // 1. Detect Crop
        var detectedCrop: CropItem? = null
        if (text.contains("कांदा") || text.contains("कांदे") || text.contains("प्याज") || text.contains("onion") || text.contains("kanda")) {
            detectedCrop = CropItem.ONION
        } else if (text.contains("सोयाबीन") || text.contains("सोया") || text.contains("soybean") || text.contains("soya")) {
            detectedCrop = CropItem.SOYBEAN
        } else if (text.contains("कापूस") || text.contains("कपास") || text.contains("cotton") || text.contains("kapus")) {
            detectedCrop = CropItem.COTTON
        } else if (text.contains("ऊस") || text.contains("गन्ना") || text.contains("sugarcane") || text.contains("oos") || text.contains("us")) {
            detectedCrop = CropItem.SUGARCANE
        } else if (text.contains("तूर") || text.contains("तुरी") || text.contains("अरहर") || text.contains("tur") || text.contains("toor")) {
            detectedCrop = CropItem.TUR
        } else if (text.contains("इतर") || text.contains("अन्य") || text.contains("other")) {
            detectedCrop = CropItem.OTHER
        }

        // 2. Detect Category
        var detectedCat: CategoryItem? = null
        if (text.contains("विक्री") || text.contains("विकला") || text.contains("विकले") || text.contains("बिक्री") || text.contains("बेचा") || text.contains("पैसे आले") || text.contains("sale") || text.contains("sold")) {
            detectedCat = CategoryItem.SALE
        } else if (text.contains("बियाणे") || text.contains("बीज") || text.contains("बियाणा") || text.contains("seed") || text.contains("biya") || text.contains(" बी ") || text.endsWith(" बी")) {
            detectedCat = CategoryItem.SEED
        } else if (text.contains("खत") || text.contains("युरिया") || text.contains("डीपी") || text.contains("खाद") || text.contains("fertilizer") || text.contains("khat") || text.contains("urea")) {
            detectedCat = CategoryItem.FERTILIZER
        } else if (text.contains("फवारणी") || text.contains("औषध") || text.contains("दवाई") || text.contains("स्प्रे") || text.contains("spray") || text.contains("pesticide") || text.contains("favarni")) {
            detectedCat = CategoryItem.PESTICIDE
        } else if (text.contains("मजुरी") || text.contains("मजूर") || text.contains("कामगार") || text.contains("मज़दूरी") || text.contains("मजदूरी") || text.contains("labour") || text.contains("majuri")) {
            detectedCat = CategoryItem.LABOUR
        } else if (text.contains("डिझेल") || text.contains("डीझेल") || text.contains("डीजल") || text.contains("पेट्रोल") || text.contains("diesel") || text.contains("petrol")) {
            detectedCat = CategoryItem.DIESEL
        } else if (text.contains("पाणी") || text.contains("पानी") || text.contains("सिंचन") || text.contains("water") || text.contains("pani")) {
            detectedCat = CategoryItem.WATER
        } else if (text.contains("वाहतूक") || text.contains("भाडे") || text.contains("गाडी") || text.contains("भाड़ा") || text.contains("ट्रक") || text.contains("transport") || text.contains("gadi") || text.contains("bhade")) {
            detectedCat = CategoryItem.TRANSPORT
        } else if (text.contains("इतर") || text.contains("अन्य") || text.contains("other")) {
            detectedCat = CategoryItem.OTHER
        }

        // 3. Detect Amount
        var detectedAmount: Long? = null

        // 3a. Check "2.5k" or "1.5 k"
        val kMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*k\\b").find(text)
        if (kMatch != null) {
            val num = kMatch.groupValues[1].toDoubleOrNull()
            if (num != null) {
                detectedAmount = Math.round(num * 1000)
            }
        }

        // 3b. Check number + multiplier ("5 हजार", "2 लाख")
        if (detectedAmount == null) {
            val multMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(हजार|हज़ार|thousand|लाख|lakh|शे|सौ)").find(text)
            if (multMatch != null) {
                val num = multMatch.groupValues[1].toDoubleOrNull() ?: 0.0
                val unit = multMatch.groupValues[2]
                detectedAmount = when (unit) {
                    "लाख", "lakh" -> Math.round(num * 100000)
                    "शे", "सौ" -> Math.round(num * 100)
                    else -> Math.round(num * 1000)
                }
            }
        }

        // 3c. Check colloquial phrases
        if (detectedAmount == null) {
            for ((phrase, valNum) in COLLOQUIAL_AMOUNTS) {
                if (text.contains(phrase)) {
                    detectedAmount = valNum
                    break
                }
            }
        }

        // 3d. Check direct digits
        if (detectedAmount == null) {
            val digitsMatch = Regex("\\d+").find(text)
            if (digitsMatch != null) {
                detectedAmount = digitsMatch.value.toLongOrNull()
            }
        }

        return ParsedVoiceResult(
            crop = detectedCrop,
            category = detectedCat,
            amount = detectedAmount
        )
    }
}
