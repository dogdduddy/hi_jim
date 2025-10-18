package com.jim.hi_jim.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.jim.hi_jim.presentation.constants.UserConstants
import com.jim.hi_jim.shared.model.GameRequestStatus
import com.jim.hi_jim.presentation.ui.common.MainMenuScreen
import com.jim.hi_jim.presentation.ui.sumo.LocalSumoGameScreen
import com.jim.hi_jim.presentation.ui.mukjjippa.LocalMukjjippaGameScreen
import com.jim.hi_jim.presentation.ui.sumo.GameLobbyScreen
import com.jim.hi_jim.presentation.ui.mukjjippa.MukjjippaGameLobbyScreen
import com.jim.hi_jim.presentation.ui.sumo.MultiplayerSumoGameScreen
import com.jim.hi_jim.presentation.ui.mukjjippa.MultiplayerMukjjippaGameScreen
import com.jim.hi_jim.presentation.viewmodel.sumo.GameLobbyViewModel
import com.jim.hi_jim.presentation.viewmodel.mukjjippa.MukjjippaGameLobbyViewModel
import com.jim.hi_jim.presentation.viewmodel.sumo.MultiplayerGameViewModel
import com.jim.hi_jim.presentation.viewmodel.mukjjippa.MultiplayerMukjjippaViewModel
import com.jim.hi_jim.fcm.FCMTokenManager

sealed class Screen {
    object MainMenu : Screen()
    object GameLobby : Screen()
    object MukjjippaGameLobby : Screen()
    data class MultiplayerGame(val gameId: String) : Screen()
    data class MultiplayerMukjjippaGame(val gameId: String) : Screen()
    object LocalGame : Screen()
    object LocalMukjjippaGame : Screen()
}

class MainActivity : ComponentActivity() {
    private var shouldOpenGameLobby = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        // FCM 토큰 등록
        FCMTokenManager.refreshToken()

        // 알림에서 열었는지 확인
        val openFromNotification = intent?.getBooleanExtra("openGameLobby", false) ?: false
        android.util.Log.d("MainActivity", "onCreate - openGameLobby: $openFromNotification, intent: ${intent?.extras}")
        shouldOpenGameLobby.value = openFromNotification

        setContent {
            SumoGameApp(shouldOpenGameLobby = shouldOpenGameLobby)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // 알림 클릭으로 앱이 열렸을 때
        val openFromNotification = intent.getBooleanExtra("openGameLobby", false)
        android.util.Log.d("MainActivity", "onNewIntent - openGameLobby: $openFromNotification, intent: ${intent.extras}")
        if (openFromNotification) {
            shouldOpenGameLobby.value = true
        }
    }
}

