package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.CropEntryEntity
import com.example.model.CategoryItem
import com.example.model.CropItem
import com.example.model.Language
import com.example.model.NumberToWords
import com.example.ui.components.BounceableCard
import com.example.ui.components.CropVectorBadge
import com.example.ui.theme.FarmBorder
import com.example.ui.theme.FarmGreenAction
import com.example.ui.theme.FarmGreenDark
import com.example.ui.theme.FarmSurface
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary
import com.example.ui.theme.HarvestYellowBg
import com.example.ui.theme.HarvestYellowBorder
import com.example.ui.theme.HarvestYellowDarkText
import com.example.ui.theme.LossBg
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitBg
import com.example.ui.theme.ProfitGreen

@Composable
fun ProfitScreen(
    entries: List<CropEntryEntity>,
    profitFilterCrop: CropItem?,
    lastSavedId: Long?,
    language: Language,
    cropQuintalsMap: Map<String, Long> = emptyMap(),
    onOpenQuintalModal: (CropItem) -> Unit = {},
    onSpeakBreakEven: (CropItem, Long) -> Unit = { _, _ -> },
    onDeleteEntry: (Long) -> Unit = {},
    onDeleteCropCalculations: (CropItem) -> Unit = {},
    onClearQuintals: (CropItem) -> Unit = {},
    onClearAllEntries: () -> Unit = {},
    onUndoClick: () -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (entries.isEmpty()) {
                val emptyTitle = when (language) {
                    Language.MR -> "🌱 अजून कोणतीही नोंद नाही"
                    Language.HI -> "🌱 अभी कोई प्रविष्टि नहीं है"
                    Language.EN -> "🌱 No entries saved yet"
                }
                val emptySub = when (language) {
                    Language.MR -> "मुख्य स्क्रीनवर जाऊन पीक निवडा आणि पहिली नोंद करा"
                    Language.HI -> "होम स्क्रीन पर जाकर फसल चुनें और पहली प्रविष्टि जोड़ें"
                    Language.EN -> "Go to Home and tap a crop to add your first entry"
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FarmSurface, RoundedCornerShape(20.dp))
                        .border(2.5.dp, FarmBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                        .testTag("profit_empty_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = emptyTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = emptySub,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (profitFilterCrop != null) {
                val cropEntries = remember(entries, profitFilterCrop.key) {
                    entries.filter { it.crop.equals(profitFilterCrop.key, ignoreCase = true) }
                }
                val summary = remember(entries, profitFilterCrop.key) {
                    computeSummary(entries, profitFilterCrop.key)
                }
                val quintals = cropQuintalsMap[profitFilterCrop.key] ?: 0L

                // 1. Hero Profit / Loss Card with Formula Breakdown + Bin Button
                ProfitCard(
                    crop = profitFilterCrop,
                    title = profitFilterCrop.getDisplayName(language),
                    summary = summary,
                    language = language,
                    showBinButton = cropEntries.isNotEmpty() || quintals > 0L,
                    onBinClick = { onDeleteCropCalculations(profitFilterCrop) }
                )

                // 2. Break-Even Price Tag Card (Big Yellow Price-Tag Card) + Bin Button for Quintals
                BreakEvenPriceTagCard(
                    crop = profitFilterCrop,
                    totalCost = summary.expense,
                    quintals = quintals,
                    language = language,
                    onEditQuintals = { onOpenQuintalModal(profitFilterCrop) },
                    onClearQuintals = { onClearQuintals(profitFilterCrop) },
                    onSpeakBreakEven = { onSpeakBreakEven(profitFilterCrop, quintals) }
                )

                // 3. Individual Entries for this Crop with Bin Button on each row
                if (cropEntries.isNotEmpty()) {
                    EntriesSectionWithBin(
                        entries = cropEntries,
                        language = language,
                        onDeleteEntry = onDeleteEntry
                    )
                }
            } else {
                // Overall Farm Total Card + Bin Button to clear all calculations
                val overallSummary = remember(entries) { computeSummary(entries, null) }
                val overallTitle = when (language) {
                    Language.MR -> "💰 एकूण शेती हिशोब"
                    Language.HI -> "💰 कुल खेती हिसाब"
                    Language.EN -> "💰 Total Farm Balance"
                }
                ProfitCard(
                    crop = null,
                    title = overallTitle,
                    summary = overallSummary,
                    language = language,
                    showBinButton = entries.isNotEmpty(),
                    onBinClick = onClearAllEntries
                )

                // Per-Crop Cards + Per-Crop Break-Even Cards + Bin Buttons
                for (crop in CropItem.entries) {
                    val cropEntries = entries.filter { it.crop.equals(crop.key, ignoreCase = true) }
                    if (cropEntries.isNotEmpty()) {
                        val cropSummary = computeSummary(entries, crop.key)
                        val quintals = cropQuintalsMap[crop.key] ?: 0L
                        ProfitCard(
                            crop = crop,
                            title = crop.getDisplayName(language),
                            summary = cropSummary,
                            language = language,
                            showBinButton = true,
                            onBinClick = { onDeleteCropCalculations(crop) }
                        )
                        BreakEvenPriceTagCard(
                            crop = crop,
                            totalCost = cropSummary.expense,
                            quintals = quintals,
                            language = language,
                            onEditQuintals = { onOpenQuintalModal(crop) },
                            onClearQuintals = { onClearQuintals(crop) },
                            onSpeakBreakEven = { onSpeakBreakEven(crop, quintals) }
                        )
                    }
                }

                // Individual Entries List with Bin Button on each entry so user can delete any item
                if (entries.isNotEmpty()) {
                    EntriesSectionWithBin(
                        entries = entries,
                        language = language,
                        onDeleteEntry = onDeleteEntry
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Action Buttons: Undo Last, Bin (Delete All), & Home
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (lastSavedId != null) {
                val undoText = when (language) {
                    Language.MR -> "↩️ मागील खोडा"
                    Language.HI -> "↩️ पिछला मिटाएं"
                    Language.EN -> "↩️ Undo Last"
                }
                BounceableCard(
                    onClick = onUndoClick,
                    backgroundColor = LossBg,
                    borderColor = LossRed,
                    borderWidth = 3.dp,
                    cornerRadius = 20.dp,
                    elevation = 5.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(76.dp)
                        .testTag("profit_undo_button")
                        .semantics { contentDescription = undoText }
                ) {
                    Text(
                        text = undoText,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        color = LossRed
                    )
                }
            }

            if (entries.isNotEmpty()) {
                val clearBinDesc = when (language) {
                    Language.MR -> "सर्व हिशोब पुसा / Delete all calculations"
                    Language.HI -> "सभी हिसाब मिटाएं / Delete all calculations"
                    Language.EN -> "Delete all calculations"
                }
                BounceableCard(
                    onClick = onClearAllEntries,
                    backgroundColor = LossBg,
                    borderColor = LossRed,
                    borderWidth = 3.dp,
                    cornerRadius = 20.dp,
                    elevation = 5.dp,
                    modifier = Modifier
                        .size(width = 84.dp, height = 76.dp)
                        .testTag("profit_clear_all_bin_button")
                        .semantics { contentDescription = clearBinDesc }
                ) {
                    Text(
                        text = "🗑️",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = LossRed
                    )
                }
            }

            BounceableCard(
                onClick = onDoneClick,
                backgroundColor = FarmGreenAction,
                borderColor = FarmGreenDark,
                borderWidth = 3.dp,
                cornerRadius = 20.dp,
                elevation = 6.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(76.dp)
                    .testTag("profit_home_button")
                    .semantics { contentDescription = "मुख्य स्क्रीनवर जा / Home" }
            ) {
                Text(
                    text = "🏠 ✔",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun EntriesSectionWithBin(
    entries: List<CropEntryEntity>,
    language: Language,
    onDeleteEntry: (Long) -> Unit
) {
    val sectionTitle = when (language) {
        Language.MR -> "📋 सर्व नोंदी (पुसण्यासाठी 🗑️ दाबा)"
        Language.HI -> "📋 सभी प्रविष्टियाँ (मिटाने के लिए 🗑️ दबाएं)"
        Language.EN -> "📋 Saved Entries (Tap 🗑️ to delete)"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FarmSurface, RoundedCornerShape(20.dp))
            .border(2.5.dp, FarmBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("profit_entries_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = sectionTitle,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = FarmTextPrimary
        )

        for (entry in entries) {
            val cropItem = CropItem.fromKey(entry.crop)
            val categoryItem = CategoryItem.fromKey(entry.category)
            val isSale = entry.isIncome || entry.category == "sale"
            val sign = if (isSale) "+" else "−"
            val amountColor = if (isSale) ProfitGreen else LossRed
            val rowBg = if (isSale) ProfitBg else LossBg

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowBg, RoundedCornerShape(16.dp))
                    .border(2.dp, amountColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("entry_row_${entry.id}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${cropItem.iconEmoji}${categoryItem.iconEmoji}",
                        fontSize = 24.sp
                    )
                    Column {
                        Text(
                            text = "${cropItem.getDisplayName(language)} • ${categoryItem.getDisplayName(language)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary
                        )
                        Text(
                            text = "$sign ${NumberToWords.formatCurrency(entry.amount, language)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = amountColor
                        )
                    }
                }

                // Bin Button to delete this specific calculation entry
                BounceableCard(
                    onClick = { onDeleteEntry(entry.id) },
                    backgroundColor = FarmSurface,
                    borderColor = LossRed,
                    borderWidth = 2.5.dp,
                    cornerRadius = 14.dp,
                    elevation = 2.dp,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("entry_bin_${entry.id}")
                        .semantics {
                            contentDescription = "Delete ${cropItem.getDisplayName(language)} ${categoryItem.getDisplayName(language)} ${entry.amount}"
                        }
                ) {
                    Text(
                        text = "🗑️",
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}

data class CropSummary(val income: Long, val expense: Long, val net: Long)

private fun computeSummary(entries: List<CropEntryEntity>, cropKey: String?): CropSummary {
    var inc = 0L
    var exp = 0L
    for (e in entries) {
        if (cropKey == null || e.crop.equals(cropKey, ignoreCase = true)) {
            if (e.isIncome || e.category == "sale") {
                inc += e.amount
            } else {
                exp += e.amount
            }
        }
    }
    return CropSummary(income = inc, expense = exp, net = inc - exp)
}

@Composable
private fun ProfitCard(
    crop: CropItem?,
    title: String,
    summary: CropSummary,
    language: Language,
    showBinButton: Boolean = false,
    onBinClick: () -> Unit = {}
) {
    val isProfit = summary.net >= 0
    val absNet = Math.abs(summary.net)
    val arrow = if (isProfit) "▲" else "▼"
    val statusWord = when (language) {
        Language.MR -> if (isProfit) "नफा" else "तोटा"
        Language.HI -> if (isProfit) "मुनाफा" else "नुकसान"
        Language.EN -> if (isProfit) "Profit" else "Loss"
    }

    val cardBg = if (isProfit) ProfitBg else LossBg
    val cardBorder = if (isProfit) ProfitGreen else LossRed
    val arrowColor = if (isProfit) ProfitGreen else LossRed

    val incomeLabel = when (language) {
        Language.MR -> "➕ विक्री (उत्पन्न)"
        Language.HI -> "➕ बिक्री (आमदनी)"
        Language.EN -> "➕ Sales (Income)"
    }
    val expenseLabel = when (language) {
        Language.MR -> "➖ एकूण खर्च"
        Language.HI -> "➖ कुल खर्च"
        Language.EN -> "➖ Total Cost"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBg, RoundedCornerShape(24.dp))
            .border(3.5.dp, cardBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("profit_card"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Crop Header + Bin Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (crop != null) {
                        CropVectorBadge(crop = crop, size = 48.dp)
                    }
                    Text(
                        text = title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = FarmTextPrimary
                    )
                }

                if (showBinButton) {
                    BounceableCard(
                        onClick = onBinClick,
                        backgroundColor = FarmSurface,
                        borderColor = LossRed,
                        borderWidth = 2.5.dp,
                        cornerRadius = 14.dp,
                        elevation = 2.dp,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("profit_card_bin_${crop?.key ?: "all"}")
                            .semantics { contentDescription = "Delete $title calculations" }
                    ) {
                        Text(
                            text = "🗑️",
                            fontSize = 24.sp
                        )
                    }
                }
            }

            // Giant Arrow + Huge Net Amount
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = arrow,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = arrowColor
                )
                Text(
                    text = NumberToWords.formatCurrency(absNet, language),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = FarmTextPrimary
                )
            }

            // Status Word and Spoken Words
            Text(
                text = "$statusWord • ${NumberToWords.convert(absNet, language)}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = arrowColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Visual Calculation Breakdown Box (Shows exact formula: Sales - Cost = Profit/Loss)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FarmSurface, RoundedCornerShape(14.dp))
                    .border(2.dp, cardBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = incomeLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FarmTextSecondary)
                    Text(
                        text = NumberToWords.formatCurrency(summary.income, language),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = ProfitGreen
                    )
                }
                Text(text = "−", fontSize = 26.sp, fontWeight = FontWeight.Black, color = FarmTextPrimary)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = expenseLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FarmTextSecondary)
                    Text(
                        text = NumberToWords.formatCurrency(summary.expense, language),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = LossRed
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakEvenPriceTagCard(
    crop: CropItem,
    totalCost: Long,
    quintals: Long,
    language: Language,
    onEditQuintals: () -> Unit,
    onClearQuintals: () -> Unit = {},
    onSpeakBreakEven: () -> Unit
) {
    val tagTitle = when (language) {
        Language.MR -> "🏷️ हमीभाव / किमान विक्री दर (${crop.getDisplayName(language)})"
        Language.HI -> "🏷️ न्यूनतम बिक्री भाव (${crop.getDisplayName(language)})"
        Language.EN -> "🏷️ Break-Even Price (${crop.getDisplayName(language)})"
    }

    val totalCostLabel = when (language) {
        Language.MR -> "एकूण खर्च: ${NumberToWords.formatCurrency(totalCost, language)}"
        Language.HI -> "कुल खर्च: ${NumberToWords.formatCurrency(totalCost, language)}"
        Language.EN -> "Total Cost: ${NumberToWords.formatCurrency(totalCost, language)}"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HarvestYellowBg, RoundedCornerShape(24.dp))
            .border(3.5.dp, HarvestYellowBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("breakeven_price_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Price Tag Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tagTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = HarvestYellowDarkText,
                    modifier = Modifier.weight(1f)
                )

                if (quintals > 0L) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BounceableCard(
                            onClick = onSpeakBreakEven,
                            backgroundColor = FarmSurface,
                            borderColor = HarvestYellowBorder,
                            cornerRadius = 12.dp,
                            elevation = 2.dp,
                            modifier = Modifier.testTag("breakeven_speak_button")
                        ) {
                            Text(
                                text = "🔊 ऐका",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = HarvestYellowDarkText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        BounceableCard(
                            onClick = onClearQuintals,
                            backgroundColor = FarmSurface,
                            borderColor = LossRed,
                            borderWidth = 2.dp,
                            cornerRadius = 12.dp,
                            elevation = 2.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("breakeven_bin_${crop.key}")
                                .semantics { contentDescription = "Delete harvest quintal calculation" }
                        ) {
                            Text(
                                text = "🗑️",
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

            // Total Cost Line
            Text(
                text = totalCostLabel,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = FarmTextPrimary
            )

            // Safe handling of zero or empty quintals vs valid quintals
            if (quintals > 0L) {
                val breakEvenPerQuintal = if (totalCost > 0L) (totalCost + quintals - 1) / quintals else 0L
                val headline = NumberToWords.buildBreakEvenHeadline(breakEvenPerQuintal, language)
                val formulaLine = "${NumberToWords.formatCurrency(totalCost, language)} ÷ ${NumberToWords.formatQuintals(quintals, language)} = ${NumberToWords.formatCurrency(breakEvenPerQuintal, language)} / क्विंटल"

                // Formula Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FarmSurface, RoundedCornerShape(14.dp))
                        .border(2.dp, HarvestYellowBorder, RoundedCornerShape(14.dp))
                        .padding(vertical = 6.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formulaLine,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                // Big Price Callout
                Text(
                    text = headline,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    color = HarvestYellowDarkText,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp,
                    modifier = Modifier.testTag("breakeven_headline_text")
                )

                // Edit Quintals Button
                val editQuintalsLabel = when (language) {
                    Language.MR -> "⚖️ उत्पादन बदला (${NumberToWords.formatQuintals(quintals, language)})"
                    Language.HI -> "⚖️ क्विंटल बदलें (${NumberToWords.formatQuintals(quintals, language)})"
                    Language.EN -> "⚖️ Change Harvest (${NumberToWords.formatQuintals(quintals, language)})"
                }
                BounceableCard(
                    onClick = onEditQuintals,
                    backgroundColor = FarmSurface,
                    borderColor = HarvestYellowBorder,
                    borderWidth = 2.5.dp,
                    cornerRadius = 16.dp,
                    elevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("breakeven_edit_quintal_button")
                ) {
                    Text(
                        text = editQuintalsLabel,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = FarmTextPrimary
                    )
                }
            } else {
                // Zero or empty quintals (or zero cost): show a big friendly button without errors
                val enterQuintalsPrompt = when (language) {
                    Language.MR -> "⚖️ अंदाजे उत्पादन (क्विंटल) टाका"
                    Language.HI -> "⚖️ अनुमानित उपज (क्विंटल) डालें"
                    Language.EN -> "⚖️ Enter Expected Harvest (Quintals)"
                }
                val helperSub = when (language) {
                    Language.MR -> "तोटा टाळण्यासाठी किमान किती भावाने विकावे हे पाहण्यासाठी दाबा"
                    Language.HI -> "नुकसान से बचने का न्यूनतम भाव जानने के लिए यहाँ दबाएं"
                    Language.EN -> "Tap to calculate minimum selling price per quintal to avoid loss"
                }

                Text(
                    text = helperSub,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarvestYellowDarkText,
                    textAlign = TextAlign.Center
                )

                BounceableCard(
                    onClick = onEditQuintals,
                    backgroundColor = FarmSurface,
                    borderColor = HarvestYellowBorder,
                    borderWidth = 3.dp,
                    cornerRadius = 18.dp,
                    elevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .testTag("breakeven_enter_quintal_button")
                ) {
                    Text(
                        text = enterQuintalsPrompt,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = HarvestYellowDarkText
                    )
                }
            }
        }
    }
}
