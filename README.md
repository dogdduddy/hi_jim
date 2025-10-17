# Hi Jim - 크로스플랫폼 멀티플레이어 스모 게임

웨어러블 기기를 위한 실시간 멀티플레이어 스모 레슬링 게임입니다.
Android Wear OS와 Apple Watch에서 플레이 가능하며, 플랫폼 간 멀티플레이를 지원합니다!

## 프로젝트 구조

```
hi_jim/
├── hi_jim_android/              # Android Wear OS 앱
│   ├── app/                     # Android 앱 코드
│   └── shared/                  # Kotlin Multiplatform 공유 모듈
│       └── src/
│           ├── commonMain/      # 공유 로직 (게임 엔진, 모델)
│           ├── androidMain/     # Android 전용 코드
│           └── iosMain/         # iOS 전용 코드
└── hi_jim_ios/                  # iOS/watchOS 앱
    └── hi_jim Watch App/        # watchOS 앱 코드
```

## 게임 개요

### 컨셉
두 플레이어가 서로 다른 워치에서 화면을 탭하여 상대를 밀어내는 간단하면서도 중독성 있는 게임입니다.

### 게임 규칙
1. 각 플레이어는 반대편에서 시작 (position -5와 +5)
2. 화면을 탭하면 상대 방향으로 이동 (STEP_SIZE = 0.8)
3. 플레이어가 충돌하면 움직인 플레이어가 상대를 밀어냄 (PUSH_FORCE = 1.5)
4. 상대를 경계(±10) 밖으로 밀어내면 승리!
5. 점수는 라운드를 거쳐 누적됩니다

## 플랫폼별 구현

### Android (Wear OS)
- **언어**: Kotlin
- **UI**: Jetpack Compose for Wear OS
- **상태 관리**: ViewModel + Flow
- **공유 로직**: KMP 공유 모듈 사용
- **상세 내용**: [hi_jim_android/README.md](./hi_jim_android/README.md)

### iOS (watchOS)
- **언어**: Swift
- **UI**: SwiftUI for watchOS
- **상태 관리**: ObservableObject + Combine
- **공유 로직**: Swift로 재구현 (게임 물리 엔진 동일)
- **상세 내용**: [hi_jim_ios/README.md](./hi_jim_ios/README.md)
- **설정 가이드**: [hi_jim_ios/SETUP_GUIDE.md](./hi_jim_ios/SETUP_GUIDE.md)

## 주요 기술 스택

### 공통
- **백엔드**: Firebase Realtime Database
- **게임 엔진**: 동일한 물리 엔진 로직 (Kotlin/Swift)
- **실시간 동기화**: Firebase를 통한 양방향 데이터 동기화

### 공유 게임 로직
양쪽 플랫폼 모두 동일한 게임 물리를 구현합니다:

```kotlin
// 게임 상수
BOUNDARY = 10f          // 경계 범위
STEP_SIZE = 0.8f        // 탭당 이동 거리
PLAYER_RADIUS = 2.5f    // 플레이어 반지름
PUSH_FORCE = 1.5f       // 충돌 시 밀어내는 힘
```

### 데이터 모델
```kotlin
data class SumoGameState(
    val player1Position: Float = -5f,
    val player2Position: Float = 5f,
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val collisionPosition: Float? = null,
    val collisionTimestamp: Long = 0L
)
```

## Firebase 구조

```
firebase-root/
├── gameRequests/
│   └── {userId}/
│       └── {requestId}/
│           ├── fromUserId
│           ├── toUserId
│           ├── status (PENDING/ACCEPTED/REJECTED)
│           └── gameId
└── games/
    └── {gameId}/
        ├── player1Id
        ├── player2Id
        ├── player1Position
        ├── player2Position
        ├── gameStatus
        ├── player1Score
        ├── player2Score
        └── collisionPosition
```

## 사용자 흐름

### 1. 사용자 설정
- 각 워치에서 사용자 선택 (user_jim 또는 user_girlfriend)
- UserDefaults/SharedPreferences에 저장

### 2. 게임 로비
- 상대 유저 표시
- 게임 요청 보내기/받기
- 요청 수락 시 자동으로 게임 시작

### 3. 게임 플레이
- 전체 화면 탭으로 플레이
- 실시간 위치 동기화
- 충돌 효과 및 점수 표시

