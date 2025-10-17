# Hi Jim - iOS/watchOS 멀티플레이어 스모 게임

Apple Watch용 실시간 멀티플레이어 스모 레슬링 게임입니다.

## 프로젝트 구조

```
hi_jim Watch App/
├── hi_jimApp.swift                      # 앱 엔트리 포인트, Firebase 초기화
├── Models/
│   └── GameModels.swift                 # 데이터 모델 (게임 상태, 요청 등)
├── Engine/
│   └── SumoPhysicsEngine.swift          # 게임 물리 엔진
├── Repository/
│   └── FirebaseGameRepository.swift     # Firebase 데이터 레이어
├── ViewModels/
│   ├── GameLobbyViewModel.swift         # 로비 상태 관리
│   └── MultiplayerGameViewModel.swift   # 게임 상태 관리
└── Views/
    ├── GameLobbyView.swift              # 로비 화면
    ├── MultiplayerGameView.swift        # 게임 화면
    └── UserSetupView.swift              # 사용자 선택 화면
```

## 주요 기능

### 1. 데이터 모델 (GameModels.swift)
- `SumoGameState`: 게임 상태 (플레이어 위치, 점수, 충돌 등)
- `MultiplayerGameData`: Firebase 동기화용 데이터
- `GameRequest`: 게임 요청/응답
- `UserConstants`: 사용자 식별 (USER_1, USER_2)

### 2. 게임 물리 엔진 (SumoPhysicsEngine.swift)
- 게임 상수
  - `BOUNDARY = 10`: 경계 범위
  - `STEP_SIZE = 0.8`: 탭당 이동 거리
  - `PLAYER_RADIUS = 2.5`: 플레이어 반지름
  - `PUSH_FORCE = 1.5`: 충돌 시 밀어내는 힘

- 주요 메서드
  - `processMove()`: 플레이어 이동 및 충돌 처리
  - `checkCollisionAndPush()`: 충돌 감지 및 밀어내기
  - `checkWinCondition()`: 승리 조건 확인
  - `resetRound()`: 라운드 재시작 (점수 유지)

### 3. Firebase Repository (FirebaseGameRepository.swift)
- 게임 요청 관리
  - `sendGameRequest()`: 게임 요청 보내기
  - `observeGameRequests()`: 받은 요청 실시간 감지
  - `observeSentRequest()`: 보낸 요청 상태 확인
  - `respondToGameRequest()`: 요청 수락/거절

- 게임 상태 관리
  - `observeGameState()`: 게임 상태 실시간 감지
  - `sendPlayerMove()`: 플레이어 이동 전송
  - `resetRound()`: 라운드 재시작
  - `endGame()`: 게임 종료

### 4. ViewModel

**GameLobbyViewModel**
- 게임 로비 상태 관리
- 게임 요청 보내기/취소
- 받은 요청 수락/거절
- 게임 시작 시 자동 화면 전환

**MultiplayerGameViewModel**
- 게임 상태 실시간 동기화
- 플레이어 이동 처리
- 충돌 애니메이션 관리
- 상대가 나갔을 때 자동 로비 복귀

### 5. UI (SwiftUI for watchOS)

**GameLobbyView**
- 현재 사용자 표시 및 변경
- 상대에게 게임 요청 보내기
- 받은 요청 목록 및 수락/거절 버튼
- 보낸 요청 상태 표시 및 취소 버튼

**MultiplayerGameView**
- 게임 진행 화면
  - 전체 화면 탭으로 이동
  - 두 플레이어 원형 표시 (파란색/빨간색)
  - 충돌 효과 애니메이션
  - 점수판
- 게임 결과 화면
  - 승/패 표시
  - 최종 점수
  - 다시 시작 / 나가기 버튼

**UserSetupView**
- 사용자 선택 (user_jim / user_girlfriend)
- UserDefaults에 저장

## Firebase 데이터 구조

