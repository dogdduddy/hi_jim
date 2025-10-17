package com.jim.hi_jim.fcm

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.jim.hi_jim.presentation.constants.UserConstants

object FCMTokenManager {
    private const val TAG = "FCMTokenManager"

    /**
     * FCM 토큰을 Firebase Database에 저장
     * 경로: users/{userId}/fcmToken
     */
    fun saveTokenToFirebase(token: String) {
        val userId = UserConstants.CURRENT_USER_ID
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users/$userId")

        userRef.child("fcmToken").setValue(token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token saved successfully: $token")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to save FCM token: ${error.message}")
            }
    }

    /**
     * 현재 기기의 FCM 토큰을 가져와서 Firebase에 저장
     */
    fun refreshToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(TAG, "FCM token retrieved: $token")
                saveTokenToFirebase(token)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to retrieve FCM token: ${error.message}")
            }
    }
}
