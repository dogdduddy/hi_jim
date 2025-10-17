package com.jim.hi_jim.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jim.hi_jim.R
import com.jim.hi_jim.presentation.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM Token: $token")

        // FCM 토큰을 Firebase Database에 저장
        FCMTokenManager.saveTokenToFirebase(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // 알림 데이터 추출
        val title = message.data["title"] ?: message.notification?.title ?: "게임 요청"
        val body = message.data["body"] ?: message.notification?.body ?: ""
        val requestId = message.data["requestId"]
        val fromUserId = message.data["fromUserId"]

        Log.d(TAG, "Title: $title, Body: $body, RequestId: $requestId")

        // 알림 표시
        showNotification(title, body, requestId, fromUserId)
    }

    private fun showNotification(title: String, body: String, requestId: String?, fromUserId: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 알림 채널 생성 (Android O 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "게임 요청",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "스모 게임 요청 알림"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 클릭 시 앱 열고 게임 로비로 이동
        val openIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("openGameLobby", true)
            putExtra("requestId", requestId)
            putExtra("fromUserId", fromUserId)
        } ?: Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("openGameLobby", true)
            putExtra("requestId", requestId)
            putExtra("fromUserId", fromUserId)
        }

        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 생성 (액션 버튼 없이 단순화)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "FCM"
        private const val CHANNEL_ID = "game_request_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
