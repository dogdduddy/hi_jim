package com.jim.hi_jim.shared.model

data class MultiplayerMukjjippaData(
    val gameId: String = "",
    val player1Id: String = "",  // user_jim
    val player2Id: String = "",  // user_girlfriend
    val gameType: String = GameType.MUKJJIPPA.name,
    val phase: String = MukjjippaPhase.ROCK_PAPER_SCISSORS.name,
    val countdownState: String = CountdownState.WAITING.name,
    val currentMessage: String = "",
    val jimScore: Int = 0,
    val hiScore: Int = 0,
    val jimChoice: String? = null, // MukjjippaChoice의 name
    val hiChoice: String? = null,  // MukjjippaChoice의 name
    val attackerId: String? = null,
    val previousAttackerChoice: String? = null, // MukjjippaChoice의 name
    val winner: String? = null,
    val isGameFinished: Boolean = false,
    val bothPlayersReady: Boolean = false,
    val lastMovePlayerId: String = "",
    val lastMoveTimestamp: Long = 0L
) {
    // Firebase에서 가져올 때 필요한 빈 생성자
    constructor() : this("", "", "", GameType.MUKJJIPPA.name, MukjjippaPhase.ROCK_PAPER_SCISSORS.name, CountdownState.WAITING.name, "", 0, 0, null, null, null, null, null, false, false, "", 0L)

    // MukjjippaGameState로 변환
    fun toMukjjippaGameState(): MukjjippaGameState {
        val phaseEnum = MukjjippaPhase.valueOf(phase)
        return MukjjippaGameState(
            phase = phaseEnum,
            countdownState = CountdownState.valueOf(countdownState),
            currentMessage = currentMessage,
            jimScore = jimScore,
            hiScore = hiScore,
            jimChoice = jimChoice?.let { MukjjippaChoice.valueOf(it) },
            hiChoice = hiChoice?.let { MukjjippaChoice.valueOf(it) },
            attackerId = attackerId,
            previousAttackerChoice = previousAttackerChoice?.let { MukjjippaChoice.valueOf(it) },
            winner = winner,
            // GAME_OVER 상태면 무조건 isGameFinished = true
            isGameFinished = phaseEnum == MukjjippaPhase.GAME_OVER || isGameFinished,
            bothPlayersReady = bothPlayersReady
        )
    }

    companion object {
        // MukjjippaGameState에서 변환
        fun fromMukjjippaGameState(
            gameId: String,
            player1Id: String,
            player2Id: String,
            state: MukjjippaGameState,
            lastMovePlayerId: String = ""
        ): MultiplayerMukjjippaData {
            return MultiplayerMukjjippaData(
                gameId = gameId,
                player1Id = player1Id,
                player2Id = player2Id,
                phase = state.phase.name,
                countdownState = state.countdownState.name,
                currentMessage = state.currentMessage,
                jimScore = state.jimScore,
                hiScore = state.hiScore,
                jimChoice = state.jimChoice?.name,
                hiChoice = state.hiChoice?.name,
                attackerId = state.attackerId,
                previousAttackerChoice = state.previousAttackerChoice?.name,
                winner = state.winner,
                isGameFinished = state.isGameFinished,
                bothPlayersReady = state.bothPlayersReady,
                lastMovePlayerId = lastMovePlayerId,
                lastMoveTimestamp = System.currentTimeMillis()
            )
        }
    }
}