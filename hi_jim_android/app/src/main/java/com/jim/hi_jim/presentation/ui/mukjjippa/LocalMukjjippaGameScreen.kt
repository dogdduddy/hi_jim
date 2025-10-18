package com.jim.hi_jim.presentation.ui.mukjjippa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.jim.hi_jim.shared.constants.MukjjippaConstants
import com.jim.hi_jim.shared.model.*
import kotlinx.coroutines.delay

@Composable
fun LocalMukjjippaGameScreen() {
    var gameState by remember { mutableStateOf(MukjjippaGameState(bothPlayersReady = true)) }

    LaunchedEffect(gameState.bothPlayersReady) {
        if (gameState.bothPlayersReady && gameState.countdownState == CountdownState.WAITING) {
            startCountdown(gameState) { newState ->
                gameState = newState
            }
        }
    }

    LaunchedEffect(gameState.isChoiceComplete()) {
        if (gameState.isChoiceComplete() && gameState.countdownState == CountdownState.RESULT_WAIT) {
            delay(MukjjippaConstants.RESULT_DISPLAY_DELAY_MS)
            gameState = gameState.copy(countdownState = CountdownState.SHOWING_RESULT)

            delay(2000) // 결과 표시 시간

            val result = processGameResult(gameState)
            gameState = result
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            // 상단 영역 (스코어 + 공격자 정보)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 공격자 정보
                if (gameState.phase == MukjjippaPhase.MUKJJIPPA && gameState.attackerId != null) {
                    Text(
                        text = "${gameState.getAttackerDisplayName()}의 공격!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // 스코어
                Text(
                    text = "${MukjjippaConstants.USER_JIM_DISPLAY_NAME} : ${gameState.jimScore}   ${MukjjippaConstants.USER_GIRLFRIEND_DISPLAY_NAME} : ${gameState.hiScore}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // 중앙 영역 (메시지 또는 결과)
            Box(
                modifier = Modifier.height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    gameState.isGameFinished -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${gameState.getWinnerDisplayName()}의 승리!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Green,
                                textAlign = TextAlign.Center
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val newJimScore = if (gameState.winner == MukjjippaConstants.USER_JIM_ID) gameState.jimScore + 1 else gameState.jimScore
                                        val newHiScore = if (gameState.winner == MukjjippaConstants.USER_GIRLFRIEND_ID) gameState.hiScore + 1 else gameState.hiScore

                                        gameState = MukjjippaGameState(
                                            bothPlayersReady = true,
                                            jimScore = newJimScore,
                                            hiScore = newHiScore
                                        )
                                    },
                                    modifier = Modifier.height(35.dp),
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4A90E2))
                                ) {
                                    Text("재시작", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { /* 나가기 로직 */ },
                                    modifier = Modifier.height(35.dp),
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF666666))
                                ) {
                                    Text("나가기", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    gameState.countdownState == CountdownState.SHOWING_RESULT -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "상대방 선택:",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = gameState.hiChoice?.displayName ?: "",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Yellow
                                )
                                Text(
                                    text = gameState.jimChoice?.displayName ?: "",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Cyan
                                )
                            }
                        }
                    }

                    gameState.currentMessage.isNotEmpty() -> {
                        Text(
                            text = gameState.currentMessage,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 하단 버튼 영역
            if (!gameState.isGameFinished) {
                // 버튼 활성화 조건
                val canInteract = when {
                    // 결과 표시 중에는 비활성화
                    gameState.countdownState == CountdownState.SHOWING_RESULT -> false

                    // 결과 대기 중이면 비활성화
                    gameState.countdownState == CountdownState.RESULT_WAIT -> false

                    // 가위바위보: COUNTDOWN_3 이후부터 활성화 ("보!" 멘트 이후)
                    gameState.phase == MukjjippaPhase.ROCK_PAPER_SCISSORS -> {
                        gameState.countdownState == CountdownState.COUNTDOWN_3
                    }

                    // 묵찌빠: COUNTDOWN_2 이후부터 활성화 (두 번째 멘트 이후)
                    gameState.phase == MukjjippaPhase.MUKJJIPPA -> {
                        gameState.countdownState == CountdownState.COUNTDOWN_2
                    }

                    else -> false
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ChoiceButton(
                        choice = MukjjippaChoice.SCISSORS,
                        isSelected = gameState.jimChoice == MukjjippaChoice.SCISSORS,
                        isEnabled = canInteract,
                        onClick = {
                            if (gameState.countdownState != CountdownState.RESULT_WAIT) {
                                gameState = gameState.copy(jimChoice = MukjjippaChoice.SCISSORS)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ChoiceButton(
                        choice = MukjjippaChoice.ROCK,
                        isSelected = gameState.jimChoice == MukjjippaChoice.ROCK,
                        isEnabled = canInteract,
                        onClick = {
                            if (gameState.countdownState != CountdownState.RESULT_WAIT) {
                                gameState = gameState.copy(jimChoice = MukjjippaChoice.ROCK)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ChoiceButton(
                        choice = MukjjippaChoice.PAPER,
                        isSelected = gameState.jimChoice == MukjjippaChoice.PAPER,
                        isEnabled = canInteract,
                        onClick = {
                            if (gameState.countdownState != CountdownState.RESULT_WAIT) {
                                gameState = gameState.copy(jimChoice = MukjjippaChoice.PAPER)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoiceButton(
    choice: MukjjippaChoice,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) Color(0xFF4A90E2) else Color(0xFF2A2A2A),
            disabledBackgroundColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = choice.displayName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isEnabled) Color.White else Color.Gray
        )
    }
}

private suspend fun startCountdown(
    currentState: MukjjippaGameState,
    onStateChange: (MukjjippaGameState) -> Unit
) {
    val messages = if (currentState.phase == MukjjippaPhase.ROCK_PAPER_SCISSORS) {
        listOf(
            MukjjippaConstants.Messages.ROCK_PAPER_SCISSORS_1,
            MukjjippaConstants.Messages.ROCK_PAPER_SCISSORS_2,
            MukjjippaConstants.Messages.ROCK_PAPER_SCISSORS_3
        )
    } else {
        val prevChoice = currentState.previousAttackerChoice ?: MukjjippaChoice.ROCK
        listOf(
            prevChoice.getCountdownMessage(),
            prevChoice.getCountdownMessage(),
            "" // 마지막은 대기 후 결과 표시
        )
    }

    // 첫 번째 메시지
    onStateChange(currentState.copy(
        countdownState = CountdownState.COUNTDOWN_1,
        currentMessage = messages[0]
    ))
    delay(MukjjippaConstants.COUNTDOWN_DELAY_MS)

    // 두 번째 메시지
    onStateChange(currentState.copy(
        countdownState = CountdownState.COUNTDOWN_2,
        currentMessage = messages[1]
    ))
    delay(MukjjippaConstants.COUNTDOWN_DELAY_MS)

    // 세 번째 메시지 (가위바위보의 경우) 또는 대기 (묵찌빠의 경우)
    if (currentState.phase == MukjjippaPhase.ROCK_PAPER_SCISSORS) {
        onStateChange(currentState.copy(
            countdownState = CountdownState.COUNTDOWN_3,
            currentMessage = messages[2]
        ))
        delay(MukjjippaConstants.COUNTDOWN_DELAY_MS)
    }

    // 결과 대기 상태로 전환
    onStateChange(currentState.copy(
        countdownState = CountdownState.RESULT_WAIT,
        currentMessage = "",
        hiChoice = generateRandomChoice() // AI 선택
    ))
}

private fun generateRandomChoice(): MukjjippaChoice {
    return MukjjippaChoice.values().random()
}

private fun processGameResult(gameState: MukjjippaGameState): MukjjippaGameState {
    val jimChoice = gameState.jimChoice ?: return gameState
    val hiChoice = gameState.hiChoice ?: return gameState

    return when (gameState.phase) {
        MukjjippaPhase.ROCK_PAPER_SCISSORS -> {
            when {
                jimChoice == hiChoice -> {
                    // 무승부, 다시 가위바위보
                    gameState.resetChoices().copy(bothPlayersReady = true)
                }
                jimChoice.beats(hiChoice) -> {
                    // Jim이 공격자가 됨
                    gameState.copy(
                        phase = MukjjippaPhase.MUKJJIPPA,
                        attackerId = MukjjippaConstants.USER_JIM_ID,
                        previousAttackerChoice = jimChoice,
                        countdownState = CountdownState.WAITING,
                        jimChoice = null,
                        hiChoice = null,
                        currentMessage = "",
                        bothPlayersReady = true
                    )
                }
                else -> {
                    // Hi가 공격자가 됨
                    gameState.copy(
                        phase = MukjjippaPhase.MUKJJIPPA,
                        attackerId = MukjjippaConstants.USER_GIRLFRIEND_ID,
                        previousAttackerChoice = hiChoice,
                        countdownState = CountdownState.WAITING,
                        jimChoice = null,
                        hiChoice = null,
                        currentMessage = "",
                        bothPlayersReady = true
                    )
                }
            }
        }

        MukjjippaPhase.MUKJJIPPA -> {
            when {
                jimChoice == hiChoice -> {
                    // 공격자가 승리
                    gameState.copy(
                        phase = MukjjippaPhase.GAME_OVER,
                        winner = gameState.attackerId,
                        isGameFinished = true
                    )
                }
                gameState.attackerId == MukjjippaConstants.USER_JIM_ID -> {
                    if (jimChoice.beats(hiChoice)) {
                        // Jim이 계속 공격
                        gameState.copy(
                            previousAttackerChoice = jimChoice,
                            countdownState = CountdownState.WAITING,
                            jimChoice = null,
                            hiChoice = null,
                            currentMessage = "",
                            bothPlayersReady = true
                        )
                    } else {
                        // Hi가 공격자가 됨
                        gameState.copy(
                            attackerId = MukjjippaConstants.USER_GIRLFRIEND_ID,
                            previousAttackerChoice = hiChoice,
                            countdownState = CountdownState.WAITING,
                            jimChoice = null,
                            hiChoice = null,
                            currentMessage = "",
                            bothPlayersReady = true
                        )
                    }
                }
                else -> {
                    if (hiChoice.beats(jimChoice)) {
                        // Hi가 계속 공격
                        gameState.copy(
                            previousAttackerChoice = hiChoice,
                            countdownState = CountdownState.WAITING,
                            jimChoice = null,
                            hiChoice = null,
                            currentMessage = "",
                            bothPlayersReady = true
                        )
                    } else {
                        // Jim이 공격자가 됨
                        gameState.copy(
                            attackerId = MukjjippaConstants.USER_JIM_ID,
                            previousAttackerChoice = jimChoice,
                            countdownState = CountdownState.WAITING,
                            jimChoice = null,
                            hiChoice = null,
                            currentMessage = "",
                            bothPlayersReady = true
                        )
                    }
                }
            }
        }

        MukjjippaPhase.GAME_OVER -> gameState
    }
}