const {onValueCreated, onValueUpdated} = require('firebase-functions/v2/database');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * 게임 요청이 생성될 때 상대방에게 푸시 알림 전송
 *
 * Trigger: /gameRequests/{requestId} 생성 시
 */
exports.sendGameRequestNotification = onValueCreated(
    {
        ref: '/gameRequests/{toUserId}/{requestId}',
        instance: 'hi-jim-99200-default-rtdb',
        region: 'asia-southeast1'
    },
    async (event) => {
        const snapshot = event.data;
        const requestId = event.params.requestId;
        const toUserId = event.params.toUserId;
        const requestData = snapshot.val();

        console.log('Game request created:', requestId, toUserId, requestData);

        // 요청 데이터 검증
        if (!requestData || !requestData.fromUserId || !requestData.toUserId) {
            console.error('Invalid request data');
            return null;
        }

        const { fromUserId, status } = requestData;

        // toUserId(경로)와 requestData.toUserId가 일치하는지 확인 (중복 방지)
        if (toUserId !== requestData.toUserId) {
            console.log('Skipping: this is sender copy, not receiver copy');
            return null;
        }

        // PENDING 상태가 아니면 알림 보내지 않음
        if (status !== 'PENDING') {
            console.log('Request status is not PENDING, skipping notification');
            return null;
        }

        try {
            // 받는 사람의 FCM 토큰 가져오기
            const toUserSnapshot = await admin.database()
                .ref(`/users/${toUserId}/fcmToken`)
                .once('value');

            const fcmToken = toUserSnapshot.val();

            if (!fcmToken) {
                console.log(`No FCM token found for user ${toUserId}`);
                return null;
            }

            // 보내는 사람의 이름 가져오기 (옵션)
            const fromUserSnapshot = await admin.database()
                .ref(`/users/${fromUserId}/name`)
                .once('value');

            const fromUserName = fromUserSnapshot.val() || fromUserId;

            // 알림 메시지 구성 (data-only 메시지로 변경)
            const message = {
                token: fcmToken,
                data: {
                    title: '게임 요청',
                    body: `${fromUserName}님이 스모 게임을 하자고 했어요!`,
                    requestId: requestId,
                    fromUserId: fromUserId,
                    type: 'game_request',
                },
                android: {
                    priority: 'high',
                },
            };

            // FCM 전송
            const response = await admin.messaging().send(message);
            console.log('Successfully sent notification:', response);

            return response;
        } catch (error) {
            console.error('Error sending notification:', error);
            return null;
        }
    }
);

/**
 * 게임 요청 상태가 변경될 때 처리
 * ACCEPTED 상태가 되면 요청을 보낸 사람에게 알림 전송
 */
exports.sendGameRequestAcceptedNotification = onValueUpdated(
    {
        ref: '/gameRequests/{userId}/{requestId}',
        instance: 'hi-jim-99200-default-rtdb',
        region: 'asia-southeast1'
    },
    async (event) => {
        const requestId = event.params.requestId;
        const beforeData = event.data.before.val();
        const afterData = event.data.after.val();

        console.log('Game request updated:', requestId, beforeData?.status, '->', afterData?.status);

        // beforeData가 null이면 새로 생성된 것 (onCreate 대신 onUpdate 실행됨)
        if (!beforeData && afterData && afterData.status === 'PENDING') {
            console.log('New request detected in onUpdate, sending notification...');

            const { fromUserId, toUserId } = afterData;
            const userIdFromPath = event.params.userId;

            // userId(경로)와 toUserId가 일치하는지 확인 (중복 방지)
            if (userIdFromPath !== toUserId) {
                console.log('Skipping onUpdate: this is sender copy, not receiver copy');
                return null;
            }

            try {
                // 받는 사람의 FCM 토큰 가져오기
                const toUserSnapshot = await admin.database()
                    .ref(`/users/${toUserId}/fcmToken`)
                    .once('value');

                const fcmToken = toUserSnapshot.val();

                if (!fcmToken) {
                    console.log(`No FCM token found for user ${toUserId}`);
                    return null;
                }

                // 보내는 사람의 이름 가져오기
                const fromUserSnapshot = await admin.database()
                    .ref(`/users/${fromUserId}/name`)
                    .once('value');

                const fromUserName = fromUserSnapshot.val() || fromUserId;

                // 알림 메시지 구성 (data-only)
                const message = {
                    token: fcmToken,
                    data: {
                        title: '게임 요청',
                        body: `${fromUserName}님이 스모 게임을 하자고 했어요!`,
                        requestId: requestId,
                        fromUserId: fromUserId,
                        type: 'game_request',
                    },
                    android: {
                        priority: 'high',
                    },
                };

                // FCM 전송
                const response = await admin.messaging().send(message);
                console.log('Successfully sent notification:', response);

                return response;
            } catch (error) {
                console.error('Error sending notification:', error);
                return null;
            }
        }

        // PENDING -> ACCEPTED로 변경되었는지 확인
        if (beforeData?.status === 'PENDING' && afterData?.status === 'ACCEPTED') {
            const { fromUserId, toUserId } = afterData;

            try {
                // 요청을 보낸 사람의 FCM 토큰 가져오기
                const fromUserSnapshot = await admin.database()
                    .ref(`/users/${fromUserId}/fcmToken`)
                    .once('value');

                const fcmToken = fromUserSnapshot.val();

                if (!fcmToken) {
                    console.log(`No FCM token found for user ${fromUserId}`);
                    return null;
                }

                // 수락한 사람의 이름 가져오기
                const toUserSnapshot = await admin.database()
                    .ref(`/users/${toUserId}/name`)
                    .once('value');

                const toUserName = toUserSnapshot.val() || toUserId;

                // 알림 메시지 구성
                const message = {
                    token: fcmToken,
                    notification: {
                        title: '게임 요청 수락됨',
                        body: `${toUserName}님이 게임 요청을 수락했어요!`,
                    },
                    data: {
                        requestId: requestId,
                        fromUserId: toUserId,
                        type: 'game_request_accepted',
                    },
                    android: {
                        priority: 'high',
                        notification: {
                            channelId: 'game_request_channel',
                        },
                    },
                    apns: {
                        headers: {
                            'apns-priority': '10',
                        },
                        payload: {
                            aps: {
                                alert: {
                                    title: '게임 요청 수락됨',
                                    body: `${toUserName}님이 게임 요청을 수락했어요!`,
                                },
                                sound: 'default',
                            },
                        },
                    },
                };

                // FCM 전송
                const response = await admin.messaging().send(message);
                console.log('Successfully sent acceptance notification:', response);

                return response;
            } catch (error) {
                console.error('Error sending acceptance notification:', error);
                return null;
            }
        }

        return null;
    }
);
