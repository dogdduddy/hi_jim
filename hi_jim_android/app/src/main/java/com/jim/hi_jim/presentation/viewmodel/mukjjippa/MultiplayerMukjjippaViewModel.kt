package com.jim.hi_jim.presentation.viewmodel.mukjjippa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jim.hi_jim.data.repository.FirebaseGameRepository
import com.jim.hi_jim.shared.constants.MukjjippaConstants
import com.jim.hi_jim.shared.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MultiplayerMukjjippaViewModel(
    private val gameId: String,
    private val currentUserId: String = MukjjippaConstants.USER_JIM_ID
) : ViewModel() {

    private val repository = FirebaseGameRepository()

    private val _gameData = MutableStateFlow<MultiplayerMukjjippaData?>(null)
    val gameData: StateFlow<MultiplayerMukjjippaData?> = _gameData.asStateFlow()

    private var countdownJob: kotlinx.coroutines.Job? = null
    private var resultProcessingJob: kotlinx.coroutines.Job? = null

    init {
        observeGameState()
    }

    private fun observeGameState() {
        viewModelScope.launch {
            repository.observeMukjjippaGameState(gameId).collect { data ->
                _gameData.value = data

                // 게임 로직 처리
                data?.let { processGameLogic(it) }
            }
        }
    }

    private fun processGameLogic(data: MultiplayerMukjjippaData) {
        val gameState = data.toMukjjippaGameState()

        android.util.Log.d("MukjjippaVM", "processGameLogic called: phase=${gameState.phase}, isGameFinished=${gameState.isGameFinished}, countdownState=${gameState.countdownState}, jimChoice=${gameState.jimChoice}, hiChoice=${gameState.hiChoice}, attackerId=${gameState.attackerId}")

        // 게임이 종료되었으면 모든 진행 중인 작업 취소
        if (gameState.isGameFinished || gameState.phase == MukjjippaPhase.GAME_OVER) {
            android.util.Log.d("MukjjippaVM", "Game finished or GAME_OVER phase, cancelling jobs and returning")
            countdownJob?.cancel()
            resultProcessingJob?.cancel()
            return
        }

        // 양쪽 플레이어가 모두 참여했고, 대기 상태이며, 게임이 종료되지 않았을 때만 카운트다운 시작 (player1만)
        if (gameState.bothPlayersReady &&
            gameState.countdownState == CountdownState.WAITING &&
            !gameState.isGameFinished &&
            currentUserId == MukjjippaConstants.USER_JIM_ID) {  // Jim(player1)만 카운트다운 시작
            // 이미 카운트다운이 진행 중이면 무시
            if (countdownJob?.isActive == true) return

            startCountdown(gameState)
        }

        // 양쪽 플레이어가 모두 선택했고, 결과 대기 상태라면 먼저 상대방 선택 표시 (player1만)
        if (gameState.isChoiceComplete() &&
            gameState.countdownState == CountdownState.RESULT_WAIT &&
            currentUserId == MukjjippaConstants.USER_JIM_ID) {  // Jim(player1)만 처리
            // 이미 처리 중이면 무시
            if (resultProcessingJob?.isActive == true) return

            resultProcessingJob = viewModelScope.launch {
                delay(MukjjippaConstants.RESULT_DISPLAY_DELAY_MS)
                // 상대방 선택 표시
                updateGameState(gameState.copy(countdownState = CountdownState.SHOWING_RESULT))
            }
        }

        // 상대방 선택 표시 상태에서 2초 후 결과 처리 (player1만 처리)
        if (gameState.isChoiceComplete() &&
            gameState.countdownState == CountdownState.SHOWING_RESULT &&
            currentUserId == MukjjippaConstants.USER_JIM_ID) {  // Jim(player1)만 결과 처리
            android.util.Log.d("MukjjippaVM", "SHOWING_RESULT condition met, resultProcessingJob active=${resultProcessingJob?.isActive}")
            // 이미 처리 중이면 무시
            if (resultProcessingJob?.isActive == true) return

            resultProcessingJob = viewModelScope.launch {
                android.util.Log.d("MukjjippaVM", "Starting 2 second delay before processing result")
                delay(2000) // 2초 동안 상대방 선택 표시
                // 현재 상태를 다시 가져와서 처리
                val currentGameState = _gameData.value?.toMukjjippaGameState()
                android.util.Log.d("MukjjippaVM", "After delay: currentGameState phase=${currentGameState?.phase}, countdownState=${currentGameState?.countdownState}, isFinished=${currentGameState?.isGameFinished}")
                if (currentGameState != null &&
                    currentGameState.countdownState == CountdownState.SHOWING_RESULT &&
                    !currentGameState.isGameFinished) {
                    android.util.Log.d("MukjjippaVM", "Calling processGameResult")
                    processGameResult(currentGameState)
                } else {
                    android.util.Log.d("MukjjippaVM", "NOT calling processGameResult - conditions not met")
                }
            }
        }
    }

    private fun startCountdown(gameState: MukjjippaGameState) {
        countdownJob = viewModelScope.launch {
            val messages = if (gameState.phase == MukjjippaPhase.ROCK_PAPER_SCISSORS) {
                listOf(
                    MukjjippaConstants.Messages.ROCK_PAPER_SCISSORS_1,
                    MukjjippaConstants.Messages.ROCK_PAPER_SCISSORS_2,
                    MukjjippaConstants.Messages.ROCK_PAPER_SCISSORS_3
                )
            } else {
                val prevChoice = gameState.previousAttackerChoice ?: MukjjippaChoice.ROCK
                listOf(
                    prevChoice.getCountdownMessage(),
                    prevChoice.getCountdownMessage(),
                    ""
                )
            }

            // 첫 번째 메시지
            updateGameState(gameState.copy(
                countdownState = CountdownState.COUNTDOWN_1,
                currentMessage = messages[0]
            ))
            delay(1000) // 1초 표시
            // 메시지 사라짐
            updateGameState(gameState.copy(
                countdownState = CountdownState.COUNTDOWN_1,
                currentMessage = ""
            ))
            delay(500) // 0.5초 대기

            // 두 번째 메시지
            updateGameState(gameState.copy(
                countdownState = CountdownState.COUNTDOWN_2,
                currentMessage = messages[1]
            ))
            delay(1000) // 1초 표시
            // 메시지 사라짐
            updateGameState(gameState.copy(
                countdownState = CountdownState.COUNTDOWN_2,
                currentMessage = ""
            ))
            delay(500) // 0.5초 대기

            // 세 번째 메시지 (가위바위보의 경우)
            if (gameState.phase == MukjjippaPhase.ROCK_PAPER_SCISSORS) {
                updateGameState(gameState.copy(
                    countdownState = CountdownState.COUNTDOWN_3,
                    currentMessage = messages[2]
                ))
                delay(1000) // 1초 표시
                // 메시지 사라짐
                updateGameState(gameState.copy(
                    countdownState = CountdownState.COUNTDOWN_3,
                    currentMessage = ""
                ))
                delay(500) // 0.5초 대기
            }

            // 결과 대기 상태로 전환
            updateGameState(gameState.copy(
                countdownState = CountdownState.RESULT_WAIT,
                currentMessage = ""
            ))
        }
    }

    private fun processGameResult(gameState: MukjjippaGameState) {
        if (!gameState.isChoiceComplete()) return

        val jimChoice = gameState.jimChoice!!
        val hiChoice = gameState.hiChoice!!

        android.util.Log.d("MukjjippaVM", "processGameResult: phase=${gameState.phase}, jimChoice=$jimChoice, hiChoice=$hiChoice, attackerId=${gameState.attackerId}")

        val newGameState = when (gameState.phase) {
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
                        android.util.Log.d("MukjjippaVM", "VICTORY CONDITION: Same choices in MUKJJIPPA phase! Winner: ${gameState.attackerId}")
                        // 공격자가 승리
                        gameState.copy(
                            phase = MukjjippaPhase.GAME_OVER,
                            winner = gameState.attackerId,
                            isGameFinished = true,
                            countdownState = CountdownState.WAITING,
                            currentMessage = "",
                            jimChoice = null,
                            hiChoice = null
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

        android.util.Log.d("MukjjippaVM", "processGameResult complete: newGameState phase=${newGameState.phase}, isGameFinished=${newGameState.isGameFinished}, winner=${newGameState.winner}")
        updateGameState(newGameState)
    }

    fun makeChoice(choice: MukjjippaChoice) {
        val currentData = _gameData.value ?: return
        val gameState = currentData.toMukjjippaGameState()

        // 게임이 종료되었으면 선택할 수 없음
        if (gameState.isGameFinished) {
            return
        }

        // 결과 표시 중에는 선택할 수 없음
        if (gameState.countdownState == CountdownState.SHOWING_RESULT) {
            return
        }

        // 현재 플레이어가 이미 선택했는지 확인
        val currentPlayerChoice = if (currentUserId == MukjjippaConstants.USER_JIM_ID) {
            gameState.jimChoice
        } else {
            gameState.hiChoice
        }

        // 이미 선택했고 결과 대기 중이면 변경 불가
        if (currentPlayerChoice != null && gameState.countdownState == CountdownState.RESULT_WAIT) {
            return
        }

        val updatedGameState = if (currentUserId == MukjjippaConstants.USER_JIM_ID) {
            gameState.copy(jimChoice = choice)
        } else {
            gameState.copy(hiChoice = choice)
        }

        updateGameState(updatedGameState)
    }

    fun restartGame() {
        val currentData = _gameData.value ?: return
        val winner = currentData.winner

        val newJimScore = if (winner == MukjjippaConstants.USER_JIM_ID) currentData.jimScore + 1 else currentData.jimScore
        val newHiScore = if (winner == MukjjippaConstants.USER_GIRLFRIEND_ID) currentData.hiScore + 1 else currentData.hiScore

        val newGameState = MukjjippaGameState(
            bothPlayersReady = true,
            jimScore = newJimScore,
            hiScore = newHiScore
        )

        updateGameState(newGameState)
    }

    suspend fun quitGame() {
        repository.endMukjjippaGame(gameId)
    }

    private fun updateGameState(gameState: MukjjippaGameState) {
        val currentData = _gameData.value ?: return

        android.util.Log.d("MukjjippaVM", "updateGameState called: phase=${gameState.phase}, isGameFinished=${gameState.isGameFinished}, countdownState=${gameState.countdownState}, winner=${gameState.winner}, jimChoice=${gameState.jimChoice}, hiChoice=${gameState.hiChoice}")

        val updatedData = MultiplayerMukjjippaData.fromMukjjippaGameState(
            gameId = gameId,
            player1Id = currentData.player1Id,
            player2Id = currentData.player2Id,
            state = gameState,
            lastMovePlayerId = currentUserId
        )

        viewModelScope.launch {
            repository.updateMukjjippaGameState(updatedData)
            android.util.Log.d("MukjjippaVM", "updateMukjjippaGameState completed")
        }
    }
}