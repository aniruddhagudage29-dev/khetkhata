package com.example.advisor

import com.example.data.CropEntryEntity
import com.example.model.CropItem
import com.example.model.Language
import com.example.model.NumberToWords

data class AdvisorMessage(
    val sender: String, // "user" or "advisor"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

object KisanMitraAdvisor {

    fun generateCropFinancialAnalysis(
        crop: CropItem,
        entries: List<CropEntryEntity>,
        language: Language
    ): String {
        val cropEntries = entries.filter { it.crop.equals(crop.key, ignoreCase = true) }
        var totalExpense = 0L
        var totalIncome = 0L

        cropEntries.forEach { e ->
            if (e.isIncome || e.category == "sale") totalIncome += e.amount
            else totalExpense += e.amount
        }

        val net = totalIncome - totalExpense
        val cropName = crop.getDisplayName(language)
        val formattedNet = NumberToWords.formatCurrency(net, language)
        val formattedExp = NumberToWords.formatCurrency(totalExpense, language)
        val formattedInc = NumberToWords.formatCurrency(totalIncome, language)

        return when (language) {
            Language.MR -> {
                if (net >= 0) {
                    "🌱 **$cropName हिशोब:**\n" +
                    "• एकूण उत्पन्न: $formattedInc\n" +
                    "• एकूण खर्च: $formattedExp\n" +
                    "• **नफा: $formattedNet**\n\n" +
                    "💡 **सल्ला:** तुमचे उत्पन्न चांगले आहे! खत व फवारणी खर्चाचे प्रमाण नियंत्रणात ठेवा. पुढील हंगामासाठी उत्तम दर्जाचे बियाणे निवडा."
                } else {
                    "🌱 **$cropName हिशोब:**\n" +
                    "• एकूण खर्च: $formattedExp\n" +
                    "• विक्री उत्पन्न: $formattedInc\n" +
                    "• **तोटा: $formattedNet**\n\n" +
                    "⚠️ **सल्ला:** सध्या खर्च उत्पन्नापेक्षा जास्त आहे. मजुरी आणि डिझेलच्या खर्चावर लक्ष ठेवा. स्थानिक कृषी उत्पन्न बाजार समितीत (APMC) भाव सुधारण्याची वाट पहा."
                }
            }
            Language.HI -> {
                if (net >= 0) {
                    "🌱 **$cropName हिसाब:**\n" +
                    "• कुल आमदनी: $formattedInc\n" +
                    "• कुल खर्च: $formattedExp\n" +
                    "• **मुनाफा: $formattedNet**\n\n" +
                    "💡 **सुझाव:** आपका मुनाफा अच्छा है! खाद और कीटनाशक लागत पर संतुलन बनाए रखें।"
                } else {
                    "🌱 **$cropName हिसाब:**\n" +
                    "• कुल खर्च: $formattedExp\n" +
                    "• कुल आमदनी: $formattedInc\n" +
                    "• **नुकसान: $formattedNet**\n\n" +
                    "⚠️ **सुझाव:** खर्च अधिक हुआ है। मंडी में अच्छे दाम मिलने तक भंडारण की व्यवस्था देखें।"
                }
            }
            Language.EN -> {
                if (net >= 0) {
                    "🌱 **$cropName Summary:**\n" +
                    "• Income: $formattedInc\n" +
                    "• Expenses: $formattedExp\n" +
                    "• **Net Profit: $formattedNet**\n\n" +
                    "💡 **Tip:** Strong returns! Optimize fertilizer and labor scheduling for next season."
                } else {
                    "🌱 **$cropName Summary:**\n" +
                    "• Expenses: $formattedExp\n" +
                    "• Income: $formattedInc\n" +
                    "• **Net Loss: $formattedNet**\n\n" +
                    "⚠️ **Tip:** Expenses exceed revenues. Monitor pesticide costs and wait for peak APMC mandi rates."
                }
            }
        }
    }

    fun answerFarmerQuery(query: String, language: Language): String {
        val q = query.lowercase()

        return when {
            q.contains("खत") || q.contains("fertilizer") || q.contains("खाद") -> {
                when (language) {
                    Language.MR -> "🌾 **खतांचे नियोजन:** कांदा आणि सोयाबीन पिकासाठी नत्र, स्फुरद आणि पालाश (NPK) संतुलित प्रमाणात द्या. पेरणीच्या वेळी डीएपी किंवा १०:२६:२६ चा वापर करा. अवाजवी खतांचा खर्च टाळा."
                    Language.HI -> "🌾 **खाद प्रबंधन:** फसल में आवश्यकतानुसार ही यूरिया और डीएपी का प्रयोग करें। बुवाई के समय संतुलित NPK का उपयोग लाभदायक रहता है।"
                    Language.EN -> "🌾 **Fertilizer Tip:** Use balanced NPK (10:26:26 or DAP) during sowing. Avoid excessive urea to prevent insect attack and save input cost."
                }
            }
            q.contains("पाणी") || q.contains("water") || q.contains("पानी") || q.contains("सिंचन") -> {
                when (language) {
                    Language.MR -> "💧 **पाणी व्यवस्थापन:** ठिबक सिंचनाचा (Drip) वापर केल्यास ५०% पाण्याची आणि खतांची बचत होते. उन्हाळ्यात कांदा किंवा ऊसाला हलके व नियमित पाणी द्या."
                    Language.HI -> "💧 **सिंचाई सलाह:** ड्रिप सिंचाई अपनाने से 50% पानी की बचत होती है और फसल की उपज में सुधार होता है।"
                    Language.EN -> "💧 **Irrigation Advice:** Drip irrigation reduces water consumption by up to 50% and improves fertilizer uptake."
                }
            }
            q.contains("फवारणी") || q.contains("रोग") || q.contains("spray") || q.contains("कीड") -> {
                when (language) {
                    Language.MR -> "🧴 **फवारणी व कीड नियंत्रण:** ढगाळ वातावरणात कांद्यावरील करपा रोगासाठी किंवा सोयाबीनवरील अळीसाठी शिफारस केलेले बुरशीनाशक वापरा. फवारणी नेहमी सकाळी किंवा संध्याकाळी करा."
                    Language.HI -> "🧴 **कीट एवं रोग नियंत्रण:** फफूंदनाशक और कीटनाशक का छिड़काव सुबह या शाम के समय शांत मौसम में ही करें।"
                    Language.EN -> "🧴 **Crop Protection:** Apply recommended fungicide/insecticide during early morning or late afternoon for maximum efficacy."
                }
            }
            q.contains("भाव") || q.contains("मंडी") || q.contains("market") || q.contains("बाजार") -> {
                when (language) {
                    Language.MR -> "📈 **बाजारभाव माहिती:** कांदा विक्रीसाठी लासलगाव, पिंपळगाव व नाशिक बाजारपेठेचे दर तपासा. आवक जास्त असल्यास माल वाळवून चाळीत साठवून ठेवा."
                    Language.HI -> "📈 **मंडी भाव:** स्थानीय APMC मंडी के दैनिक भाव जानकर ही फसल बिक्री करें। ग्रेडिंग करने से बेहतर दाम मिलते हैं।"
                    Language.EN -> "📈 **Market Rates:** Check regional APMC prices before selling. Grading your harvest ensures a 10-15% premium price."
                }
            }
            else -> {
                when (language) {
                    Language.MR -> "🧑‍🌾 **शेतकरी मित्र:** मी तुमचा शेती व हिशोब मदतनीस आहे. तुम्ही मला खत, बियाणे, पाणी किंवा बाजारभावाबाबत विचारू शकता!"
                    Language.HI -> "🧑‍🌾 **किसान मित्र:** मैं आपका कृषि सलाहकार हूँ। आप खाद, बीज, सिंचाई या फसल भाव के बारे में प्रश्न पूछ सकते हैं।"
                    Language.EN -> "🧑‍🌾 **Farmer Buddy:** I am your farm finance & advisory helper. Ask me about fertilizers, pest management, or crop market rates!"
                }
            }
        }
    }
}
