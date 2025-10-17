# iOS 프로젝트 설정 가이드

## 1. 필수 요구사항

- macOS with Xcode 15.0 이상
- Apple Watch (시뮬레이터 또는 실제 기기)
- Firebase 프로젝트 (Android와 동일한 프로젝트 사용)

## 2. Xcode 프로젝트 설정

### A. 프로젝트 열기
```bash
cd hi_jim_ios
open hi_jim.xcodeproj
```

### B. 새로운 파일들을 Xcode 프로젝트에 추가

Xcode에서:

1. **Models 폴더 추가**
   - 프로젝트 네비게이터에서 `hi_jim Watch App` 우클릭
   - New Group 선택, 이름을 `Models`로 설정
   - `GameModels.swift` 파일을 이 그룹으로 드래그

2. **Engine 폴더 추가**
   - New Group 생성, 이름 `Engine`
   - `SumoPhysicsEngine.swift` 파일 추가

3. **Repository 폴더 추가**
   - New Group 생성, 이름 `Repository`
   - `FirebaseGameRepository.swift` 파일 추가

4. **ViewModels 폴더 추가**
   - New Group 생성, 이름 `ViewModels`
   - `GameLobbyViewModel.swift` 파일 추가
   - `MultiplayerGameViewModel.swift` 파일 추가

5. **Views 폴더 추가**
   - New Group 생성, 이름 `Views`
   - `GameLobbyView.swift` 파일 추가
   - `MultiplayerGameView.swift` 파일 추가
   - `UserSetupView.swift` 파일 추가

**또는** 파일을 직접 Finder에서 Xcode로 드래그하면 자동으로 프로젝트에 추가됩니다.

### C. Firebase SDK 추가

1. Xcode에서 `File > Add Package Dependencies...` 선택

2. 검색창에 다음 URL 입력:
   ```
   https://github.com/firebase/firebase-ios-sdk
   ```

3. 버전 선택: `10.0.0` 이상 (최신 stable 버전)

4. 다음 제품들을 **hi_jim Watch App** 타겟에 추가:
   - ✅ `FirebaseCore`
   - ✅ `FirebaseDatabase`

5. `Add Package` 클릭

### D. GoogleService-Info.plist 확인

`hi_jim Watch App` 폴더에 `GoogleService-Info.plist` 파일이 있는지 확인합니다.