```
firebase-root/
├── gameRequests/
│   ├── {userId}/
│   │   └── {requestId}/
│   │       ├── requestId
│   │       ├── fromUserId
│   │       ├── toUserId
│   │       ├── status (PENDING/ACCEPTED/REJECTED)
│   │       ├── timestamp
│   │       └── gameId
└── games/
    └── {gameId}/
        ├── gameId
        ├── player1Id
        ├── player2Id
        ├── player1Position
        ├── player2Position
        ├── gameStatus (PLAYING/PLAYER1_WIN/PLAYER2_WIN)
        ├── player1Score
        ├── player2Score
        ├── lastMovePlayerId
        ├── lastMoveTimestamp
        ├── collisionPosition
        └── collisionTimestamp
```

## 설정 방법

### 1. Xcode 프로젝트 열기
```bash
cd hi_jim_ios
open hi_jim.xcodeproj
```

### 2. Firebase iOS SDK 추가 (Swift Package Manager)
Xcode에서:
1. File > Add Package Dependencies
2. 다음 패키지들 추가:
   - `https://github.com/firebase/firebase-ios-sdk`
   - 필요한 제품: FirebaseCore, FirebaseDatabase

### 3. 빌드 및 실행
1. Xcode에서 시뮬레이터 또는 실제 Apple Watch 선택
2. Command + R로 빌드 및 실행

### 4. 두 개의 워치에서 테스트
- **첫 번째 워치**: 사용자를 `user_jim`으로 설정
- **두 번째 워치**: 사용자를 `user_girlfriend`로 설정
- 한쪽에서 게임 요청을 보내면 다른 쪽에서 수락
- 게임 시작!

## 게임 방법

1. **로비에서**
   - 상대에게 게임 요청 보내기
   - 또는 받은 요청 수락하기

2. **게임 중**
   - 화면 아무 곳이나 탭하여 앞으로 이동
   - 상대와 충돌하면 밀어내기
   - 상대를 경계 밖으로 밀어내면 승리!

3. **게임 종료**
   - 다시 시작: 같은 상대와 계속 플레이 (점수 유지)
   - 나가기: 로비로 돌아가기

## 사용자 식별

- 로비 화면 상단의 사용자 이름을 탭하면 변경 가능
- `user_jim`: Player 1 (왼쪽 시작, 파란색)
- `user_girlfriend`: Player 2 (오른쪽 시작, 빨간색)
- UserDefaults에 저장되어 앱 재시작 후에도 유지

## Android 버전과의 호환성

이 iOS 앱은 Android Wear OS 버전과 완전히 호환됩니다:
- 동일한 Firebase 데이터베이스 사용
- 동일한 게임 물리 엔진 로직
- iOS ↔ Android 간 멀티플레이 가능

## 주요 특징

- ✅ 실시간 동기화 (Firebase Realtime Database)
- ✅ Combine 프레임워크 기반 반응형 UI
- ✅ watchOS 최적화 (작은 화면, 탭 인터랙션)
- ✅ 자동 재연결 및 에러 처리
- ✅ 상대가 나갔을 때 자동 로비 복귀
- ✅ SwiftUI 기반 모던 UI

## 개발 노트

### KMP 공유 모듈 대신 Swift로 구현한 이유
- 원래는 Kotlin Multiplatform의 공유 모듈을 사용할 계획이었으나,
- Xcode command line tools 설정 문제로 프레임워크 빌드가 어려워
- 대신 게임 로직을 Swift로 재구현했습니다
- Android 버전의 코드를 그대로 번역하여 동일한 동작을 보장합니다

### 향후 개선 사항
- [ ] Xcode build phase에 KMP 프레임워크 빌드 통합
- [ ] 햅틱 피드백 추가 (충돌 시, 승리 시)
- [ ] 사운드 효과
- [ ] 게임 통계 및 랭킹
- [ ] 여러 명의 사용자 지원

## 문제 해결

### Firebase 연결 안 됨
- `GoogleService-Info.plist` 파일이 프로젝트에 포함되어 있는지 확인
- Firebase Console에서 iOS 앱이 등록되어 있는지 확인

### 게임 요청이 안 보임
- 두 워치가 서로 다른 사용자 ID로 설정되어 있는지 확인
- Firebase Console에서 데이터가 실제로 저장되는지 확인

### 빌드 에러
- Firebase SDK가 제대로 설치되어 있는지 확인
- Xcode를 재시작해보세요
- Clean Build Folder (Command + Shift + K)

## 라이선스

이 프로젝트는 개인 프로젝트입니다.
