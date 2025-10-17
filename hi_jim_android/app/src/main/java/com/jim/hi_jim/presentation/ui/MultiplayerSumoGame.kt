package com.jim.hi_jim.presentation.ui

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
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
import com.jim.hi_jim.shared.model.GameStatus
import com.jim.hi_jim.shared.model.MultiplayerGameData
import kotlinx.coroutines.launch

@Composable
fun MultiplayerSumoGameScreen(
    currentUserId: String,
    gameData: MultiplayerGameData?,
    onPlayerMove: () -> Unit,
    onNextRound: () -> Unit,
    onQuitGame: () -> Unit
) {
    if (gameData == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "게임 로딩 중...",
                fontSize = 14.sp,
                color = Color.White
            )
        }
        return
    }

    val gameState = gameData.toSumoGameState()
    val isPlayer1 = currentUserId == gameData.player1Id
    var buttonsEnabled by remember { mutableStateOf(true) }

    val player1AnimPos = remember { Animatable(gameState.player1Position) }
    val player2AnimPos = remember { Animatable(gameState.player2Position) }
    val collisionAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // 위치 애니메이션
    LaunchedEffect(gameState.player1Position, gameState.player2Position) {
        launch {
            player1AnimPos.animateTo(
                targetValue = gameState.player1Position,
                animationSpec = tween(
                    durationMillis = 150,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            )
        }
        launch {
            player2AnimPos.animateTo(
                targetValue = gameState.player2Position,
                animationSpec = tween(
                    durationMillis = 150,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            )
        }
    }

    // 충돌 이펙트 애니메이션
    LaunchedEffect(gameState.collisionTimestamp) {
        if (gameState.collisionPosition != null && gameState.collisionTimestamp > 0) {
            collisionAlpha.snapTo(1.0f)
            collisionAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutLinearInEasing
                )
            )
        }
    }

    // 승리 시 버튼 1.5초 동안 비활성화
    LaunchedEffect(gameState.gameStatus) {
        if (gameState.gameStatus == GameStatus.PLAYER1_WIN || gameState.gameStatus == GameStatus.PLAYER2_WIN) {
            buttonsEnabled = false
            kotlinx.coroutines.delay(1500)
            buttonsEnabled = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 클릭 영역 (전체 화면)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (gameState.gameStatus == GameStatus.PLAYING) {
                        Log.d("MultiplayerGame", "Player move: $currentUserId")
                        onPlayerMove()
                    }
                }
        )

        // 게임 화면
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // 스코어 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "🔵 ${gameState.player1Score}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A90E2)
                )
                Text(
                    text = "🔴 ${gameState.player2Score}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE85D75)
                )
            }

            Spacer(Modifier.height(10.dp))

            // 게임 캔버스
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                SumoGameCanvas(
                    player1Position = player1AnimPos.value,
                    player2Position = player2AnimPos.value,
                    gameStatus = gameState.gameStatus,
                    collisionPosition = gameState.collisionPosition,
                    collisionAlpha = collisionAlpha.value
                )
            }

            Spacer(Modifier.height(20.dp))
        }

        // 승리 화면 오버레이
        if (gameState.gameStatus == GameStatus.PLAYER1_WIN || gameState.gameStatus == GameStatus.PLAYER2_WIN) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    val winnerText = if (gameState.gameStatus == GameStatus.PLAYER1_WIN)
                        "Player 1\nWins!" else "Player 2\nWins!"
                    val winnerColor = if (gameState.gameStatus == GameStatus.PLAYER1_WIN)
                        Color(0xFF4A90E2) else Color(0xFFE85D75)

                    Text(
                        text = winnerText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = winnerColor,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(14.dp))

                    // 다음 라운드 버튼
                    Button(
                        onClick = { if (buttonsEnabled) onNextRound() },
                        modifier = Modifier
                            .height(35.dp)
                            .wrapContentWidth(),
                        enabled = buttonsEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text(
                            text = "NEXT ROUND",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    // 게임 나가기 버튼
                    Button(
                        onClick = { if (buttonsEnabled) onQuitGame() },
                        modifier = Modifier
                            .height(28.dp)
                            .wrapContentWidth(),
                        enabled = buttonsEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Gray
                        )
                    ) {
                        Text(
                            text = "QUIT GAME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
