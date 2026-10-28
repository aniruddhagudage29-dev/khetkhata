package com.example.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedTeachingHand(modifier: Modifier = Modifier) {
    Text(
        text = "👆",
        fontSize = 38.sp,
        modifier = modifier
    )
}
