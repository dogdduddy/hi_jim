package com.jim.hi_jim.presentation.viewmodel.sumo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jim.hi_jim.data.repository.FirebaseGameRepository
import com.jim.hi_jim.presentation.constants.UserConstants
import com.jim.hi_jim.shared.engine.SumoPhysicsEngine
import com.jim.hi_jim.shared.model.MultiplayerGameData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MultiplayerGameViewModel(
    private val gameId: String
) : ViewModel() {

    private val repository = FirebaseGameRepository()
    private val engine = SumoPhysicsEngine()
    private val currentUserId = UserConstants.CURRENT_USER_ID

    // 게임 상태
    val gameData: StateFlow<MultiplayerGameData?> = repository
        .observeGameState(gameId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    companion object {
        private const val TAG = "MultiplayerGameVM"
    }

    // 플레이어 이동
    fun playerMove() {
        viewModelScope.launch {
            val current = gameData.value ?: return@launch

            // 현재 상태를 SumoGameState로 변환
            val currentState = current.toSumoGameState()

            // 물리 엔진으로 새 상태 계산
            val playerId = if (currentUserId == current.player1Id) "player1" else "player2"
            val newState = engine.processMove(
                currentState = currentState,
                playerId = playerId,
                timestamp = System.currentTimeMillis()
            )

            // MultiplayerGameData로 변환하여 Firebase에 저장
            val newGameData = MultiplayerGameData.fromSumoGameState(
                gameId = gameId,
                player1Id = current.player1Id,
                player2Id = current.player2Id,
                state = newState,
                lastMovePlayerId = currentUserId
            )

            repository.updateGameState(newGameData)
        }
    }

    // 다음 라운드
    fun nextRound() {
        viewModelScope.launch {
            val current = gameData.value ?: return@launch

            val resetState = engine.resetRound(
                currentScore1 = current.player1Score,
                currentScore2 = current.player2Score
            )

            val newGameData = MultiplayerGameData.fromSumoGameState(
                gameId = gameId,
                player1Id = current.player1Id,
                player2Id = current.player2Id,
                state = resetState,
                lastMovePlayerId = ""
            )

            repository.updateGameState(newGameData)
        }
    }

    // 게임 종료
    suspend fun quitGame() {
        repository.endGame(gameId)
    }
}
