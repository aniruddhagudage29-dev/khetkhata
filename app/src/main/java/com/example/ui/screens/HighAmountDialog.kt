package com.example.ui.screens

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
import androidx.compose.ui.window.Dialog
import com.example.model.Language
import com.example.model.NumberToWords
import com.example.ui.theme.FarmBorder
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmSurface
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.LossBg
import com.example.ui.theme.LossRed

@Composable
fun HighAmountDialog(
    amountStr: String,
    language: Language,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val amountNum = amountStr.toLongOrNull() ?: 0L
    val formatted = NumberToWords.formatCurrency(amountNum, language)
    val words = NumberToWords.convert(amountNum, language)

    val promptText = when (language) {
        Language.MR -> "रक्कम मोठी आहे. नक्की का?"
        Language.HI -> "रकम बड़ी है। क्या पक्का है?"
        Language.EN -> "Large amount. Are you sure?"
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FarmSurface, RoundedCornerShape(22.dp))
                .border(4.dp, FarmBorder, RoundedCornerShape(22.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = formatted,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = FarmTextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = words,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = promptText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = LossRed,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Cancel ✖
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .background(LossBg, RoundedCornerShape(16.dp))
                            .border(3.dp, LossRed, RoundedCornerShape(16.dp))
                            .clickable { onDismiss() }
                            .testTag("high_amount_cancel_button")
                            .semantics { contentDescription = "रद्द करा / Cancel" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✖",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = LossRed
                        )
                    }

                    // Confirm ✔
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .background(FarmGreenPrimary, RoundedCornerShape(16.dp))
                            .border(3.dp, Color(0xFF052E16), RoundedCornerShape(16.dp))
                            .clickable { onConfirm() }
                            .testTag("high_amount_confirm_button")
                            .semantics { contentDescription = "होय, जतन करा / Confirm" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✔",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
