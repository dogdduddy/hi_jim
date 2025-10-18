package com.jim.hi_jim.presentation.ui.sumo

import com.jim.hi_jim.shared.engine.SumoPhysicsEngine
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.jim.hi_jim.shared.model.GameStatus
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SumoGameCanvas(
    player1Position: Float,
    player2Position: Float,
    gameStatus: GameStatus,
    collisionPosition: Float? = null,
    collisionAlpha: Float = 0f,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // 좌표계: -10 ~ +10을 화면에 맞춤
        val unitWidth = size.width / 24

        // 플레이어 반지름 (게임 로직과 동일하게)
        val playerRadiusPx = SumoPhysicsEngine.PLAYER_RADIUS * unitWidth

        // 1. 배경 - 경기장 (노란색 원)
        val ringRadius = size.width * 0.38f
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = ringRadius,
            center = Offset(centerX, centerY)
        )

        // 2. 경기장 테두리 (하얀색)
        drawCircle(
            color = Color.White,
            radius = ringRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 8f)
        )

        // 3. 중앙선 (얇은 하얀선)
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(centerX, centerY - ringRadius * 0.6f),
            end = Offset(centerX, centerY + ringRadius * 0.6f),
            strokeWidth = 2f
        )

        // 4. Player 1 (파란색)
        val p1X = centerX + player1Position * unitWidth
        val p1Y = centerY

        // 그림자
        drawCircle(
            color = Color.Black.copy(alpha = 0.15f),
            radius = playerRadiusPx,
            center = Offset(p1X + 3, p1Y + 3)
        )

        // 몸통
        drawCircle(
            color = Color(0xFF6BA3D8),
            radius = playerRadiusPx,
            center = Offset(p1X, p1Y)
        )

        // 테두리
        drawCircle(
            color = Color.Black,
            radius = playerRadiusPx,
            center = Offset(p1X, p1Y),
            style = Stroke(width = 5f)
        )

        // 5. Player 2 (빨간색)
        val p2X = centerX + player2Position * unitWidth
        val p2Y = centerY

        // 그림자
        drawCircle(
            color = Color.Black.copy(alpha = 0.15f),
            radius = playerRadiusPx,
            center = Offset(p2X + 3, p2Y + 3)
        )

        // 몸통
        drawCircle(
            color = Color(0xFFE87D8D),
            radius = playerRadiusPx,
            center = Offset(p2X, p2Y)
        )

        // 테두리
        drawCircle(
            color = Color.Black,
            radius = playerRadiusPx,
            center = Offset(p2X, p2Y),
            style = Stroke(width = 5f)
        )

        // 6. 충돌 스파크 이펙트 (방사형 삼각형)
        if (collisionPosition != null && collisionAlpha > 0f) {
            val sparkX = centerX + collisionPosition * unitWidth
            val sparkY = centerY

            // 방사형으로 퍼지는 삼각형들 (크기 1/3로 축소)
            val sparkCount = 10  // 10개의 스파크
            val baseLength = playerRadiusPx * 0.83f * (1f + (1f - collisionAlpha) * 0.3f)  // 2.5f / 3 ≈ 0.83f
            val startOffset = baseLength * 0.5f  // 이펙트 크기의 절반만큼 중심에서 떨어진 곳에서 시작

            for (i in 0 until sparkCount) {
                val angle = (i * 360f / sparkCount)
                val angleRad = angle * (Math.PI / 180f).toFloat()

                // 각 스파크의 길이를 랜덤하게 (불규칙)
                val lengthVariation = if (i % 2 == 0) 1.0f else 0.7f
                val sparkLength = baseLength * lengthVariation

                // 삼각형 Path 생성 (폭도 1/3로 축소)
                val path = Path().apply {
                    // 시작점을 startOffset만큼 떨어진 위치로 설정
                    moveTo(startOffset, 0f)

                    // 삼각형의 왼쪽 모서리 (폭 4도로 축소)
                    val leftAngle = -4f * (Math.PI / 180f).toFloat()
                    lineTo(
                        cos(leftAngle) * (sparkLength + startOffset),
                        sin(leftAngle) * (sparkLength + startOffset)
                    )

                    // 삼각형의 오른쪽 모서리 (폭 4도로 축소)
                    val rightAngle = 4f * (Math.PI / 180f).toFloat()
                    lineTo(
                        cos(rightAngle) * (sparkLength + startOffset),
                        sin(rightAngle) * (sparkLength + startOffset)
                    )

                    close()
                }

                // 회전 및 그리기
                rotate(angle, pivot = Offset(sparkX, sparkY)) {
                    translate(sparkX, sparkY) {
                        drawPath(
                            path = path,
                            color = Color(0xFFE0E0E0).copy(alpha = collisionAlpha * 0.9f)
                        )
                    }
                }
            }
        }
    }
}