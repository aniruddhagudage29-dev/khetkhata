package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CropItem
import com.example.model.Language
import com.example.ui.theme.FarmBorder
import com.example.ui.theme.FarmGreenAction
import com.example.ui.theme.FarmGreenDark

@Composable
fun BounceableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = FarmBorder,
    borderWidth: Dp = 2.5.dp,
    cornerRadius: Dp = 22.dp,
    elevation: Dp = 5.dp,
    enabled: Boolean = true,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = tween(durationMillis = 80),
        label = "cardPressScale"
    )

    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(backgroundColor, shape)
            .border(borderWidth, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = contentAlignment,
        content = content
    )
}

@Composable
fun CropVectorBadge(
    crop: CropItem,
    size: Dp = 58.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(crop.cardTint, CircleShape)
            .border(2.dp, crop.accentColor.copy(alpha = 0.65f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.78f)) {
            val w = this.size.width
            val h = this.size.height
            when (crop) {
                CropItem.ONION -> {
                    // Rich purple-red onion bulb with green top shoots
                    drawLine(
                        color = Color(0xFF15803D),
                        start = Offset(w * 0.5f, h * 0.25f),
                        end = Offset(w * 0.38f, h * 0.05f),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF15803D),
                        start = Offset(w * 0.5f, h * 0.25f),
                        end = Offset(w * 0.5f, h * 0.02f),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF15803D),
                        start = Offset(w * 0.5f, h * 0.25f),
                        end = Offset(w * 0.62f, h * 0.05f),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                    val bulbPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.18f)
                        cubicTo(w * 0.12f, h * 0.42f, w * 0.12f, h * 0.90f, w * 0.5f, h * 0.92f)
                        cubicTo(w * 0.88f, h * 0.90f, w * 0.88f, h * 0.42f, w * 0.5f, h * 0.18f)
                        close()
                    }
                    drawPath(bulbPath, color = Color(0xFF9D174D))
                    drawPath(bulbPath, color = Color(0xFF4C0519), style = Stroke(width = 3.5f))
                }
                CropItem.SOYBEAN -> {
                    // Lush green pod with three golden soybeans
                    val podPath = Path().apply {
                        moveTo(w * 0.15f, h * 0.78f)
                        quadraticTo(w * 0.20f, h * 0.20f, w * 0.85f, h * 0.18f)
                        quadraticTo(w * 0.80f, h * 0.75f, w * 0.15f, h * 0.78f)
                        close()
                    }
                    drawPath(podPath, color = Color(0xFF65A30D))
                    drawPath(podPath, color = Color(0xFF1A2E05), style = Stroke(width = 3.5f))
                    drawCircle(color = Color(0xFFFDE047), radius = w * 0.10f, center = Offset(w * 0.35f, h * 0.58f))
                    drawCircle(color = Color(0xFFFDE047), radius = w * 0.10f, center = Offset(w * 0.52f, h * 0.45f))
                    drawCircle(color = Color(0xFFFDE047), radius = w * 0.09f, center = Offset(w * 0.68f, h * 0.33f))
                }
                CropItem.COTTON -> {
                    // Fluffy white cotton puffs with brown calyx base
                    drawLine(
                        color = Color(0xFF78350F),
                        start = Offset(w * 0.5f, h * 0.62f),
                        end = Offset(w * 0.5f, h * 0.94f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = Color.White, radius = w * 0.22f, center = Offset(w * 0.35f, h * 0.46f))
                    drawCircle(color = Color.White, radius = w * 0.22f, center = Offset(w * 0.65f, h * 0.46f))
                    drawCircle(color = Color.White, radius = w * 0.23f, center = Offset(w * 0.50f, h * 0.30f))
                    drawCircle(color = Color(0xFF78350F), radius = w * 0.22f, center = Offset(w * 0.35f, h * 0.46f), style = Stroke(width = 3f))
                    drawCircle(color = Color(0xFF78350F), radius = w * 0.22f, center = Offset(w * 0.65f, h * 0.46f), style = Stroke(width = 3f))
                    drawCircle(color = Color(0xFF78350F), radius = w * 0.23f, center = Offset(w * 0.50f, h * 0.30f), style = Stroke(width = 3f))
                }
                CropItem.SUGARCANE -> {
                    // Two segmented green sugarcane stalks with top leaves
                    drawRoundRect(
                        color = Color(0xFF15803D),
                        topLeft = Offset(w * 0.26f, h * 0.18f),
                        size = Size(w * 0.16f, h * 0.74f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = Color(0xFF16A34A),
                        topLeft = Offset(w * 0.56f, h * 0.14f),
                        size = Size(w * 0.16f, h * 0.78f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    // Yellow nodes
                    for (yFactor in listOf(0.38f, 0.58f, 0.76f)) {
                        drawLine(
                            color = Color(0xFFFEF08A),
                            start = Offset(w * 0.24f, h * yFactor),
                            end = Offset(w * 0.44f, h * yFactor),
                            strokeWidth = 4f
                        )
                        drawLine(
                            color = Color(0xFFFEF08A),
                            start = Offset(w * 0.54f, h * yFactor),
                            end = Offset(w * 0.74f, h * yFactor),
                            strokeWidth = 4f
                        )
                    }
                }
                CropItem.TUR -> {
                    // Golden harvest tur dal grains and green branch
                    drawLine(
                        color = Color(0xFF15803D),
                        start = Offset(w * 0.2f, h * 0.85f),
                        end = Offset(w * 0.8f, h * 0.2f),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = Color(0xFFF59E0B), radius = w * 0.14f, center = Offset(w * 0.35f, h * 0.38f))
                    drawCircle(color = Color(0xFFD97706), radius = w * 0.14f, center = Offset(w * 0.65f, h * 0.62f))
                    drawCircle(color = Color(0xFFFBBF24), radius = w * 0.13f, center = Offset(w * 0.58f, h * 0.28f))
                }
                CropItem.OTHER -> {
                    // Vibrant dual-leaf sprout
                    drawLine(
                        color = Color(0xFF15803D),
                        start = Offset(w * 0.5f, h * 0.88f),
                        end = Offset(w * 0.5f, h * 0.32f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    drawOval(
                        color = Color(0xFF22C55E),
                        topLeft = Offset(w * 0.16f, h * 0.28f),
                        size = Size(w * 0.34f, h * 0.24f)
                    )
                    drawOval(
                        color = Color(0xFF16A34A),
                        topLeft = Offset(w * 0.50f, h * 0.18f),
                        size = Size(w * 0.34f, h * 0.24f)
                    )
                }
            }
        }
        // Subtle emoji badge in bottom-right corner for instant dual recognition
        Text(
            text = crop.iconEmoji,
            fontSize = (size.value * 0.36f).sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(1.dp)
        )
    }
}

@Composable
fun SaveCheckmarkCelebration(
    language: Language,
    modifier: Modifier = Modifier
) {
    val label = when (language) {
        Language.MR -> "जतन झाले!"
        Language.HI -> "सेव हो गया!"
        Language.EN -> "Saved!"
    }
    Column(
        modifier = modifier
            .background(FarmGreenAction, RoundedCornerShape(28.dp))
            .border(4.dp, FarmGreenDark, RoundedCornerShape(28.dp))
            .padding(horizontal = 36.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✔",
                fontSize = 54.sp,
                fontWeight = FontWeight.Black,
                color = FarmGreenAction
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}
