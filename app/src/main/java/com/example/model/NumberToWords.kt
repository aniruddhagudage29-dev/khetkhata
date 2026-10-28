package com.example.model

object NumberToWords {

    private val DEV_DIGITS = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')

    fun toLocalDigits(numberStr: String, language: Language): String {
        if (language == Language.EN) return numberStr
        val sb = StringBuilder()
        for (ch in numberStr) {
            if (ch in '0'..'9') {
                sb.append(DEV_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Formats numbers using authentic Indian numbering commas (e.g. 1,00,000 for 1 Lakh).
     */
    fun formatIndianNumber(amount: Long): String {
        val s = Math.abs(amount).toString()
        if (s.length <= 3) return s
        val lastThree = s.substring(s.length - 3)
        val remaining = s.substring(0, s.length - 3)
        val groups = mutableListOf<String>()
        var i = remaining.length
        while (i > 0) {
            val start = (i - 2).coerceAtLeast(0)
            groups.add(0, remaining.substring(start, i))
            i = start
        }
        return groups.joinToString(",") + "," + lastThree
    }

    fun formatCurrency(amount: Long, language: Language): String {
        val indianCommaStr = formatIndianNumber(amount)
        val localizedDigits = toLocalDigits(indianCommaStr, language)
        return "₹ $localizedDigits"
    }

    fun formatQuintals(quintals: Long, language: Language): String {
        val numStr = toLocalDigits(quintals.toString(), language)
        return when (language) {
            Language.MR -> "$numStr क्विंटल"
            Language.HI -> "$numStr क्विंटल"
            Language.EN -> "$numStr Quintals"
        }
    }

    fun buildBreakEvenHeadline(pricePerQuintal: Long, language: Language): String {
        val priceFormatted = formatCurrency(pricePerQuintal, language)
        return when (language) {
            Language.MR -> "तोटा टाळण्यासाठी $priceFormatted प्रति क्विंटल पेक्षा जास्त भावाने विका"
            Language.HI -> "नुकसान से बचने के लिए $priceFormatted प्रति क्विंटल से ऊपर बेचें"
            Language.EN -> "Sell above $priceFormatted per quintal to avoid loss"
        }
    }

    fun buildBreakEvenSpeech(
        cropName: String,
        totalCost: Long,
        quintals: Long,
        pricePerQuintal: Long,
        language: Language
    ): String {
        val costWords = convert(totalCost, language)
        val priceWords = convert(pricePerQuintal, language)
        val qNum = toLocalDigits(quintals.toString(), language)
        return when (language) {
            Language.MR -> "$cropName पिकाचा एकूण खर्च $costWords आहे. $qNum क्विंटल उत्पादनासाठी तोटा टाळण्यासाठी किमान $priceWords प्रति क्विंटल पेक्षा जास्त भावाने विका."
            Language.HI -> "$cropName का कुल खर्च $costWords है। $qNum क्विंटल उपज पर नुकसान से बचने के लिए $priceWords प्रति क्विंटल से ऊपर बेचें।"
            Language.EN -> "Total cost for $cropName is $costWords. For $quintals quintals, sell above $priceWords per quintal to avoid loss."
        }
    }

    // Verified standard Marathi 1-99 orthography
    private val WORDS_MR = arrayOf(
        "", "एक", "दोन", "तीन", "चार", "पाच", "सहा", "सात", "आठ", "नऊ", "दहा",
        "अकरा", "बारा", "तेरा", "चौदा", "पंधरा", "सोळा", "सतरा", "अठरा", "एकोणीस", "वीस",
        "एकवीस", "बावीस", "तेवीस", "चोवीस", "पंचवीस", "सव्वीस", "सत्तावीस", "अठ्ठावीस", "एकोणतीस", "तीस",
        "एकतीस", "बत्तीस", "तेहतीस", "चौतीस", "पस्तीस", "छत्तीस", "सदतीस", "अडतीस", "एकोणचाळीस", "चाळीस",
        "एकेचाळीस", "बेचाळीस", "त्रेचाळीस", "चव्वेचाळीस", "पंचेचाळीस", "सेहेचाळीस", "सत्तेचाळीस", "अठ्ठेचाळीस", "एकोणपन्नास", "पन्नास",
        "एकावन्न", "बावन्न", "त्रेपन्न", "चोपन्न", "पंचावन्न", "छप्पन्न", "सत्तावन्न", "अठ्ठावन्न", "एकोणसाठ", "साठ",
        "एकसष्ठ", "बासष्ठ", "त्रेसष्ठ", "चौसष्ठ", "पासष्ठ", "सहासष्ठ", "सदुसष्ठ", "अडुसष्ठ", "एकोणसत्तर", "सत्तर",
        "एकाहत्तर", "बाहत्तर", "त्र्याहत्तर", "चौऱ्याहत्तर", "पंच्याहत्तर", "शहात्तर", "सत्याहत्तर", "अठ्ठ्याहत्तर", "एकोणऐंशी", "ऐंशी",
        "एक्याऐंशी", "ब्याऐंशी", "त्र्याऐंशी", "चौऱ्याऐंशी", "पंच्याऐंशी", "शहाऐंशी", "सत्याऐंशी", "अठ्ठ्याऐंशी", "एकोणनव्वद", "नव्वद",
        "एक्याण्णव", "ब्याण्णव", "त्र्याण्णव", "चौऱ्याण्णव", "पंच्याण्णव", "शहाण्णव", "सत्याण्णव", "अठ्ठ्याण्णव", "नव्व्याण्णव"
    )

    // Verified standard Hindi 1-99 orthography
    private val WORDS_HI = arrayOf(
        "", "एक", "दो", "तीन", "चार", "पाँच", "छह", "सात", "आठ", "नौ", "दस",
        "ग्यारह", "बारह", "तेरह", "चौदह", "पंद्रह", "सोलह", "सत्रह", "अठारह", "उन्नीस", "बीस",
        "इक्कीस", "बाईस", "तेईस", "चौबीस", "पच्चीस", "छब्बीस", "सत्ताईस", "अट्ठाईस", "उनतीस", "तीस",
        "इकतीस", "बत्तीस", "तैंतीस", "चौंतीस", "पैंतीस", "छत्तीस", "सैंतीस", "अड़तीस", "उनतालीस", "चालीस",
        "इकतालीस", "बयालीस", "तैंतालीस", "चवालीस", "पैंतालीस", "छियालीस", "सैंतालीस", "अड़तालीस", "उनचास", "पचास",
        "इकावन", "बावन", "तिरेपन", "चौवन", "पचपन", "छप्पन", "सत्तावन", "अट्ठावन", "उनसठ", "साठ",
        "इकसठ", "बासठ", "तिरेसठ", "चौंसठ", "पैंसठ", "छियासठ", "सड़सठ", "अड़सठ", "उनहत्तर", "सत्तर",
        "इकहत्तर", "बहत्तर", "तिहत्तर", "चौहत्तर", "पचहत्तर", "छिहत्तर", "सतहत्तर", "अठहत्तर", "उनासी", "अस्सी",
        "इक्यासी", "बयासी", "तिरासी", "चौरासी", "पचासी", "छियासी", "सतासी", "अठासी", "नवासी", "नब्बे",
        "इक्यानवे", "बयानवे", "तिरानवे", "चौरानवे", "पचानवे", "छियानवे", "सतानवे", "अट्ठानवे", "निन्यानवे"
    )

    private val WORDS_EN_ONES = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    )
    private val WORDS_EN_TENS = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    private fun twoDigitWord(n: Int, lang: Language): String {
        if (n <= 0) return ""
        return when (lang) {
            Language.MR -> if (n in WORDS_MR.indices) WORDS_MR[n] else n.toString()
            Language.HI -> if (n in WORDS_HI.indices) WORDS_HI[n] else n.toString()
            Language.EN -> {
                if (n < 20) WORDS_EN_ONES[n]
                else (WORDS_EN_TENS[n / 10] + " " + WORDS_EN_ONES[n % 10]).trim()
            }
        }
    }

    fun convert(amount: Long, lang: Language): String {
        var n = Math.abs(amount)
        if (n == 0L) {
            return when (lang) {
                Language.MR -> "शून्य रुपये"
                Language.HI -> "शून्य रुपये"
                Language.EN -> "Zero Rupees"
            }
        }

        val parts = mutableListOf<String>()

        val crore = (n / 10000000).toInt()
        n %= 10000000
        val lakh = (n / 100000).toInt()
        n %= 100000
        val thousand = (n / 1000).toInt()
        n %= 1000
        val hundred = (n / 100).toInt()
        val rem = (n % 100).toInt()

        val unitCrore = when (lang) { Language.MR -> "कोटी"; Language.HI -> "करोड़"; Language.EN -> "Crore" }
        val unitLakh = when (lang) { Language.MR -> "लाख"; Language.HI -> "लाख"; Language.EN -> "Lakh" }
        val unitThousand = when (lang) { Language.MR -> "हजार"; Language.HI -> "हज़ार"; Language.EN -> "Thousand" }
        val unitHundred = when (lang) { Language.MR -> "शे"; Language.HI -> "सौ"; Language.EN -> "Hundred" }

        if (crore > 0) parts.add("${twoDigitWord(crore, lang)} $unitCrore")
        if (lakh > 0) parts.add("${twoDigitWord(lakh, lang)} $unitLakh")
        if (thousand > 0) parts.add("${twoDigitWord(thousand, lang)} $unitThousand")
        if (hundred > 0) {
            if (lang == Language.MR) {
                if (hundred == 1 && rem == 0) {
                    parts.add("शंभर")
                } else {
                    parts.add("${twoDigitWord(hundred, lang)}$unitHundred")
                }
            } else {
                parts.add("${twoDigitWord(hundred, lang)} $unitHundred")
            }
        }
        if (rem > 0) parts.add(twoDigitWord(rem, lang))

        val currencySuffix = when (lang) {
            Language.MR -> "रुपये"
            Language.HI -> "रुपये"
            Language.EN -> "Rupees"
        }

        return (parts.joinToString(" ") + " $currencySuffix").trim()
    }
}
