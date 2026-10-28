package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CropItem
import com.example.model.Language
import com.example.ui.components.AnimatedTeachingHand
import com.example.ui.components.BounceableCard
import com.example.ui.components.CropVectorBadge
import com.example.ui.theme.FarmBorder
import com.example.ui.theme.FarmSurface
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.HarvestYellowBg
import com.example.ui.theme.HarvestYellowBorder
import com.example.ui.theme.HarvestYellowDarkText

@Composable
fun HomeScreen(
    language: Language,
    hasTaught: Boolean,
    onCropSelected: (CropItem) -> Unit,
    onMyProfitClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onFamilyClick: () -> Unit,
    onAdvisorClick: () -> Unit,
    onHowToUseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val crops = CropItem.entries

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 2-Column Crop Grid (3 rows of 2 large rounded cards)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (row in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0 until 2) {
                        val index = row * 2 + col
                        val crop = crops[index]
                        val showHand = (!hasTaught && index == 0)

                        BounceableCard(
                            onClick = { onCropSelected(crop) },
                            backgroundColor = FarmSurface,
                            borderColor = crop.accentColor,
                            borderWidth = 2.5.dp,
                            cornerRadius = 22.dp,
                            elevation = 6.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(114.dp)
                                .testTag("crop_tile_${crop.key}")
                                .semantics {
                                    contentDescription = "${crop.getDisplayName(language)} पीक निवडा"
                                }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CropVectorBadge(crop = crop, size = 56.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                // Strict One-Word Label in 25sp+ bold font
                                Text(
                                    text = crop.getDisplayName(language),
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Black,
                                    color = FarmTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (showHand) {
                                AnimatedTeachingHand(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 8.dp, bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Big Harvest-Yellow "माझा नफा (My Profit)" Button
        val profitLabel = when (language) {
            Language.MR -> "💰 माझा नफा"
            Language.HI -> "💰 मेरा मुनाफा"
            Language.EN -> "💰 My Profit"
        }

        BounceableCard(
            onClick = onMyProfitClick,
            backgroundColor = HarvestYellowBg,
            borderColor = HarvestYellowBorder,
            borderWidth = 3.dp,
            cornerRadius = 22.dp,
            elevation = 7.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .testTag("home_my_profit_button")
                .semantics { contentDescription = "$profitLabel पहा" }
        ) {
            Text(
                text = profitLabel,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = HarvestYellowDarkText
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Bar: Language Toggle Only
        BounceableCard(
            onClick = onLanguageClick,
            backgroundColor = FarmSurface,
            borderColor = FarmBorder,
            borderWidth = 2.5.dp,
            cornerRadius = 18.dp,
            elevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("home_lang_toggle")
                .semantics { contentDescription = "🌐 ${language.displayName}" }
        ) {
            Text(
                text = "🌐 ${language.displayName}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = FarmTextPrimary
            )
        }
    }
}
