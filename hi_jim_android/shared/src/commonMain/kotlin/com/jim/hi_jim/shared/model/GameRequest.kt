package com.jim.hi_jim.shared.model

data class GameRequest(
    val requestId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val gameType: GameType = GameType.SUMO,  // 게임 타입 (SUMO, MUKJJIPPA)
    val status: GameRequestStatus = GameRequestStatus.PENDING,
    val timestamp: Long = 0L,
    val gameId: String? = null  // 수락 시 생성된 게임 ID
) {
    // Firebase에서 가져올 때 필요한 빈 생성자
    constructor() : this("", "", "", GameType.SUMO, GameRequestStatus.PENDING, 0L, null)
}

enum class GameRequestStatus {
    PENDING,   // 대기 중
    ACCEPTED,  // 수락됨
    REJECTED   // 거절됨
}
