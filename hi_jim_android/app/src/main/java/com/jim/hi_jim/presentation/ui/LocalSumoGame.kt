package com.jim.hi_jim.presentation.ui

import android.util.Log
import com.jim.hi_jim.shared.engine.SumoPhysicsEngine
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Text
import com.jim.hi_jim.shared.model.GameStatus
import com.jim.hi_jim.shared.model.SumoGameState
import kotlinx.coroutines.launch

@Composable
fun LocalSumoGameScreen() {
    var gameState by remember { mutableStateOf(SumoGameState()) }
    val engine = remember { SumoPhysicsEngine() }
    var buttonsEnabled by remember { mutableStateOf(true) }

    val player1AnimPos = remember { Animatable(gameState.player1Position) }
    val player2AnimPos = remember { Animatable(gameState.player2Position) }
    val collisionAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

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
            // 방사형 스파크 이펙트: 1.0 -> 0.0으로 빠르게 페이드아웃
            collisionAlpha.snapTo(1.0f)
            collisionAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 300,  // 300ms 동안 천천히 사라짐
                    easing = androidx.compose.animation.core.FastOutLinearInEasing
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

    fun handlePlayer1Move() {
        if (gameState.gameStatus == GameStatus.PLAYING) {
            gameState = engine.processMove(
                currentState = gameState,
                playerId = "player1",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun handlePlayer2Move() {
        if (gameState.gameStatus == GameStatus.PLAYING) {
            gameState = engine.processMove(
                currentState = gameState,
                playerId = "player2",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    // 다음 라운드 (스코어 유지)
    fun handleNextRound() {
        gameState = engine.resetRound(
            currentScore1 = gameState.player1Score,
            currentScore2 = gameState.player2Score
        )
        scope.launch {
            player1AnimPos.snapTo(gameState.player1Position)
            player2AnimPos.snapTo(gameState.player2Position)
        }
    }

    // 전체 리셋 (스코어 초기화)
    fun handleReset() {
        gameState = engine.resetGame()
        scope.launch {
            player1AnimPos.snapTo(gameState.player1Position)
            player2AnimPos.snapTo(gameState.player2Position)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 게임 진행 중이든 승리 화면이든 항상 같은 레이아웃 유지
        // 좌우 분할 클릭 영역
        Row(modifier = Modifier.fillMaxSize()) {
            // 왼쪽 영역 - Player 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        Log.d("PlayLog", "handlePlayer1 Move")
                        handlePlayer1Move()
                    }
            )

            // 오른쪽 영역 - Player 2
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        Log.d("PlayLog", "handlePlayer2 Move")
                        handlePlayer2Move()
                    }
            )
        }

        // 게임 화면 (항상 같은 위치)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // 승리 스코어 표시 (항상 표시)
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

            // 게임 캔버스 (항상 같은 위치)
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

        // 승리 화면일 때만 중앙에 오버레이
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
                        onClick = { if (buttonsEnabled) handleNextRound() },
                        modifier = Modifier
                            .height(35.dp)
                            .wrapContentWidth(),
                        enabled = buttonsEnabled,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xFF4CAF50)
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

                    // 전체 리셋 버튼 (작게)
                    Button(
                        onClick = { if (buttonsEnabled) handleReset() },
                        modifier = Modifier
                            .height(28.dp)
                            .wrapContentWidth(),
                        enabled = buttonsEnabled,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text(
                            text = "RESET GAME",
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