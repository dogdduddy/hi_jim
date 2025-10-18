package com.jim.hi_jim.presentation.viewmodel.mukjjippa

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jim.hi_jim.data.repository.FirebaseGameRepository
import com.jim.hi_jim.presentation.constants.UserConstants
import com.jim.hi_jim.shared.model.GameRequest
import com.jim.hi_jim.shared.model.GameRequestStatus
import com.jim.hi_jim.shared.model.GameType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MukjjippaGameLobbyViewModel : ViewModel() {

    private val repository = FirebaseGameRepository()
    private val currentUserId = UserConstants.CURRENT_USER_ID
    private val otherUserId = if (currentUserId == UserConstants.USER_1)
        UserConstants.USER_2 else UserConstants.USER_1

    // 받은 묵찌빠 게임 요청 목록
    val receivedRequests: StateFlow<List<GameRequest>> = repository
        .observeGameRequestsByType(currentUserId, GameType.MUKJJIPPA)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 보낸 요청 ID
    private val _sentRequestId = MutableStateFlow<String?>(null)

    // 보낸 요청의 전체 정보 (상태 + gameId 포함)
    private val sentRequest: StateFlow<GameRequest?> = _sentRequestId
        .flatMapLatest { requestId ->
            if (requestId != null) {
                repository.observeSentRequest(currentUserId, requestId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    // 보낸 요청 상태 (UI 표시용)
    val sentRequestStatus: StateFlow<GameRequestStatus?> = sentRequest
        .map { it?.status }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 생성된 게임 ID
    private val _gameId = MutableStateFlow<String?>(null)
    val gameId: StateFlow<String?> = _gameId.asStateFlow()

    companion object {
        private const val TAG = "MukjjippaGameLobbyVM"
    }

    // 묵찌빠 게임 요청 보내기
    fun sendGameRequest() {
        viewModelScope.launch {
            val result = repository.sendGameRequest(currentUserId, otherUserId, GameType.MUKJJIPPA)
            result.onSuccess { requestId ->
                _sentRequestId.value = requestId
                Log.d(TAG, "Mukjjippa request sent: $requestId")
            }.onFailure { error ->
                Log.e(TAG, "Failed to send mukjjippa request", error)
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
                    Log.d(TAG, "Mukjjippa game created: $gameId")
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed to accept mukjjippa request", error)
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
                Log.d(TAG, "Mukjjippa request cancelled: $requestId")
            }
        }
    }

    // 보낸 요청이 수락되었을 때 게임 ID 확인
    init {
        viewModelScope.launch {
            sentRequest.collect { request ->
                Log.d(TAG, "Sent mukjjippa request update: status=${request?.status}, gameId=${request?.gameId}")

                when {
                    // 요청이 수락되고 gameId가 있으면 게임 시작
                    request?.status == GameRequestStatus.ACCEPTED && request.gameId != null -> {
                        _gameId.value = request.gameId
                        _sentRequestId.value = null
                        Log.d(TAG, "✅ Mukjjippa game started from request sender: ${request.gameId}")
                    }
                    // 요청이 삭제된 경우 (거절/취소)
                    request == null && _sentRequestId.value != null -> {
                        _sentRequestId.value = null
                        Log.d(TAG, "🔴 Mukjjippa request rejected or cancelled")
                    }
                }
            }
        }
    }
}
