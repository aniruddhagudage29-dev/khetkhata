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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.advisor.AdvisorMessage
import com.example.model.Language
import com.example.ui.theme.FarmBorder
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenDark
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmSurface
import com.example.ui.theme.FarmTextPrimary

@Composable
fun KisanMitraDialog(
    language: Language,
    chatMessages: List<AdvisorMessage>,
    onAskQuestion: (String) -> Unit,
    onSpeakMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var queryText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val title = when (language) {
        Language.MR -> "🧑‍🌾 शेतकरी मित्र (कृषी सल्ला)"
        Language.HI -> "🧑‍🌾 किसान मित्र (कृषि सलाह)"
        Language.EN -> "🧑‍🌾 Farmer Advisor"
    }

    val quickQuestions = when (language) {
        Language.MR -> listOf("खत नियोजन", "पाणी सिंचन", "कीड व फवारणी", "बाजारभाव")
        Language.HI -> listOf("खाद प्रबंधन", "सिंचाई सलाह", "कीट व रोग", "मंडी भाव")
        Language.EN -> listOf("Fertilizer", "Irrigation", "Pest control", "Market rates")
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FarmSurface, RoundedCornerShape(22.dp))
                .border(4.dp, FarmBorder, RoundedCornerShape(22.dp))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = FarmGreenDark
                    )

                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .border(2.dp, FarmBorder, RoundedCornerShape(10.dp))
                            .clickable { onDismiss() }
                            .padding(6.dp)
                            .testTag("advisor_dialog_close")
                    ) {
                        Text(text = "✖", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Chat Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatMessages) { msg ->
                        val isAdvisor = msg.sender == "advisor"
                        val bubbleBg = if (isAdvisor) FarmGreenContainer else Color(0xFFDBEAFE)
                        val align = if (isAdvisor) Alignment.Start else Alignment.End

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = align
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .background(bubbleBg, RoundedCornerShape(14.dp))
                                    .border(2.dp, FarmBorder, RoundedCornerShape(14.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = msg.text,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FarmTextPrimary,
                                        lineHeight = 23.sp
                                    )
                                    if (isAdvisor) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.White, CircleShape)
                                                    .border(1.5.dp, FarmBorder, CircleShape)
                                                    .clickable { onSpeakMessage(msg.text) }
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(text = "🔊 ऐका", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Suggestion Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (q in quickQuestions) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                                .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape(10.dp))
                                .clickable {
                                    onAskQuestion(q)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = q,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }

                // Input Bar with Send Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("advisor_input_field"),
                        placeholder = {
                            Text(
                                text = when (language) {
                                    Language.MR -> "येथे प्रश्न विचारा..."
                                    Language.HI -> "यहाँ प्रश्न पूछें..."
                                    Language.EN -> "Ask a question..."
                                },
                                fontSize = 16.sp
                            )
                        },
                        singleLine = true
                    )

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(FarmGreenPrimary, RoundedCornerShape(14.dp))
                            .border(2.5.dp, FarmBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                if (queryText.isNotBlank()) {
                                    onAskQuestion(queryText)
                                    queryText = ""
                                }
                            }
                            .testTag("advisor_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "➔", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }
    }
}