- **이미 있는 경우**: Android와 같은 Firebase 프로젝트를 사용 중이므로 그대로 사용
- **없는 경우**:
  1. [Firebase Console](https://console.firebase.google.com/)로 이동
  2. 프로젝트 선택
  3. iOS 앱 추가 (Bundle ID: `com.jim.hi-jim` 또는 프로젝트에 맞게 설정)
  4. `GoogleService-Info.plist` 다운로드
  5. Xcode 프로젝트의 `hi_jim Watch App` 폴더에 드래그

## 3. 빌드 설정

### A. Xcode command line tools 설정 (필수)

터미널에서 실행:
```bash
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
```

비밀번호 입력 후 확인:
```bash
xcode-select --print-path
```

출력이 `/Applications/Xcode.app/Contents/Developer`이어야 합니다.

### B. 타겟 설정 확인

Xcode에서:
1. 프로젝트 네비게이터에서 프로젝트 루트 선택
2. `hi_jim Watch App` 타겟 선택
3. `General` 탭에서:
   - **Minimum Deployments**: watchOS 9.0 이상
   - **Bundle Identifier**: Firebase에 등록한 것과 동일해야 함

4. `Signing & Capabilities` 탭에서:
   - **Team** 선택 (Apple Developer 계정 필요)
   - **Automatically manage signing** 체크

## 4. 빌드 및 실행

### A. 시뮬레이터에서 테스트

1. Xcode 상단의 스키마 선택기에서:
   - `hi_jim Watch App` 선택
   - 시뮬레이터 선택 (예: Apple Watch Series 9 (45mm))

2. `Command + R`로 빌드 및 실행

### B. 실제 기기에서 테스트

1. Apple Watch를 Mac에 연결된 iPhone과 페어링
2. Xcode에서 기기 선택
3. `Command + R`로 빌드 및 실행

## 5. 두 개의 워치에서 테스트

### 방법 1: 시뮬레이터 2개 사용

1. **첫 번째 시뮬레이터**:
   - 시뮬레이터 1 선택 및 실행
   - 앱이 실행되면 사용자를 `user_jim`으로 설정

2. **두 번째 시뮬레이터**:
   - Xcode에서 `Window > Devices and Simulators`
   - 새로운 시뮬레이터 생성
   - 새 시뮬레이터 선택 후 `Command + R`로 실행
   - 사용자를 `user_girlfriend`로 설정

3. 한 시뮬레이터에서 게임 요청 → 다른 시뮬레이터에서 수락

### 방법 2: 실제 기기 + 시뮬레이터

1. **실제 Apple Watch**: `user_jim`으로 설정
2. **시뮬레이터**: `user_girlfriend`로 설정
3. 서로 게임 요청 주고받기

### 방법 3: 실제 기기 2개

- 두 개의 Apple Watch를 각각 다른 사용자로 설정
- 가장 실제와 가까운 테스트 환경

## 6. 일반적인 문제 해결

### 문제 1: "Module 'FirebaseCore' not found"

**해결책**:
1. `File > Packages > Reset Package Caches`
2. `Product > Clean Build Folder` (Command + Shift + K)
3. Xcode 재시작
4. 다시 빌드

### 문제 2: "No such module 'FirebaseDatabase'"

**해결책**:
1. 프로젝트 설정에서 `hi_jim Watch App` 타겟 선택
2. `General` 탭의 `Frameworks, Libraries, and Embedded Content` 확인
3. Firebase 패키지가 없으면 다시 추가

### 문제 3: 빌드는 되는데 Firebase 연결 안 됨

**해결책**:
1. `GoogleService-Info.plist`가 프로젝트에 포함되어 있는지 확인
2. Firebase Console에서 iOS 앱이 등록되어 있는지 확인
3. Bundle ID가 일치하는지 확인
4. Realtime Database 규칙이 읽기/쓰기를 허용하는지 확인:
   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```

### 문제 4: Xcode command line tools 관련 에러

**해결책**:
```bash
# 현재 경로 확인
xcode-select --print-path

# Xcode 경로로 변경 (sudo 필요)
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer

# 다시 확인
xcode-select --print-path
```

### 문제 5: 코드 서명 에러

**해결책**:
1. Xcode > Preferences > Accounts에서 Apple ID 추가
2. 프로젝트 설정 > Signing & Capabilities에서 Team 선택
3. "Automatically manage signing" 체크

## 7. 디버깅 팁

### Console 로그 확인
- Xcode 하단의 Debug Area에서 Firebase 연결 및 게임 상태 로그 확인
- 예: "Game request sent: ...", "Request update: ..."

### Firebase Console 확인
1. [Firebase Console](https://console.firebase.google.com/)에서 프로젝트 선택
2. Realtime Database 탭에서 데이터 실시간 확인
   - `gameRequests/{userId}` 확인
   - `games/{gameId}` 확인

### 네트워크 문제
- 시뮬레이터/기기가 인터넷에 연결되어 있는지 확인
- Firebase 프로젝트의 Realtime Database 규칙 확인

## 8. 다음 단계

설정이 완료되었다면:

1. ✅ 앱이 빌드되고 실행됨
2. ✅ 사용자 선택 화면에서 사용자 설정 가능
3. ✅ 로비 화면에서 상대 사용자 표시
4. ✅ 게임 요청 보내기/받기 가능
5. ✅ 게임 플레이 가능

Android 버전과 함께 테스트하여 플랫폼 간 호환성을 확인하세요!

## 9. 추가 리소스

- [Firebase iOS 설정 가이드](https://firebase.google.com/docs/ios/setup)
- [SwiftUI 문서](https://developer.apple.com/documentation/swiftui)
- [watchOS 앱 개발 가이드](https://developer.apple.com/watchos/)
- [Combine 프레임워크](https://developer.apple.com/documentation/combine)
