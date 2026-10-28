package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.components.AnimatedTeachingHand
import com.example.ui.components.BounceableCard
import com.example.ui.components.CropVectorBadge
import com.example.ui.theme.FarmBorder
import com.example.ui.theme.FarmGreenAction
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenDark
import com.example.ui.theme.FarmSurface
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary

@Composable
fun CategoryScreen(
    crop: CropItem,
    language: Language,
    hasTaught: Boolean,
    onCategorySelected: (CategoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val expenseCategories = listOf(
        CategoryItem.SEED,
        CategoryItem.FERTILIZER,
        CategoryItem.PESTICIDE,
        CategoryItem.LABOUR,
        CategoryItem.DIESEL,
        CategoryItem.WATER,
        CategoryItem.TRANSPORT,
        CategoryItem.OTHER
    )
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Active Selected Crop Header Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(crop.cardTint, RoundedCornerShape(18.dp))
                .border(2.5.dp, crop.accentColor, RoundedCornerShape(18.dp))
                .padding(vertical = 6.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CropVectorBadge(crop = crop, size = 40.dp)
            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
            Text(
                text = crop.getDisplayName(language),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = FarmTextPrimary
            )
        }

        // Prominent Wide SALE Tile (Income)
        val saleCat = CategoryItem.SALE
        BounceableCard(
            onClick = { onCategorySelected(saleCat) },
            backgroundColor = FarmGreenContainer,
            borderColor = FarmGreenAction,
            borderWidth = 3.dp,
            cornerRadius = 22.dp,
            elevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .testTag("category_tile_sale")
                .semantics {
                    contentDescription = "➕ ${saleCat.getDisplayName(language)}"
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = saleCat.iconEmoji, fontSize = 38.sp)
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "➕ ${saleCat.getDisplayName(language)}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = FarmGreenDark
                    )
                    Text(
                        text = saleCat.getActionSubtitle(language),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmGreenDark
                    )
                }
            }
        }

        // 2-Column Expense Grid (4 rows of 2 rounded cards)
        for (i in 0 until 4) {
            val cat1 = expenseCategories[i * 2]
            val cat2 = expenseCategories[i * 2 + 1]

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Item 1
                BounceableCard(
                    onClick = { onCategorySelected(cat1) },
                    backgroundColor = FarmSurface,
                    borderColor = FarmBorder,
                    borderWidth = 2.5.dp,
                    cornerRadius = 22.dp,
                    elevation = 5.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .testTag("category_tile_${cat1.key}")
                        .semantics {
                            contentDescription = "➖ ${cat1.getDisplayName(language)}"
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = cat1.iconEmoji, fontSize = 34.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "➖ ${cat1.getDisplayName(language)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (!hasTaught && i == 0) {
                        AnimatedTeachingHand(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 6.dp, bottom = 2.dp)
                        )
                    }
                }

                // Item 2
                BounceableCard(
                    onClick = { onCategorySelected(cat2) },
                    backgroundColor = FarmSurface,
                    borderColor = FarmBorder,
                    borderWidth = 2.5.dp,
                    cornerRadius = 22.dp,
                    elevation = 5.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .testTag("category_tile_${cat2.key}")
                        .semantics {
                            contentDescription = "➖ ${cat2.getDisplayName(language)}"
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = cat2.iconEmoji, fontSize = 34.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "➖ ${cat2.getDisplayName(language)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
