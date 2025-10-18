package com.jim.hi_jim.presentation.ui.sumo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import com.jim.hi_jim.presentation.constants.UserConstants
import com.jim.hi_jim.shared.model.GameRequest
import com.jim.hi_jim.shared.model.GameRequestStatus

@Composable
fun GameLobbyScreen(
    receivedRequests: List<GameRequest>,
    sentRequestStatus: GameRequestStatus?,
    onSendRequest: () -> Unit,
    onCancelRequest: () -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onBackToMenu: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 제목 및 뒤로가기 버튼
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "스모 게임 로비",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onBackToMenu,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF666666)
                    )
                ) {
                    Text(
                        text = "← 메인 메뉴",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 게임 요청 보내기 버튼
        item {
            val isRequestPending = sentRequestStatus == GameRequestStatus.PENDING

            Button(
                onClick = onSendRequest,
                enabled = !isRequestPending,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (isRequestPending) Color.Gray else Color(0xFF4CAF50),
                    disabledBackgroundColor = Color.Gray
                )
            ) {
                Text(
                    text = if (isRequestPending) "요청 중..." else "게임 요청하기",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 보낸 요청 상태
        item {
            if (sentRequestStatus != null) {
                val (statusText, statusColor) = when (sentRequestStatus) {
                    GameRequestStatus.PENDING -> "요청 대기 중..." to Color.Yellow
                    GameRequestStatus.ACCEPTED -> "요청 수락됨!" to Color.Green
                    GameRequestStatus.REJECTED -> "요청 거절됨" to Color.Red
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        color = statusColor,
                        textAlign = TextAlign.Center
                    )

                    // 대기 중일 때만 취소 버튼 표시
                    if (sentRequestStatus == GameRequestStatus.PENDING) {
                        Button(
                            onClick = onCancelRequest,
                            modifier = Modifier
                                .width(90.dp)
                                .height(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFFF9800)
                            )
                        ) {
                            Text(
                                text = "취소",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 받은 요청 제목
        if (receivedRequests.isNotEmpty()) {
            item {
                Text(
                    text = "받은 요청",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 받은 요청 목록
            items(receivedRequests) { request ->
                ReceivedRequestItem(
                    request = request,
                    onAccept = { onAcceptRequest(request.requestId) },
                    onReject = { onRejectRequest(request.requestId) }
                )
            }
        } else {
            item {
                Text(
                    text = "받은 요청이 없습니다",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
fun ReceivedRequestItem(
    request: GameRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 요청자 정보
        Text(
            text = if (request.fromUserId == UserConstants.USER_1) "Jim" else "Hi",
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 수락/거절 버튼
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 수락 버튼
            Button(
                onClick = onAccept,
                modifier = Modifier
                    .weight(1f)
                    .height(35.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "수락",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 거절 버튼
            Button(
                onClick = onReject,
                modifier = Modifier
                    .weight(1f)
                    .height(35.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFE85D75)
                )
            ) {
                Text(
                    text = "거절",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
