package com.jim.hi_jim.presentation.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text

@Composable
fun MainMenuScreen(
    onSumoGameClick: () -> Unit,
    onMukjjippaGameClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "게임 선택",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 스모 게임 버튼
            Button(
                onClick = onSumoGameClick,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF4A90E2)
                )
            ) {
                Text(
                    text = "스모 게임",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // 묵찌빠 게임 버튼
            Button(
                onClick = onMukjjippaGameClick,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF2E8B57)
                )
            ) {
                Text(
                    text = "묵찌빠 게임",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
