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

@Composable
fun MultiplayerMukjjippaGameScreen(
    currentUserId: String,
    gameData: MultiplayerMukjjippaData?,
    onPlayerChoice: (MukjjippaChoice) -> Unit,
    onRestartGame: () -> Unit,
    onQuitGame: () -> Unit
) {
    val gameState = gameData?.toMukjjippaGameState()

    if (gameState == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "게임 로딩 중...",
                fontSize = 16.sp,
                color = Color.White
            )
        }
        return
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
            // 상단 영역 (뒤로가기 버튼 + 스코어 + 공격자 정보)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 뒤로가기 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Button(
                        onClick = onQuitGame,
                        modifier = Modifier.size(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF4D4D4D)
                        )
                    ) {
                        Text(
                            text = "←",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

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
                                    onClick = onRestartGame,
                                    modifier = Modifier.height(35.dp),
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4A90E2))
                                ) {
                                    Text("재시작", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = onQuitGame,
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
                                val opponentChoice = if (currentUserId == MukjjippaConstants.USER_JIM_ID) {
                                    gameState.hiChoice
                                } else {
                                    gameState.jimChoice
                                }
                                val myChoice = if (currentUserId == MukjjippaConstants.USER_JIM_ID) {
                                    gameState.jimChoice
                                } else {
                                    gameState.hiChoice
                                }

                                Text(
                                    text = opponentChoice?.displayName ?: "",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Yellow
                                )
                                Text(
                                    text = myChoice?.displayName ?: "",
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

                    !gameState.bothPlayersReady -> {
                        Text(
                            text = "상대방을 기다리는 중...",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 하단 버튼 영역
            if (!gameState.isGameFinished) {
                val currentPlayerChoice = gameState.getChoiceForPlayer(currentUserId)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 버튼 활성화 조건
                    val canInteract = when {
                        // 결과 표시 중에는 비활성화
                        gameState.countdownState == CountdownState.SHOWING_RESULT -> false

                        // 이미 선택했고 결과 대기 중이면 비활성화
                        currentPlayerChoice != null && gameState.countdownState == CountdownState.RESULT_WAIT -> false

                        // 가위바위보: COUNTDOWN_3 이후부터 활성화 ("보!" 멘트 이후)
                        gameState.phase == MukjjippaPhase.ROCK_PAPER_SCISSORS -> {
                            gameState.countdownState == CountdownState.COUNTDOWN_3 ||
                            gameState.countdownState == CountdownState.RESULT_WAIT
                        }

                        // 묵찌빠: COUNTDOWN_2 이후부터 활성화 (두 번째 멘트 이후)
                        gameState.phase == MukjjippaPhase.MUKJJIPPA -> {
                            gameState.countdownState == CountdownState.COUNTDOWN_2 ||
                            gameState.countdownState == CountdownState.RESULT_WAIT
                        }

                        else -> false
                    }

                    val isButtonEnabled = canInteract && gameState.bothPlayersReady

                    MukjjippaChoiceButton(
                        choice = MukjjippaChoice.SCISSORS,
                        isSelected = currentPlayerChoice == MukjjippaChoice.SCISSORS,
                        isEnabled = isButtonEnabled,
                        onClick = {
                            onPlayerChoice(MukjjippaChoice.SCISSORS)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    MukjjippaChoiceButton(
                        choice = MukjjippaChoice.ROCK,
                        isSelected = currentPlayerChoice == MukjjippaChoice.ROCK,
                        isEnabled = isButtonEnabled,
                        onClick = {
                            onPlayerChoice(MukjjippaChoice.ROCK)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    MukjjippaChoiceButton(
                        choice = MukjjippaChoice.PAPER,
                        isSelected = currentPlayerChoice == MukjjippaChoice.PAPER,
                        isEnabled = isButtonEnabled,
                        onClick = {
                            onPlayerChoice(MukjjippaChoice.PAPER)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MukjjippaChoiceButton(
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