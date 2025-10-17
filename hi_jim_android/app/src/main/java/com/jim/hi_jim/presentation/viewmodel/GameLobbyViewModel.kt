package com.jim.hi_jim.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jim.hi_jim.data.repository.FirebaseGameRepository
import com.jim.hi_jim.presentation.constants.UserConstants
import com.jim.hi_jim.shared.model.GameRequest
import com.jim.hi_jim.shared.model.GameRequestStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameLobbyViewModel : ViewModel() {

    private val repository = FirebaseGameRepository()
    private val currentUserId = UserConstants.CURRENT_USER_ID
    private val otherUserId = if (currentUserId == UserConstants.USER_1)
        UserConstants.USER_2 else UserConstants.USER_1

    // 받은 요청 목록
    val receivedRequests: StateFlow<List<GameRequest>> = repository
        .observeGameRequests(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 보낸 요청 ID
    private val _sentRequestId = MutableStateFlow<String?>(null)

    // 보낸 요청 상태
    val sentRequestStatus: StateFlow<GameRequestStatus?> = _sentRequestId
        .flatMapLatest { requestId ->
            if (requestId != null) {
                repository.observeRequestStatus(otherUserId, requestId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 생성된 게임 ID
    private val _gameId = MutableStateFlow<String?>(null)
    val gameId: StateFlow<String?> = _gameId.asStateFlow()

    companion object {
        private const val TAG = "GameLobbyViewModel"
    }

    // 게임 요청 보내기
    fun sendGameRequest() {
        viewModelScope.launch {
            val result = repository.sendGameRequest(currentUserId, otherUserId)
            result.onSuccess { requestId ->
                _sentRequestId.value = requestId
                Log.d(TAG, "Request sent: $requestId")
            }.onFailure { error ->
                Log.e(TAG, "Failed to send request", error)
            }
        }
    }

    // 게임 요청 수락
    fun acceptGameRequest(requestId: String) {
        viewModelScope.launch {
            val result = repository.respondToGameRequest(currentUserId, requestId, accept = true)
            result.onSuccess { gameId ->
                if (gameId != null) {
                    _gameId.value = gameId
                    Log.d(TAG, "Game created: $gameId")
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed to accept request", error)
            }
        }
    }

    // 게임 요청 거절
    fun rejectGameRequest(requestId: String) {
        viewModelScope.launch {
            repository.respondToGameRequest(currentUserId, requestId, accept = false)
        }
    }

    // 요청 취소
    fun cancelGameRequest() {
        viewModelScope.launch {
            val requestId = _sentRequestId.value
            if (requestId != null) {
                repository.respondToGameRequest(currentUserId, requestId, accept = false)
                _sentRequestId.value = null
                Log.d(TAG, "Request cancelled: $requestId")
            }
        }
    }

    // 보낸 요청이 수락되었을 때 게임 ID 확인
    init {
        viewModelScope.launch {
            _sentRequestId.flatMapLatest { requestId ->
                if (requestId != null) {
                    // 보낸 요청의 전체 정보를 감시 (fromUserId 경로)
                    repository.observeSentRequest(currentUserId, requestId)
                } else {
                    flowOf(null)
                }
            }.collect { request ->
                Log.d(TAG, "Sent request update: ${request?.status}, gameId=${request?.gameId}")
                if (request?.status == GameRequestStatus.ACCEPTED && request.gameId != null) {
                    _gameId.value = request.gameId
                    Log.d(TAG, "Game started from request sender: ${request.gameId}")
                } else if (request == null) {
                    // 요청이 삭제된 경우 (거절/취소)
                    _sentRequestId.value = null
                    Log.d(TAG, "Request rejected or cancelled")
                }
            }
        }
    }
}
