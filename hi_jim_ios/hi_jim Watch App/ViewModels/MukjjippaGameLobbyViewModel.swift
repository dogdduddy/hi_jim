//
//  MukjjippaGameLobbyViewModel.swift
//  hi_jim Watch App
//
//  묵찌빠 게임 로비 상태 관리
//

import Foundation
import Combine

@MainActor
class MukjjippaGameLobbyViewModel: ObservableObject {
    @Published var receivedRequests: [GameRequest] = []
    @Published var sentRequestStatus: GameRequestStatus?
    @Published var currentGameId: String?
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let repository = FirebaseGameRepository()
    private var cancellables = Set<AnyCancellable>()
    private var currentRequestId: String?

    let currentUserId = UserConstants.CURRENT_USER_ID
    let opponentUserId = UserConstants.opponentUserId

    // 초기화 시 받은 요청 감지 시작
    init() {
        observeReceivedRequests()
    }

    // MARK: - Observe Requests

    // 받은 게임 요청 감지 (묵찌빠만)
    private func observeReceivedRequests() {
        repository.observeGameRequestsByType(userId: currentUserId, gameType: .MUKJJIPPA)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { [weak self] completion in
                    if case .failure(let error) = completion {
                        self?.errorMessage = "요청 감지 오류: \(error.localizedDescription)"
                    }
                },
                receiveValue: { [weak self] requests in
                    self?.receivedRequests = requests
                }
            )
            .store(in: &cancellables)
    }

    // 보낸 요청 상태 감지
    private func observeSentRequestStatus(requestId: String) {
        repository.observeSentRequest(fromUserId: currentUserId, requestId: requestId)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { [weak self] completion in
                    if case .failure(let error) = completion {
                        self?.errorMessage = "요청 상태 감지 오류: \(error.localizedDescription)"
                    }
                },
                receiveValue: { [weak self] request in
                    guard let self = self else { return }

                    if let request = request {
                        print("🟡 [MukjjippaLobby] Sent request update: status=\(request.status), gameId=\(request.gameId ?? "nil")")
                        self.sentRequestStatus = request.status

                        // 수락되면 게임 ID 저장하고 게임 화면으로 이동
                        if request.status == .accepted {
                            if let gameId = request.gameId {
                                print("✅ [MukjjippaLobby] Game started from request sender: \(gameId)")
                                self.currentGameId = gameId
                                self.sentRequestStatus = nil
                                self.currentRequestId = nil
                            } else {
                                print("🔴 [MukjjippaLobby] Request accepted but gameId is nil!")
                            }
                        }
                    } else {
                        print("🟡 [MukjjippaLobby] Request deleted (rejected/cancelled)")
                        self.sentRequestStatus = nil
                        self.currentRequestId = nil
                    }
                }
            )
            .store(in: &cancellables)
    }

    // MARK: - Actions

    // 게임 요청 보내기
    func sendGameRequest() {
        print("🟡 [MukjjippaLobby] Sending game request from \(currentUserId) to \(opponentUserId)")
        isLoading = true
        errorMessage = nil

        Task {
            do {
                let requestId = try await repository.sendGameRequest(
                    fromUserId: currentUserId,
                    toUserId: opponentUserId,
                    gameType: .MUKJJIPPA
                )

                print("✅ [MukjjippaLobby] Request sent with ID: \(requestId)")
                currentRequestId = requestId
                sentRequestStatus = .pending
                observeSentRequestStatus(requestId: requestId)
                isLoading = false

            } catch {
                print("🔴 [MukjjippaLobby] Send request failed: \(error.localizedDescription)")
                errorMessage = "요청 전송 실패: \(error.localizedDescription)"
                isLoading = false
            }
        }
    }

    // 보낸 요청 취소
    func cancelSentRequest() {
        guard let requestId = currentRequestId else { return }

        isLoading = true

        Task {
            do {
                _ = try await repository.respondToGameRequest(
                    userId: currentUserId,
                    requestId: requestId,
                    accept: false
                )

                sentRequestStatus = nil
                currentRequestId = nil
                isLoading = false

            } catch {
                errorMessage = "요청 취소 실패: \(error.localizedDescription)"
                isLoading = false
            }
        }
    }

    // 받은 요청 수락
    func acceptRequest(_ request: GameRequest) {
        print("🟡 [MukjjippaLobby] Accepting request: \(request.requestId)")
        isLoading = true
        errorMessage = nil

        Task {
            do {
                if let gameId = try await repository.respondToGameRequest(
                    userId: currentUserId,
                    requestId: request.requestId,
                    accept: true
                ) {
                    print("✅ [MukjjippaLobby] Game created with ID: \(gameId)")
                    currentGameId = gameId
                } else {
                    print("🔴 [MukjjippaLobby] respondToGameRequest returned nil gameId")
                }
                isLoading = false

            } catch {
                print("🔴 [MukjjippaLobby] Accept request failed: \(error.localizedDescription)")
                errorMessage = "요청 수락 실패: \(error.localizedDescription)"
                isLoading = false
            }
        }
    }

    // 받은 요청 거절
    func rejectRequest(_ request: GameRequest) {
        Task {
            do {
                _ = try await repository.respondToGameRequest(
                    userId: currentUserId,
                    requestId: request.requestId,
                    accept: false
                )
            } catch {
                errorMessage = "요청 거절 실패: \(error.localizedDescription)"
            }
        }
    }

    // 게임에서 돌아왔을 때 게임 ID 초기화
    func resetGame() {
        currentGameId = nil
    }
}