@Composable
fun SumoGameApp(shouldOpenGameLobby: MutableState<Boolean> = mutableStateOf(false)) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }
    val lobbyViewModel: GameLobbyViewModel = viewModel()
    val mukjjippaLobbyViewModel: MukjjippaGameLobbyViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    // 알림 클릭 감지
    LaunchedEffect(shouldOpenGameLobby.value) {
        if (shouldOpenGameLobby.value) {
            currentScreen = Screen.GameLobby
            shouldOpenGameLobby.value = false // 리셋
        }
    }

    // 스모 게임 ID 감지하여 자동으로 게임 화면으로 이동
    val sumoGameId by lobbyViewModel.gameId.collectAsState()
    val sumoSentRequestStatus by lobbyViewModel.sentRequestStatus.collectAsState()

    LaunchedEffect(sumoGameId) {
        sumoGameId?.let {
            currentScreen = Screen.MultiplayerGame(it)
        }
    }

    // 묵찌빠 게임 ID 감지하여 자동으로 게임 화면으로 이동
    val mukjjippaGameId by mukjjippaLobbyViewModel.gameId.collectAsState()
    val mukjjippaSentRequestStatus by mukjjippaLobbyViewModel.sentRequestStatus.collectAsState()

    LaunchedEffect(mukjjippaGameId) {
        mukjjippaGameId?.let {
            currentScreen = Screen.MultiplayerMukjjippaGame(it)
        }
    }

    when (val screen = currentScreen) {
        is Screen.MainMenu -> {
            MainMenuScreen(
                onSumoGameClick = {
                    currentScreen = Screen.GameLobby
                },
                onMukjjippaGameClick = {
                    currentScreen = Screen.MukjjippaGameLobby
                }
            )
        }

        is Screen.GameLobby -> {
            val receivedRequests by lobbyViewModel.receivedRequests.collectAsState()
            val requestStatus by lobbyViewModel.sentRequestStatus.collectAsState()

            GameLobbyScreen(
                receivedRequests = receivedRequests,
                sentRequestStatus = requestStatus,
                onSendRequest = {
                    lobbyViewModel.sendGameRequest()
                },
                onCancelRequest = {
                    lobbyViewModel.cancelGameRequest()
                },
                onAcceptRequest = { requestId ->
                    lobbyViewModel.acceptGameRequest(requestId)
                },
                onRejectRequest = { requestId ->
                    lobbyViewModel.rejectGameRequest(requestId)
                },
                onBackToMenu = {
                    currentScreen = Screen.MainMenu
                }
            )
        }

        is Screen.MultiplayerGame -> {
            val gameViewModel: MultiplayerGameViewModel = viewModel(
                key = screen.gameId,
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MultiplayerGameViewModel(screen.gameId) as T
                    }
                }
            )

            val gameData by gameViewModel.gameData.collectAsState()

            // 게임이 삭제되면 (상대방이 나가면) 자동으로 로비로 복귀
            LaunchedEffect(gameData) {
                if (gameData == null && currentScreen is Screen.MultiplayerGame) {
                    // 잠시 대기 후 로비로 복귀 (초기 로딩과 구분하기 위해)
                    kotlinx.coroutines.delay(1000)
                    if (gameData == null) {
                        currentScreen = Screen.GameLobby
                    }
                }
            }

            MultiplayerSumoGameScreen(
                currentUserId = UserConstants.CURRENT_USER_ID,
                gameData = gameData,
                onPlayerMove = {
                    gameViewModel.playerMove()
                },
                onNextRound = {
                    gameViewModel.nextRound()
                },
                onQuitGame = {
                    coroutineScope.launch {
                        gameViewModel.quitGame()
                        currentScreen = Screen.GameLobby
                    }
                }
            )
        }

        is Screen.LocalGame -> {
            LocalSumoGameScreen()
        }

        is Screen.LocalMukjjippaGame -> {
            LocalMukjjippaGameScreen()
        }

        is Screen.MukjjippaGameLobby -> {
            val receivedRequests by mukjjippaLobbyViewModel.receivedRequests.collectAsState()
            val requestStatus by mukjjippaLobbyViewModel.sentRequestStatus.collectAsState()

            MukjjippaGameLobbyScreen(
                receivedRequests = receivedRequests,
                sentRequestStatus = requestStatus,
                onSendRequest = {
                    mukjjippaLobbyViewModel.sendGameRequest()
                },
                onCancelRequest = {
                    mukjjippaLobbyViewModel.cancelGameRequest()
                },
                onAcceptRequest = { requestId ->
                    mukjjippaLobbyViewModel.acceptGameRequest(requestId)
                },
                onRejectRequest = { requestId ->
                    mukjjippaLobbyViewModel.rejectGameRequest(requestId)
                },
                onBackToMenu = {
                    currentScreen = Screen.MainMenu
                }
            )
        }

        is Screen.MultiplayerMukjjippaGame -> {
            val mukjjippaGameViewModel: MultiplayerMukjjippaViewModel = viewModel(
                key = screen.gameId,
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MultiplayerMukjjippaViewModel(
                            gameId = screen.gameId,
                            currentUserId = UserConstants.CURRENT_USER_ID
                        ) as T
                    }
                }
            )

            val mukjjippaGameData by mukjjippaGameViewModel.gameData.collectAsState()

            // 게임이 삭제되면 (상대방이 나가면) 자동으로 로비로 복귀
            LaunchedEffect(mukjjippaGameData) {
                if (mukjjippaGameData == null && currentScreen is Screen.MultiplayerMukjjippaGame) {
                    // 잠시 대기 후 로비로 복귀 (초기 로딩과 구분하기 위해)
                    kotlinx.coroutines.delay(1000)
                    if (mukjjippaGameData == null) {
                        currentScreen = Screen.MukjjippaGameLobby
                    }
                }
            }

            MultiplayerMukjjippaGameScreen(
                currentUserId = UserConstants.CURRENT_USER_ID,
                gameData = mukjjippaGameData,
                onPlayerChoice = { choice ->
                    mukjjippaGameViewModel.makeChoice(choice)
                },
                onRestartGame = {
                    mukjjippaGameViewModel.restartGame()
                },
                onQuitGame = {
                    coroutineScope.launch {
                        mukjjippaGameViewModel.quitGame()
                        currentScreen = Screen.MukjjippaGameLobby
                    }
                }
            )
        }
    }
}