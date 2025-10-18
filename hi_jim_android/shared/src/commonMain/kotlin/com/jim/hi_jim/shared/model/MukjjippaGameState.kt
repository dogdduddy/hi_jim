package com.jim.hi_jim.shared.model

enum class MukjjippaPhase {
    ROCK_PAPER_SCISSORS,  // 가위바위보 단계
    MUKJJIPPA,           // 묵찌빠 단계
    GAME_OVER            // 게임 종료
}

enum class CountdownState {
    WAITING,     // 대기 중
    COUNTDOWN_1, // 첫 번째 멘트
    COUNTDOWN_2, // 두 번째 멘트
    COUNTDOWN_3, // 세 번째 멘트
    RESULT_WAIT, // 결과 대기
    SHOWING_RESULT // 결과 표시
}

data class MukjjippaGameState(
    val phase: MukjjippaPhase = MukjjippaPhase.ROCK_PAPER_SCISSORS,
    val countdownState: CountdownState = CountdownState.WAITING,
    val currentMessage: String = "",
    val jimScore: Int = 0,
    val hiScore: Int = 0,
    val jimChoice: MukjjippaChoice? = null,
    val hiChoice: MukjjippaChoice? = null,
    val attackerId: String? = null, // 공격자 ID (user_jim 또는 user_girlfriend)
    val previousAttackerChoice: MukjjippaChoice? = null, // 묵찌빠에서 사용할 이전 선택
    val winner: String? = null, // 게임 승리자
    val isGameFinished: Boolean = false,
    val bothPlayersReady: Boolean = false // 두 플레이어가 모두 게임에 참여했는지
) {
    fun getAttackerDisplayName(): String? {
        return when (attackerId) {
            "user_jim" -> "Jim"
            "user_girlfriend" -> "Hi"
            else -> null
        }
    }

    fun getWinnerDisplayName(): String? {
        return when (winner) {
            "user_jim" -> "Jim"
            "user_girlfriend" -> "Hi"
            else -> null
        }
    }

    fun isChoiceComplete(): Boolean {
        return jimChoice != null && hiChoice != null
    }

    fun resetChoices(): MukjjippaGameState {
        return copy(
            jimChoice = null,
            hiChoice = null,
            countdownState = CountdownState.WAITING,
            currentMessage = ""
        )
    }

    fun getScoreForPlayer(playerId: String): Int {
        return when (playerId) {
            "user_jim" -> jimScore
            "user_girlfriend" -> hiScore
            else -> 0
        }
    }

    fun getChoiceForPlayer(playerId: String): MukjjippaChoice? {
        return when (playerId) {
            "user_jim" -> jimChoice
            "user_girlfriend" -> hiChoice
            else -> null
        }
    }
}