### 4. 게임 종료
- 승/패 결과 표시
- 다시 시작 (점수 유지)
- 로비로 돌아가기

## 크로스플랫폼 호환성

이 프로젝트의 핵심 특징은 **Android와 iOS 간 멀티플레이**가 가능하다는 점입니다!

- ✅ Android ↔ Android 멀티플레이
- ✅ iOS ↔ iOS 멀티플레이
- ✅ **Android ↔ iOS 크로스플랫폼 멀티플레이**

동일한 Firebase Realtime Database를 공유하므로, 플랫폼에 관계없이 함께 플레이할 수 있습니다.

## 시작하기

### Android
```bash
cd hi_jim_android
./gradlew :app:installDebug
```

자세한 내용은 [Android README](./hi_jim_android/README.md) 참조

### iOS
```bash
cd hi_jim_ios
open hi_jim.xcodeproj
```

자세한 내용은 [iOS SETUP_GUIDE](./hi_jim_ios/SETUP_GUIDE.md) 참조

## Firebase 설정

1. [Firebase Console](https://console.firebase.google.com/)에서 프로젝트 생성

2. **Android 앱 추가**:
   - Bundle ID 설정
   - `google-services.json` 다운로드 → `hi_jim_android/app/`에 저장

3. **iOS 앱 추가**:
   - Bundle ID 설정
   - `GoogleService-Info.plist` 다운로드 → `hi_jim_ios/hi_jim Watch App/`에 저장

4. **Realtime Database 규칙** (개발 중):
   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```

   **프로덕션**에서는 보안 규칙을 적절히 설정하세요!

## 개발 노트

### Kotlin Multiplatform (KMP)
- Android 앱은 KMP 공유 모듈을 사용하여 게임 로직을 공유합니다
- iOS 앱도 KMP 프레임워크를 사용할 수 있도록 설계되었으나, 현재는 Swift로 재구현되어 있습니다
- 향후 Xcode 빌드 시스템에 KMP 프레임워크 빌드를 통합할 예정입니다

### 게임 물리 엔진
두 플랫폼 모두 동일한 로직을 구현합니다:
1. **이동 처리**: 탭한 플레이어만 STEP_SIZE만큼 이동
2. **충돌 감지**: 두 플레이어 중심 거리가 PLAYER_RADIUS * 2 미만
3. **밀어내기**: 충돌 시 겹친 부분 계산 + PUSH_FORCE 적용
4. **승리 조건**: 플레이어가 경계(±10) 밖으로 나가면 상대 승리

### 실시간 동기화
- Firebase Realtime Database를 통한 양방향 동기화
- 한쪽에서 이동하면 즉시 반대편에 반영
- 상대가 게임을 나가면 자동으로 로비로 복귀

## 테스트 시나리오

### 시나리오 1: 같은 플랫폼
1. Android 시뮬레이터 2개 또는 실제 기기 2개
2. 한쪽을 user_jim, 다른 쪽을 user_girlfriend로 설정
3. 게임 요청 → 수락 → 플레이

### 시나리오 2: 크로스플랫폼
1. Android 기기 1개 + Apple Watch 1개
2. 각각 다른 사용자로 설정
3. 플랫폼 간 멀티플레이 테스트

### 시나리오 3: 네트워크 안정성
1. 게임 중 한쪽 기기의 네트워크 끊기
2. 재연결 시 상태 복구 확인
3. 상대가 완전히 나갔을 때 로비 복귀 확인

## 주요 기능

- ✅ 실시간 멀티플레이어 게임
- ✅ 크로스플랫폼 지원 (Android ↔ iOS)
- ✅ 게임 요청/응답 시스템
- ✅ 충돌 감지 및 물리 시뮬레이션
- ✅ 점수 추적 및 라운드 시스템
- ✅ 자동 재연결 및 에러 처리
- ✅ 웨어러블 최적화 UI

## 향후 계획

- [ ] KMP 프레임워크를 iOS에서도 사용
- [ ] 햅틱 피드백 및 사운드 효과
- [ ] 게임 통계 및 랭킹 시스템
- [ ] 여러 게임 모드 추가
- [ ] 관전 모드
- [ ] 친구 시스템

## 라이선스

이 프로젝트는 개인 프로젝트입니다.

## 문의

문제가 발생하거나 질문이 있으시면 이슈를 등록해주세요!
