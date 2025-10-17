package com.jim.hi_jim.shared.constants

object UserConstants {
    // 하드코딩된 사용자 ID (본인과 여자친구)
    const val USER_1 = "user_jim"
    const val USER_2 = "user_girlfriend"

    // 현재 디바이스의 사용자 ID (각 워치마다 설정)
    // 실제로는 SharedPreferences나 다른 방식으로 저장하겠지만, 우선 하드코딩
    const val CURRENT_USER_ID = USER_2  // 이 값을 각 워치에서 다르게 설정
}
