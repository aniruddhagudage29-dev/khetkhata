package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CropItem
import com.example.model.Language
import com.example.model.NumberToWords
import com.example.ui.components.BounceableCard
import com.example.ui.theme.FarmBorder
import com.example.ui.theme.FarmCreamBg
import com.example.ui.theme.FarmGreenAction
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenDark
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmSurface
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary
import com.example.ui.theme.HarvestYellowBg
import com.example.ui.theme.HarvestYellowBorder
import com.example.ui.theme.HarvestYellowDarkText
import com.example.ui.theme.LossBg
import com.example.ui.theme.LossRed

@Composable
fun QuintalInputDialog(
    crop: CropItem,
    quintalStr: String,
    language: Language,
    onNumpadPress: (String) -> Unit,
    onChipPress: (Long) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val quintalNum = quintalStr.toLongOrNull() ?: 0L
    val displayQuintals = NumberToWords.formatQuintals(quintalNum, language)

    val title = when (language) {
        Language.MR -> "⚖️ ${crop.getDisplayName(language)} — अंदाजे उत्पादन"
        Language.HI -> "⚖️ ${crop.getDisplayName(language)} — अनुमानित उपज"
        Language.EN -> "⚖️ ${crop.getDisplayName(language)} — Expected Harvest"
    }

    val subtitle = when (language) {
        Language.MR -> "तोटा टाळण्याचा किमान भाव काढण्यासाठी अंदाजे क्विंटल टाका"
        Language.HI -> "नुकसान से बचने का न्यूनतम भाव जानने के लिए क्विंटल डालें"
        Language.EN -> "Enter expected quintals to calculate break-even price"
    }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FarmCreamBg, RoundedCornerShape(24.dp))
                .border(3.5.dp, FarmBorder, RoundedCornerShape(24.dp))
                .padding(14.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                color = FarmTextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = FarmTextSecondary,
                textAlign = TextAlign.Center
            )

            // Big Quintal Display Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HarvestYellowBg, RoundedCornerShape(16.dp))
                    .border(3.dp, HarvestYellowBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp)
                    .testTag("quintal_display_box"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayQuintals,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = HarvestYellowDarkText
                )
            }

            // Quick Quintal Chips (+5, +10, +20, +50)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (qChip in listOf(5L, 10L, 20L, 50L)) {
                    val chipText = "+" + NumberToWords.toLocalDigits(qChip.toString(), language)
                    BounceableCard(
                        onClick = { onChipPress(qChip) },
                        backgroundColor = FarmSurface,
                        borderColor = HarvestYellowBorder,
                        borderWidth = 2.dp,
                        cornerRadius = 12.dp,
                        elevation = 3.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("quintal_chip_$qChip")
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary
                        )
                    }
                }
            }

            // 3x4 Big Number Pad for Quintals
            val keypadRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "DEL")
            )
            for (row in keypadRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (key in row) {
                        val isClear = key == "C"
                        val isDel = key == "DEL"
                        val keyLabel = when {
                            isClear -> "C"
                            isDel -> "⌫"
                            else -> NumberToWords.toLocalDigits(key, language)
                        }
                        BounceableCard(
                            onClick = { onNumpadPress(key) },
                            backgroundColor = if (isClear) LossBg else FarmSurface,
                            borderColor = if (isClear) LossRed else FarmBorder,
                            borderWidth = 2.dp,
                            cornerRadius = 14.dp,
                            elevation = 3.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("quintal_key_$key")
                        ) {
                            Text(
                                text = keyLabel,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isClear) LossRed else FarmTextPrimary
                            )
                        }
                    }
                }
            }

            // Action Row: Skip (✖) & Save (✔)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BounceableCard(
                    onClick = onDismiss,
                    backgroundColor = LossBg,
                    borderColor = LossRed,
                    borderWidth = 2.5.dp,
                    cornerRadius = 16.dp,
                    elevation = 4.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .testTag("quintal_cancel_button")
                ) {
                    Text(
                        text = "✖",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = LossRed
                    )
                }

                BounceableCard(
                    onClick = onSave,
                    backgroundColor = FarmGreenAction,
                    borderColor = FarmGreenDark,
                    borderWidth = 3.dp,
                    cornerRadius = 16.dp,
                    elevation = 6.dp,
                    modifier = Modifier
                        .weight(2f)
                        .height(64.dp)
                        .testTag("quintal_save_button")
                        .semantics { contentDescription = "क्विंटल जतन करा / Save Quintals" }
                ) {
                    Text(
                        text = "✔",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun HowToUseGuideDialog(
    language: Language,
    onSpeakGuide: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    val title = when (language) {
        Language.MR -> "📘 शेतखाते कसे वापरावे व हिशोब कसा होतो?"
        Language.HI -> "📘 खेतखाता कैसे चलाएं और हिसाब कैसे होता है?"
        Language.EN -> "📘 How to Use & How Math Works"
    }

    val step1 = when (language) {
        Language.MR -> "१. पहिली पायरी (पीक निवडा): मुख्य स्क्रीनवर तुमच्या पिकाच्या चित्रावर दाबा (उदा. 🧅 कांदा, 🌱 सोयाबीन)."
        Language.HI -> "१. पहला कदम (फसल चुनें): मुख्य स्क्रीन पर अपनी फसल के चित्र पर दबाएं (जैसे 🧅 प्याज, 🌱 सोयाबीन)।"
        Language.EN -> "1. Step 1 (Tap Crop): Tap your crop picture card on the Home screen (e.g. 🧅 Onion, 🌱 Soybean)."
    }

    val step2 = when (language) {
        Language.MR -> "२. दुसरी पायरी (काय झाले?): पैसे आले असल्यास हिरवे '➕ विक्री' बटण दाबा, किंवा खर्च झाला असल्यास '➖ बियाणे, खत, मजुरी' निवडा."
        Language.HI -> "२. दूसरा कदम (किस लिए?): पैसे आए तो हरा '➕ बिक्री' बटन दबाएं, या खर्च हुआ तो '➖ बीज, खाद, मज़दूरी' चुनें।"
        Language.EN -> "2. Step 2 (What For?): Tap the green '➕ Sale' card when money comes in, or tap an expense card ('➖ Seed, Fertilizer, Labour')."
    }

    val step3 = when (language) {
        Language.MR -> "३. तिसरी पायरी (रक्कम जतन करा): मोठ्या आकड्यांवर रक्कम टाका आणि सर्वात खालचे हिरवे बटण (✔) दाबा."
        Language.HI -> "३. तीसरा कदम (रकम सेव करें): बड़े नंबर पैड से रकम डालें और नीचे का बड़ा हरा बटन (✔) दबाएं।"
        Language.EN -> "3. Step 3 (Save Amount): Enter the Rupees on the keypad and tap the giant green tick (✔)."
    }

    val formula1Title = when (language) {
        Language.MR -> "📊 सूत्र १: नफा किंवा तोटा कसा काढतात?"
        Language.HI -> "📊 सूत्र १: मुनाफा या नुकसान कैसे निकलता है?"
        Language.EN -> "📊 Formula 1: How Profit or Loss is Calculated"
    }

    val formula1Body = when (language) {
        Language.MR -> "➕ एकूण विक्री (उत्पन्न) − ➖ एकूण खर्च = नफा (▲ हिरवा) किंवा तोटा (▼ लाल)\nउदा. विक्री ₹ २८,००० − खर्च ₹ १०,००० = ₹ १८,००० नफा"
        Language.HI -> "➕ कुल बिक्री (आमदनी) − ➖ कुल खर्च = मुनाफा (▲ हरा) या नुकसान (▼ लाल)\nउदा. बिक्री ₹ २८,००० − खर्च ₹ १०,००० = ₹ १८,००० मुनाफा"
        Language.EN -> "➕ Total Sales (Income) − ➖ Total Expenses = Profit (▲ Green) or Loss (▼ Red)\nExample: Sales ₹ 28,000 − Cost ₹ 10,000 = ₹ 18,000 Profit"
    }

    val formula2Title = when (language) {
        Language.MR -> "🏷️ सूत्र २: तोटा टाळण्याचा किमान भाव (Break-Even Price)"
        Language.HI -> "🏷️ सूत्र २: नुकसान से बचने का न्यूनतम भाव (Break-Even)"
        Language.EN -> "🏷️ Formula 2: Break-Even Selling Price per Quintal"
    }

    val formula2Body = when (language) {
        Language.MR -> "➖ एकूण खर्च ÷ ⚖️ अंदाजे उत्पादन (क्विंटल) = किमान दर (₹ प्रति क्विंटल)\nउदा. ₹ १०,००० खर्च ÷ २० क्विंटल = ₹ ५०० प्रति क्विंटल पेक्षा जास्त भावाने विका"
        Language.HI -> "➖ कुल खर्च ÷ ⚖️ अनुमानित उपज (क्विंटल) = न्यूनतम भाव (₹ प्रति क्विंटल)\nउदा. ₹ १०,००० खर्च ÷ २० क्विंटल = ₹ ५०० प्रति क्विंटल से ऊपर बेचें"
        Language.EN -> "➖ Total Cost ÷ ⚖️ Expected Harvest (Quintals) = Break-Even Price\nExample: ₹ 10,000 Cost ÷ 20 Quintals = Sell above ₹ 500 per quintal"
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FarmCreamBg, RoundedCornerShape(24.dp))
                .border(3.5.dp, FarmBorder, RoundedCornerShape(24.dp))
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                    color = FarmGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                BounceableCard(
                    onClick = onSpeakGuide,
                    backgroundColor = HarvestYellowBg,
                    borderColor = HarvestYellowBorder,
                    cornerRadius = 12.dp,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text(
                        text = "🔊 ऐका",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = HarvestYellowDarkText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // 3 Steps Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FarmSurface, RoundedCornerShape(16.dp))
                    .border(2.dp, FarmBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = step1, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FarmTextPrimary)
                Text(text = step2, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FarmTextPrimary)
                Text(text = step3, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FarmTextPrimary)
            }

            // Formula 1 Card (Profit/Loss)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FarmGreenContainer, RoundedCornerShape(16.dp))
                    .border(2.5.dp, FarmGreenAction, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = formula1Title, fontSize = 19.sp, fontWeight = FontWeight.Black, color = FarmGreenDark)
                Text(text = formula1Body, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = FarmGreenDark)
            }

            // Formula 2 Card (Break-Even Price Tag)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HarvestYellowBg, RoundedCornerShape(16.dp))
                    .border(2.5.dp, HarvestYellowBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = formula2Title, fontSize = 19.sp, fontWeight = FontWeight.Black, color = HarvestYellowDarkText)
                Text(text = formula2Body, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = HarvestYellowDarkText)
            }

            BounceableCard(
                onClick = onDismiss,
                backgroundColor = FarmGreenAction,
                borderColor = FarmGreenDark,
                borderWidth = 3.dp,
                cornerRadius = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("guide_close_button")
            ) {
                Text(
                    text = "✔ समजले (Got It)",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}
