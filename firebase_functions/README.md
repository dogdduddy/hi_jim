# Firebase Functions - Push Notification Service

이 디렉토리는 게임 요청 시 푸시 알림을 보내는 Firebase Functions를 포함합니다.

## 설치

```bash
npm install
```

## 배포

Firebase CLI가 설치되어 있어야 합니다:

```bash
npm install -g firebase-tools
```

로그인:

```bash
firebase login
```

Functions 배포:

```bash
firebase deploy --only functions
```

## Functions 설명

### sendGameRequestNotification

- **Trigger**: `/gameRequests/{requestId}` 생성 시
- **동작**: 게임 요청이 생성되면 받는 사람(toUserId)에게 푸시 알림 전송
- **알림 내용**: "{fromUserName}님이 스모 게임을 하자고 했어요!"

### sendGameRequestAcceptedNotification

- **Trigger**: `/gameRequests/{requestId}` 업데이트 시
- **동작**: 게임 요청이 PENDING -> ACCEPTED로 변경되면 요청을 보낸 사람(fromUserId)에게 알림 전송
- **알림 내용**: "{toUserName}님이 게임 요청을 수락했어요!"

## 필수 설정

1. Firebase 프로젝트에서 Cloud Messaging API가 활성화되어 있어야 합니다
2. 각 사용자의 FCM 토큰이 `/users/{userId}/fcmToken` 경로에 저장되어야 합니다
3. Android 및 iOS 앱에서 FCM이 올바르게 설정되어 있어야 합니다

## 테스트

Functions 로그 확인:

```bash
firebase functions:log
```

특정 function 로그만 보기:

```bash
firebase functions:log --only sendGameRequestNotification
```
