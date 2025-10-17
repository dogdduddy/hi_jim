package com.jim.hi_jim.data.repository

import android.util.Log
import com.google.firebase.database.*
import com.jim.hi_jim.shared.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseGameRepository {

    private val database = FirebaseDatabase.getInstance()
    private val gameRequestsRef = database.getReference("gameRequests")
    private val gamesRef = database.getReference("games")

    companion object {
        private const val TAG = "FirebaseGameRepo"
    }

    // 게임 요청 보내기
    suspend fun sendGameRequest(fromUserId: String, toUserId: String): Result<String> {
        return try {
            val requestId = gameRequestsRef.child(toUserId).push().key ?: return Result.failure(Exception("Failed to generate request ID"))

            val gameRequest = GameRequest(
                requestId = requestId,
                fromUserId = fromUserId,
                toUserId = toUserId,
                status = GameRequestStatus.PENDING,
                timestamp = System.currentTimeMillis()
            )

            // 받는 사람(toUserId) 경로에 저장
            gameRequestsRef
                .child(toUserId)
                .child(requestId)
                .setValue(gameRequest)
                .await()

            // 보낸 사람(fromUserId) 경로에도 저장 (상태 확인용)
            gameRequestsRef
                .child(fromUserId)
                .child(requestId)
                .setValue(gameRequest)
                .await()

            Log.d(TAG, "Game request sent: $requestId")
            Result.success(requestId)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending game request", e)
            Result.failure(e)
        }
    }

    // 받은 게임 요청 목록 실시간 감지
    fun observeGameRequests(userId: String): Flow<List<GameRequest>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = snapshot.children.mapNotNull { it.getValue(GameRequest::class.java) }
                    .filter {
                        // PENDING 상태이면서, 내가 받는 사람(toUserId)인 요청만 표시
                        it.status == GameRequestStatus.PENDING && it.toUserId == userId
                    }
                trySend(requests)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error observing game requests", error.toException())
                close(error.toException())
            }
        }

        gameRequestsRef.child(userId).addValueEventListener(listener)

        awaitClose {
            gameRequestsRef.child(userId).removeEventListener(listener)
        }
    }

    // 게임 요청 응답 (수락/거절)
    suspend fun respondToGameRequest(
        userId: String,
        requestId: String,
        accept: Boolean
    ): Result<String?> {
        return try {
            if (accept) {
                // 수락 시 게임 생성
                val requestSnapshot = gameRequestsRef.child(userId).child(requestId).get().await()
                val request = requestSnapshot.getValue(GameRequest::class.java)

                if (request != null) {
                    val gameId = createGame(request.fromUserId, request.toUserId)

                    // 상태와 게임 ID 업데이트
                    val updates = mapOf(
                        "status" to GameRequestStatus.ACCEPTED.name,
                        "gameId" to gameId
                    )

                    // 양쪽 경로 모두 업데이트
                    gameRequestsRef
                        .child(userId)
                        .child(requestId)
                        .updateChildren(updates)
                        .await()

                    gameRequestsRef
                        .child(request.fromUserId)
                        .child(requestId)
                        .updateChildren(updates)
                        .await()

                    Log.d(TAG, "Game accepted and created: $gameId")
                    Result.success(gameId)
                } else {
                    Result.failure(Exception("Request not found"))
                }
            } else {
                // 거절 또는 취소 시
                val requestSnapshot = gameRequestsRef.child(userId).child(requestId).get().await()
                val request = requestSnapshot.getValue(GameRequest::class.java)

                if (request != null) {
                    // 양쪽 경로에서 모두 삭제
                    gameRequestsRef
                        .child(userId)
                        .child(requestId)
                        .removeValue()
                        .await()

                    gameRequestsRef
                        .child(request.fromUserId)
                        .child(requestId)
                        .removeValue()
                        .await()

                    gameRequestsRef
                        .child(request.toUserId)
                        .child(requestId)
                        .removeValue()
                        .await()

                    Log.d(TAG, "Request rejected/cancelled and removed: $requestId")
                }

                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error responding to game request", e)
            Result.failure(e)
        }
    }

    // 보낸 요청의 상태 확인 (수락/거절 여부)
    fun observeRequestStatus(toUserId: String, requestId: String): Flow<GameRequestStatus?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val request = snapshot.getValue(GameRequest::class.java)
                trySend(request?.status)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error observing request status", error.toException())
                close(error.toException())
            }
        }

        gameRequestsRef.child(toUserId).child(requestId).addValueEventListener(listener)

        awaitClose {
            gameRequestsRef.child(toUserId).child(requestId).removeEventListener(listener)
        }
    }

    // 보낸 요청의 전체 정보 확인 (상태 + gameId 포함)
    fun observeSentRequest(fromUserId: String, requestId: String): Flow<GameRequest?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val request = snapshot.getValue(GameRequest::class.java)
                trySend(request)
                Log.d(TAG, "Request update: id=$requestId, status=${request?.status}, gameId=${request?.gameId}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error observing sent request", error.toException())
                close(error.toException())
            }
        }

        gameRequestsRef.child(fromUserId).child(requestId).addValueEventListener(listener)

        awaitClose {
            gameRequestsRef.child(fromUserId).child(requestId).removeEventListener(listener)
        }
    }

    // 게임 생성
    private suspend fun createGame(player1Id: String, player2Id: String): String {
        val gameId = gamesRef.push().key ?: throw Exception("Failed to generate game ID")

        val gameData = MultiplayerGameData(
            gameId = gameId,
            player1Id = player1Id,
            player2Id = player2Id
        )

        gamesRef.child(gameId).setValue(gameData).await()

        Log.d(TAG, "Game created: $gameId")
        return gameId
    }

    // 게임 상태 업데이트
    suspend fun updateGameState(gameData: MultiplayerGameData): Result<Unit> {
        return try {
            gamesRef.child(gameData.gameId).setValue(gameData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating game state", e)
            Result.failure(e)
        }
    }

    // 게임 상태 실시간 감지
    fun observeGameState(gameId: String): Flow<MultiplayerGameData?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gameData = snapshot.getValue(MultiplayerGameData::class.java)
                trySend(gameData)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error observing game state", error.toException())
                close(error.toException())
            }
        }

        gamesRef.child(gameId).addValueEventListener(listener)

        awaitClose {
            gamesRef.child(gameId).removeEventListener(listener)
        }
    }

    // 게임 종료
    suspend fun endGame(gameId: String): Result<Unit> {
        return try {
            gamesRef.child(gameId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error ending game", e)
            Result.failure(e)
        }
    }
}
