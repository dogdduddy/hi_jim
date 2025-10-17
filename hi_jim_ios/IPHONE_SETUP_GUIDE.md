# iPhone Companion App 설정 가이드

iPhone companion 앱을 추가하여 Apple Watch에서 푸시 알림을 받을 수 있도록 합니다.

## Xcode에서 iPhone 타겟 추가하기

### 1. 새로운 타겟 추가

1. Xcode에서 프로젝트를 엽니다
2. 프로젝트 네비게이터에서 최상위 프로젝트를 선택합니다
3. 하단의 `+` 버튼을 클릭하여 새 타겟을 추가합니다
4. **iOS** → **App** 선택
5. 다음 정보를 입력합니다:
   - Product Name: `hi_jim iOS App`
   - Team: 본인의 Apple Developer Team
   - Organization Identifier: `com.jim`
   - Bundle Identifier: `com.jim.hi-jim.ios`
   - Interface: `SwiftUI`
   - Language: `Swift`
   - Include Tests: 체크 해제

### 2. Firebase 의존성 추가

1. 프로젝트를 선택하고 **Package Dependencies** 탭으로 이동
2. Firebase SDK가 이미 추가되어 있으므로, 새 타겟에 다음 라이브러리를 추가합니다:
   - `hi_jim iOS App` 타겟 선택
   - **General** → **Frameworks, Libraries, and Embedded Content**
   - 다음 Firebase 라이브러리 추가:
     - FirebaseCore
     - FirebaseDatabase
     - FirebaseMessaging

### 3. 파일 추가

다음 파일들을 새 타겟에 추가합니다:

#### 새로 생성된 파일들 (Target Membership 설정):
- `hi_jim iOS App/hi_jim_iOS_App.swift` → `hi_jim iOS App` 타겟에만
- `hi_jim iOS App/iOSAppDelegate.swift` → `hi_jim iOS App` 타겟에만
- `hi_jim iOS App/iOSFCMTokenManager.swift` → `hi_jim iOS App` 타겟에만
- `hi_jim iOS App/ContentView.swift` → `hi_jim iOS App` 타겟에만

#### 기존 파일 공유 (Target Membership에 추가):
1. `hi_jim Watch App/Models/GameModels.swift`를 선택
2. 오른쪽 패널 → **Target Membership**
3. `hi_jim iOS App` 체크박스를 선택하여 두 타겟에서 공유

### 4. GoogleService-Info.plist 추가

1. Firebase Console에서 다운로드한 `GoogleService-Info.plist` 파일을 준비
2. Xcode에서 `hi_jim iOS App` 폴더에 드래그 앤 드롭
3. **Target Membership**에서 `hi_jim iOS App`만 선택

### 5. Push Notification 권한 추가

1. `hi_jim iOS App` 타겟 선택
2. **Signing & Capabilities** 탭으로 이동
3. `+ Capability` 버튼 클릭
4. **Push Notifications** 추가
5. **Background Modes** 추가
   - Remote notifications 체크

### 6. Info.plist 설정 (필요시)

`hi_jim iOS App/Info.plist` 파일에 다음 추가 (이미 있을 수 있음):

```xml
<key>UIBackgroundModes</key>
<array>
    <string>remote-notification</string>
</array>
```

### 7. 기본 파일 수정

Xcode가 자동 생성한 다음 파일들을 삭제하거나 교체합니다:
- ❌ `hi_jim_iOS_AppApp.swift` (자동 생성됨) → 삭제
- ✅ `hi_jim iOS App/hi_jim_iOS_App.swift` (우리가 만든 것) 사용
- ❌ `ContentView.swift` (자동 생성됨) → 삭제
- ✅ `hi_jim iOS App/ContentView.swift` (우리가 만든 것) 사용

## 빌드 및 테스트

1. Scheme를 `hi_jim iOS App`으로 선택
2. iPhone 시뮬레이터 선택 (실제 기기 권장)
3. ▶️ Run

## 작동 원리

1. **iPhone 앱 시작 시**:
   - FCM 토큰이 자동으로 생성됨
   - 토큰이 Firebase Database의 `users/{userId}/fcmToken`에 저장됨

2. **게임 요청 시**:
   - Firebase Functions가 FCM 토큰을 사용하여 iPhone에 푸시 알림 전송
   - iPhone이 알림을 받으면 자동으로 Apple Watch에도 미러링됨

3. **알림 클릭 시**:
   - iPhone 앱이 열리거나 foreground로 이동
   - Apple Watch에서는 기존대로 게임 로비에서 요청 확인 가능

## 참고사항

- iPhone과 Apple Watch가 페어링되어 있어야 알림이 미러링됩니다
- iPhone 앱은 알림 수신 전용이므로 게임 기능은 없습니다
- 사용자는 Apple Watch에서만 게임을 플레이합니다
- 두 기기 모두 동일한 `CURRENT_USER_ID`를 사용해야 합니다

## 트러블슈팅

### FCM 토큰이 저장되지 않는 경우
1. Firebase Console에서 APNs 인증서가 업로드되어 있는지 확인
2. Bundle Identifier가 Firebase 프로젝트와 일치하는지 확인
3. Xcode 로그에서 FCM 토큰 출력 확인

### 알림이 오지 않는 경우
1. iPhone 설정 → 알림 → hi_jim iOS App → 알림 허용 확인
2. Firebase Functions가 정상 배포되었는지 확인
3. Firebase Database에 FCM 토큰이 저장되었는지 확인
