package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Language
import com.example.ui.theme.FarmBorder
import com.example.ui.theme.FarmGreenAction
import com.example.ui.theme.FarmGreenDark
import com.example.ui.theme.FarmSurface
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary
import com.example.ui.theme.LossBg
import com.example.ui.theme.LossRed
import kotlinx.coroutines.delay

enum class PendingResetAction {
    NONE,
    CLEAR_ENTRIES,
    FULL_RESET
}

@Composable
fun FamilyHelperDialog(
    language: Language,
    isResetting: Boolean = false,
    onExportCopy: (onReady: (String, String) -> Unit) -> Unit,
    onImportJson: (String) -> Unit,
    onClearEntries: () -> Unit,
    onFullReset: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pendingAction by remember { mutableStateOf(PendingResetAction.NONE) }
    var isHoldingConfirm by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (content.isNotBlank()) {
                    onImportJson(content)
                }
            } catch (_: Exception) {}
        }
    }

    // 3-second hold timer for confirmation step
    LaunchedEffect(isHoldingConfirm, pendingAction, isResetting) {
        if (isHoldingConfirm && pendingAction != PendingResetAction.NONE && !isResetting) {
            val stepTime = 50L
            val totalTime = 3000L
            var elapsed = 0L
            while (isHoldingConfirm && elapsed < totalTime && !isResetting) {
                delay(stepTime)
                elapsed += stepTime
                holdProgress = (elapsed.toFloat() / totalTime).coerceAtMost(1f)
            }
            if (isHoldingConfirm && holdProgress >= 1f && !isResetting) {
                val actionToRun = pendingAction
                isHoldingConfirm = false
                holdProgress = 0f
                pendingAction = PendingResetAction.NONE
                when (actionToRun) {
                    PendingResetAction.CLEAR_ENTRIES -> onClearEntries()
                    PendingResetAction.FULL_RESET -> onFullReset()
                    PendingResetAction.NONE -> {}
                }
            }
        } else if (!isHoldingConfirm) {
            holdProgress = 0f
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = holdProgress,
        animationSpec = tween(50),
        label = "holdAnim"
    )

    val title = when (language) {
        Language.MR -> "कुटुंब मदत साधने"
        Language.HI -> "परिवार मदद टूल्स"
        Language.EN -> "Family Helper Tools"
    }

    val saveCopyFirstLabel = when (language) {
        Language.MR -> "📥 आधी प्रत जतन करा (Save a copy first)"
        Language.HI -> "📥 पहले कॉपी सेव करें (Save a copy first)"
        Language.EN -> "📥 Save a copy first (JSON)"
    }

    val loadCopyLabel = when (language) {
        Language.MR -> "📤 प्रत भरा (Load Copy)"
        Language.HI -> "📤 कॉपी लोड करें (Load Copy)"
        Language.EN -> "📤 Load Copy (Restore JSON)"
    }

    val clearEntriesLabel = when (language) {
        Language.MR -> "🧹 माझ्या नोंदी पुसा (Clear my entries)"
        Language.HI -> "🧹 मेरी प्रविष्टियाँ मिटाएं (Clear my entries)"
        Language.EN -> "🧹 Clear my entries"
    }

    val fullResetLabel = when (language) {
        Language.MR -> "🔄 पूर्ण रीसेट करा (Full reset)"
        Language.HI -> "🔄 पूरा रीसेट करें (Full reset)"
        Language.EN -> "🔄 Full reset (for testing)"
    }

    val triggerSaveCopy = {
        if (!isResetting) {
            onExportCopy { json, _ ->
                try {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, json)
                        type = "application/json"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Save a copy first (JSON)")
                    context.startActivity(shareIntent)
                } catch (_: Exception) {}
            }
        }
    }

    Dialog(onDismissRequest = { if (!isResetting) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FarmSurface, RoundedCornerShape(22.dp))
                .border(4.dp, FarmBorder, RoundedCornerShape(22.dp))
                .padding(18.dp)
                .testTag("family_helper_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = FarmTextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .border(2.dp, FarmBorder, RoundedCornerShape(10.dp))
                            .clickable(enabled = !isResetting) { onDismiss() }
                            .padding(8.dp)
                            .testTag("family_dialog_close")
                    ) {
                        Text(text = "✖", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (pendingAction == PendingResetAction.NONE) {
                    // Button 1: Save a copy first (JSON)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .background(Color(0xFFE0F2FE), RoundedCornerShape(16.dp))
                            .border(3.dp, Color(0xFF0284C7), RoundedCornerShape(16.dp))
                            .clickable(enabled = !isResetting) { triggerSaveCopy() }
                            .testTag("family_btn_save_copy")
                            .semantics { contentDescription = saveCopyFirstLabel },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = saveCopyFirstLabel,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0369A1),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Button 2: Load Copy
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(16.dp))
                            .border(3.dp, Color(0xFFD97706), RoundedCornerShape(16.dp))
                            .clickable(enabled = !isResetting) {
                                filePickerLauncher.launch("application/json")
                            }
                            .testTag("family_btn_load_copy")
                            .semantics { contentDescription = loadCopyLabel },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = loadCopyLabel,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF92400E),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Reset Action 1: Clear my entries
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .background(LossBg, RoundedCornerShape(16.dp))
                            .border(3.dp, LossRed, RoundedCornerShape(16.dp))
                            .clickable(enabled = !isResetting) {
                                holdProgress = 0f
                                isHoldingConfirm = false
                                pendingAction = PendingResetAction.CLEAR_ENTRIES
                            }
                            .testTag("family_btn_clear_entries")
                            .semantics { contentDescription = clearEntriesLabel },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = clearEntriesLabel,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = LossRed,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Reset Action 2: Full reset (for testing)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .background(LossBg, RoundedCornerShape(16.dp))
                            .border(3.dp, LossRed, RoundedCornerShape(16.dp))
                            .clickable(enabled = !isResetting) {
                                holdProgress = 0f
                                isHoldingConfirm = false
                                pendingAction = PendingResetAction.FULL_RESET
                            }
                            .testTag("family_btn_full_reset")
                            .semantics { contentDescription = fullResetLabel },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fullResetLabel,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = LossRed,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Safety Confirm Step
                    val confirmHeading = when (pendingAction) {
                        PendingResetAction.CLEAR_ENTRIES -> clearEntriesLabel
                        PendingResetAction.FULL_RESET -> fullResetLabel
                        PendingResetAction.NONE -> ""
                    }
                    val confirmSub = when (language) {
                        Language.MR -> "पुसण्यापूर्वी प्रत जतन करा किंवा ३ सेकंद लाल बटण दाबून ठेवा"
                        Language.HI -> "मिटाने से पहले कॉपी सेव करें या लाल बटन 3 सेकंड दबाए रखें"
                        Language.EN -> "Save a copy first, or hold the red button for 3 seconds"
                    }
                    val backGreenLabel = when (language) {
                        Language.MR -> "⬅️ मागे (Back)"
                        Language.HI -> "⬅️ वापस (Back)"
                        Language.EN -> "⬅️ Back"
                    }
                    val holdRedLabel = when (language) {
                        Language.MR -> "🗑️ ३ सेकंद दाबून ठेवा (Hold 3s)"
                        Language.HI -> "🗑️ 3 सेकंड दबाए रखें (Hold 3s)"
                        Language.EN -> "🗑️ Hold 3 seconds to confirm"
                    }

                    Text(
                        text = confirmHeading,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = LossRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_reset_heading")
                    )

                    Text(
                        text = confirmSub,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Offer "Save a copy first" before clearing
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(Color(0xFFE0F2FE), RoundedCornerShape(16.dp))
                            .border(3.dp, Color(0xFF0284C7), RoundedCornerShape(16.dp))
                            .clickable(enabled = !isResetting) { triggerSaveCopy() }
                            .testTag("confirm_save_copy_first_button")
                            .semantics { contentDescription = saveCopyFirstLabel },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = saveCopyFirstLabel,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0369A1),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Green Back Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .background(FarmGreenAction, RoundedCornerShape(16.dp))
                            .border(3.dp, FarmGreenDark, RoundedCornerShape(16.dp))
                            .clickable(enabled = !isResetting) {
                                isHoldingConfirm = false
                                holdProgress = 0f
                                pendingAction = PendingResetAction.NONE
                            }
                            .testTag("confirm_back_button")
                            .semantics { contentDescription = backGreenLabel },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = backGreenLabel,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    // Red 3-Second Hold Button with Filling Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .background(LossBg, RoundedCornerShape(16.dp))
                            .border(3.dp, LossRed, RoundedCornerShape(16.dp))
                            .pointerInput(pendingAction, isResetting) {
                                if (!isResetting) {
                                    detectTapGestures(
                                        onPress = {
                                            isHoldingConfirm = true
                                            tryAwaitRelease()
                                            isHoldingConfirm = false
                                        }
                                    )
                                }
                            }
                            .testTag("confirm_hold_reset_button")
                            .semantics { contentDescription = holdRedLabel },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Filling Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .background(LossRed.copy(alpha = 0.35f), RoundedCornerShape(13.dp))
                                .testTag("confirm_hold_progress_bar")
                        )

                        Text(
                            text = holdRedLabel,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = LossRed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
