package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.example.ui.ScreenState
import com.example.ui.theme.ChipBlue
import com.example.ui.theme.FarmBorder
import com.example.ui.theme.FarmGreenDark
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmSurface
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.HarvestYellow
import com.example.ui.theme.HarvestYellowBg
import com.example.ui.theme.HarvestYellowBorder
import com.example.ui.theme.HarvestYellowDarkText
import com.example.ui.theme.LossRed

@Composable
fun KhetKhataTopBar(
    screen: ScreenState,
    language: Language,
    instructionText: String,
    isOnline: Boolean,
    isListening: Boolean,
    onBackClick: () -> Unit,
    onMicClick: () -> Unit,
    onSpeakerClick: () -> Unit,
    onOpenFamilyHelper: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val appBrandTitle = when (language) {
        Language.MR -> "शेतखाते"
        Language.HI -> "खेतखाता"
        Language.EN -> "KhetKhata"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Big Friendly Consumer Header Card (Deep Forest Green)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FarmGreenPrimary, RoundedCornerShape(20.dp))
                .border(2.5.dp, FarmGreenDark, RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Back Button or Brand Sprout Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (screen != ScreenState.HOME && screen != ScreenState.LANG) {
                    BounceableCard(
                        onClick = onBackClick,
                        backgroundColor = HarvestYellowBg,
                        borderColor = HarvestYellowBorder,
                        cornerRadius = 14.dp,
                        elevation = 2.dp,
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("topbar_back_button")
                            .semantics { contentDescription = "मागे जा / Back" }
                    ) {
                        Text(text = "⬅️", fontSize = 26.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(HarvestYellowBg, CircleShape)
                            .border(2.dp, HarvestYellow, CircleShape)
                            .clickable { onOpenFamilyHelper() }
                            .testTag("hidden_family_helper_trigger")
                            .semantics { contentDescription = "Family Helper Tools" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌾", fontSize = 26.sp)
                    }
                }

                // App Name + Online/Offline Small Dot (No text on dot)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = appBrandTitle,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    // Small green/grey dot (no text)
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                if (isOnline) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                                CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape)
                            .testTag("network_status_dot")
                            .semantics {
                                contentDescription = if (isOnline) "Online" else "Offline"
                            }
                    )
                }
            }

            // Right: Voice Mic (when online) + Speaker Replay Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isOnline) {
                    BounceableCard(
                        onClick = onMicClick,
                        backgroundColor = if (isListening) LossRed else ChipBlue,
                        borderColor = Color.White,
                        cornerRadius = 28.dp,
                        elevation = 0.dp,
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("topbar_mic_button")
                            .semantics { contentDescription = "आवाज इनपुट / Voice input" }
                    ) {
                        Text(text = "🎤", fontSize = 24.sp)
                    }
                }

                BounceableCard(
                    onClick = onSpeakerClick,
                    backgroundColor = HarvestYellowBg,
                    borderColor = HarvestYellow,
                    cornerRadius = 14.dp,
                    elevation = 3.dp,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("topbar_speaker_button")
                        .semantics { contentDescription = "सूचना ऐका / Replay instruction" }
                ) {
                    Text(text = "❓🔊", fontSize = 20.sp)
                }
            }
        }

        // 2. Visual Step Tracker + Current Instruction Guidance Banner
        val step1Label = when (language) {
            Language.MR -> "१. पीक"
            Language.HI -> "१. फसल"
            Language.EN -> "1. Crop"
        }
        val step2Label = when (language) {
            Language.MR -> "२. काय?"
            Language.HI -> "२. किस लिए?"
            Language.EN -> "2. What"
        }
        val step3Label = when (language) {
            Language.MR -> "३. रक्कम"
            Language.HI -> "३. रकम"
            Language.EN -> "3. Amount"
        }

        val activeStep = when (screen) {
            ScreenState.HOME -> 1
            ScreenState.CATEGORY -> 2
            ScreenState.AMOUNT -> 3
            ScreenState.PROFIT -> 4
            ScreenState.LANG -> 0
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FarmSurface, RoundedCornerShape(16.dp))
                .border(2.dp, FarmBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (activeStep in 1..3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepPill(text = step1Label, isActive = activeStep == 1, isDone = activeStep > 1)
                    Text(text = "➔", fontSize = 16.sp, fontWeight = FontWeight.Black, color = FarmTextPrimary)
                    StepPill(text = step2Label, isActive = activeStep == 2, isDone = activeStep > 2)
                    Text(text = "➔", fontSize = 16.sp, fontWeight = FontWeight.Black, color = FarmTextPrimary)
                    StepPill(text = step3Label, isActive = activeStep == 3, isDone = false)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Spoken & Visual Instruction Line
            Text(
                text = instructionText,
                color = FarmTextPrimary,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StepPill(
    text: String,
    isActive: Boolean,
    isDone: Boolean
) {
    val bg = when {
        isActive -> HarvestYellowBg
        isDone -> Color(0xFFDCFCE7)
        else -> Color(0xFFF5F5F4)
    }
    val border = when {
        isActive -> HarvestYellowBorder
        isDone -> Color(0xFF15803D)
        else -> Color(0xFFD6D3D1)
    }
    val prefix = if (isDone) "✔ " else ""

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(10.dp))
            .border(if (isActive) 2.dp else 1.dp, border, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$prefix$text",
            fontSize = 15.sp,
            fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
            color = HarvestYellowDarkText
        )
    }
}
