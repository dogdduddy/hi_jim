package com.jim.hi_jim.shared.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.jim.hi_jim.shared.engine.SumoPhysicsEngine
import com.jim.hi_jim.shared.model.GameStatus

@Composable
fun SumoGameCanvas(
    player1Position: Float,
    player2Position: Float,
    gameStatus: GameStatus,
    collisionPosition: Float? = null,
    collisionAlpha: Float = 0f
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerY = canvasHeight / 2

            // 게임 영역 범위
            val boundary = SumoPhysicsEngine.BOUNDARY
            val scale = canvasWidth / (boundary * 2)

            // 위치를 스크린 좌표로 변환
            fun toScreenX(position: Float): Float {
                return (position + boundary) * scale
            }

            // 경계선 그리기
            drawLine(
                color = Color.Red.copy(alpha = 0.5f),
                start = Offset(0f, centerY),
                end = Offset(toScreenX(-boundary), centerY),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.Red.copy(alpha = 0.5f),
                start = Offset(toScreenX(boundary), centerY),
                end = Offset(canvasWidth, centerY),
                strokeWidth = 4f
            )

            // 중앙선
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(canvasWidth / 2, centerY - 100f),
                end = Offset(canvasWidth / 2, centerY + 100f),
                strokeWidth = 2f
            )

            // 충돌 이펙트
            if (collisionPosition != null && collisionAlpha > 0f) {
                val collisionX = toScreenX(collisionPosition)
                drawCircle(
                    color = Color.Yellow.copy(alpha = collisionAlpha * 0.6f),
                    radius = 40f,
                    center = Offset(collisionX, centerY)
                )
                drawCircle(
                    color = Color.White.copy(alpha = collisionAlpha),
                    radius = 30f,
                    center = Offset(collisionX, centerY),
                    style = Stroke(width = 3f)
                )
            }

            // Player 1 (파란색)
            val p1X = toScreenX(player1Position)
            val player1Color = if (gameStatus == GameStatus.PLAYER1_WIN)
                Color(0xFF4CAF50) // 승리 시 초록색
            else
                Color(0xFF4A90E2)

            drawCircle(
                color = player1Color,
                radius = SumoPhysicsEngine.PLAYER_RADIUS * scale,
                center = Offset(p1X, centerY)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = SumoPhysicsEngine.PLAYER_RADIUS * scale,
                center = Offset(p1X, centerY),
                style = Stroke(width = 3f)
            )

            // Player 2 (빨간색)
            val p2X = toScreenX(player2Position)
            val player2Color = if (gameStatus == GameStatus.PLAYER2_WIN)
                Color(0xFF4CAF50) // 승리 시 초록색
            else
                Color(0xFFE85D75)

            drawCircle(
                color = player2Color,
                radius = SumoPhysicsEngine.PLAYER_RADIUS * scale,
                center = Offset(p2X, centerY)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = SumoPhysicsEngine.PLAYER_RADIUS * scale,
                center = Offset(p2X, centerY),
                style = Stroke(width = 3f)
            )
        }
    }
}
