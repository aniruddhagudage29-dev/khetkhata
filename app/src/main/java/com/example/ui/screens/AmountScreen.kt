package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import com.example.model.CategoryItem
import com.example.model.CropItem
import com.example.model.Language
import com.example.model.NumberToWords
import com.example.ui.components.BounceableCard
import com.example.ui.components.CropVectorBadge
import com.example.ui.components.SaveCheckmarkCelebration
import com.example.ui.theme.FarmBorder
import com.example.ui.theme.FarmGreenAction
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenDark
import com.example.ui.theme.FarmSurface
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary
import com.example.ui.theme.HarvestYellowBg
import com.example.ui.theme.HarvestYellowBorder
import com.example.ui.theme.HarvestYellowDarkText
import com.example.ui.theme.LossBg
import com.example.ui.theme.LossRed

private val QUICK_CHIPS = listOf(500L, 1000L, 2000L, 5000L)
private val KEYPAD_ROWS = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf("C", "0", "DEL")
)

@Composable
fun AmountScreen(
    crop: CropItem,
    category: CategoryItem,
    amountStr: String,
    language: Language,
    isSaving: Boolean,
    showSaveCelebration: Boolean = false,
    onNumpadPress: (String) -> Unit,
    onChipPress: (Long) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanDigits = remember(amountStr) {
        amountStr.filter { it.isDigit() }.trimStart('0').ifEmpty { "0" }
    }
    val amountNum = remember(cleanDigits) { cleanDigits.toLongOrNull() ?: 0L }
    val isSaveEnabled = !isSaving && amountNum > 0L
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Static Context Pill (Only updates if crop, category, or language changes)
            AmountContextPill(crop = crop, category = category, language = language)

            Spacer(modifier = Modifier.height(4.dp))

            // Isolated Amount Display Box (Only this composable re-renders on digit/chip taps)
            AmountDisplayBox(
                amountNum = amountNum,
                isSale = category.isSale,
                language = language
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Static Quick Addition Chips (+500, +1000, +2000, +5000)
            AmountQuickChipsRow(
                language = language,
                onChipPress = onChipPress
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Static 3x4 Large Digit Pad (Fixed 64dp button heights, no re-render on amount change)
            AmountKeypadGrid(
                language = language,
                onNumpadPress = onNumpadPress
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Isolated Save Button (Only re-renders when isSaveEnabled or isSaving changes)
            AmountSaveButton(
                isSaveEnabled = isSaveEnabled,
                isSaving = isSaving,
                language = language,
                onSaveClick = onSaveClick
            )
        }

        // Fast <=150ms Transform + Opacity Checkmark Celebration Overlay
        AnimatedVisibility(
            visible = showSaveCelebration,
            enter = fadeIn(tween(100)) + scaleIn(tween(100), initialScale = 0.85f),
            exit = fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.9f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000)),
                contentAlignment = Alignment.Center
            ) {
                SaveCheckmarkCelebration(language = language)
            }
        }
    }
}

@Composable
private fun AmountContextPill(
    crop: CropItem,
    category: CategoryItem,
    language: Language
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(
                if (category.isSale) FarmGreenContainer else FarmSurface,
                RoundedCornerShape(16.dp)
            )
            .border(
                2.5.dp,
                if (category.isSale) FarmGreenAction else FarmBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CropVectorBadge(crop = crop, size = 34.dp)
        Text(
            text = " ${crop.getDisplayName(language)} ➔ ${category.iconEmoji} ${category.getDisplayName(language)}",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = FarmTextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AmountDisplayBox(
    amountNum: Long,
    isSale: Boolean,
    language: Language
) {
    val formattedCurrency = remember(amountNum, language) {
        NumberToWords.formatCurrency(amountNum, language)
    }
    val words = remember(amountNum, language) {
        NumberToWords.convert(amountNum, language)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(102.dp)
            .background(FarmSurface, RoundedCornerShape(20.dp))
            .border(3.dp, FarmBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp)
            .testTag("amount_display_box"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedCurrency,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = if (isSale) FarmGreenAction else FarmTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = words,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = FarmTextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AmountQuickChipsRow(
    language: Language,
    onChipPress: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (chip in QUICK_CHIPS) {
            val chipLabel = remember(chip, language) {
                "+" + NumberToWords.toLocalDigits(chip.toString(), language)
            }
            val onClickChip = remember(chip, onChipPress) { { onChipPress(chip) } }
            BounceableCard(
                onClick = onClickChip,
                backgroundColor = HarvestYellowBg,
                borderColor = HarvestYellowBorder,
                borderWidth = 2.5.dp,
                cornerRadius = 14.dp,
                elevation = 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .testTag("amount_chip_$chip")
                    .semantics { contentDescription = "$chipLabel रुपये जोडा" }
            ) {
                Text(
                    text = chipLabel,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                    color = HarvestYellowDarkText
                )
            }
        }
    }
}

@Composable
private fun AmountKeypadGrid(
    language: Language,
    onNumpadPress: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (row in KEYPAD_ROWS) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (key in row) {
                    val isClear = key == "C"
                    val isDel = key == "DEL"
                    val keyLabel = remember(key, language) {
                        when {
                            isClear -> "C"
                            isDel -> "⌫"
                            else -> NumberToWords.toLocalDigits(key, language)
                        }
                    }

                    val bgColor = when {
                        isClear -> LossBg
                        isDel -> Color(0xFFF5F5F4)
                        else -> FarmSurface
                    }
                    val borderColor = if (isClear) LossRed else FarmBorder
                    val textColor = if (isClear) LossRed else FarmTextPrimary
                    val onClickKey = remember(key, onNumpadPress) { { onNumpadPress(key) } }

                    BounceableCard(
                        onClick = onClickKey,
                        backgroundColor = bgColor,
                        borderColor = borderColor,
                        borderWidth = 2.5.dp,
                        cornerRadius = 16.dp,
                        elevation = 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("numpad_key_$key")
                            .semantics {
                                contentDescription = when {
                                    isClear -> "सर्व पुसा / Clear"
                                    isDel -> "एक अंक खोडा / Backspace"
                                    else -> "अंक $keyLabel"
                                }
                            }
                    ) {
                        Text(
                            text = keyLabel,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountSaveButton(
    isSaveEnabled: Boolean,
    isSaving: Boolean,
    language: Language,
    onSaveClick: () -> Unit
) {
    val saveLabel = when (language) {
        Language.MR -> "✔ जतन करा"
        Language.HI -> "✔ सेव करें"
        Language.EN -> "✔ Save"
    }

    BounceableCard(
        onClick = onSaveClick,
        enabled = isSaveEnabled,
        backgroundColor = if (isSaveEnabled) FarmGreenAction else Color(0xFF9CA3AF),
        borderColor = if (isSaveEnabled) FarmGreenDark else Color(0xFF6B7280),
        borderWidth = 3.5.dp,
        cornerRadius = 22.dp,
        elevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .testTag("save_entry_button")
            .semantics { contentDescription = saveLabel }
    ) {
        Text(
            text = if (isSaving) "✔" else saveLabel,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}
