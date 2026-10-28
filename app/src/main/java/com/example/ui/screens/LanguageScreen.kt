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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.Language
import com.example.ui.components.AnimatedTeachingHand
import com.example.ui.components.BounceableCard
import com.example.ui.theme.FarmBorder
import com.example.ui.theme.FarmGreenDark
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmSurface
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.HarvestYellow
import com.example.ui.theme.HarvestYellowBg
import com.example.ui.theme.HarvestYellowBorder

@Composable
fun LanguageScreen(
    hasTaught: Boolean,
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Brand Hero Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FarmGreenPrimary, RoundedCornerShape(24.dp))
                .border(3.dp, FarmGreenDark, RoundedCornerShape(24.dp))
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(HarvestYellowBg, CircleShape)
                    .border(3.dp, HarvestYellow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌾", fontSize = 40.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "शेतखाते • KhetKhata",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🗣️ तुमची भाषा निवडा / भाषा चुनें / Select Language",
                color = HarvestYellowBg,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Marathi Button (Default with Harvest Gold highlight + animated hand)
        BounceableCard(
            onClick = { onLanguageSelected(Language.MR) },
            backgroundColor = HarvestYellowBg,
            borderColor = HarvestYellowBorder,
            borderWidth = 3.5.dp,
            cornerRadius = 24.dp,
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(98.dp)
                .testTag("lang_button_mr")
                .semantics { contentDescription = "मराठी भाषा निवडा" }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "🚩", fontSize = 36.sp)
                Text(
                    text = "मराठी",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = FarmTextPrimary
                )
            }
            if (!hasTaught) {
                AnimatedTeachingHand(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Hindi Button
        BounceableCard(
            onClick = { onLanguageSelected(Language.HI) },
            backgroundColor = FarmSurface,
            borderColor = FarmBorder,
            borderWidth = 3.dp,
            cornerRadius = 24.dp,
            elevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp)
                .testTag("lang_button_hi")
                .semantics { contentDescription = "हिंदी भाषा चुनें" }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "🇮🇳", fontSize = 34.sp)
                Text(
                    text = "हिंदी",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = FarmTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. English Button
        BounceableCard(
            onClick = { onLanguageSelected(Language.EN) },
            backgroundColor = FarmSurface,
            borderColor = FarmBorder,
            borderWidth = 3.dp,
            cornerRadius = 24.dp,
            elevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp)
                .testTag("lang_button_en")
                .semantics { contentDescription = "Select English" }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "🔤", fontSize = 32.sp)
                Text(
                    text = "English",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = FarmTextPrimary
                )
            }
        }
    }
}
