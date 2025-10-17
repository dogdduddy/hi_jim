//
//  FirebaseGameRepository.swift
//  hi_jim Watch App
//
//  Firebase Realtime Database를 통한 게임 데이터 관리
//

import Foundation
import FirebaseDatabase
import Combine

class FirebaseGameRepository {
    private let database = Database.database().reference()
    private let gameRequestsRef: DatabaseReference
    private let gamesRef: DatabaseReference
    private let physicsEngine = SumoPhysicsEngine()

    init() {
        gameRequestsRef = database.child("gameRequests")
        gamesRef = database.child("games")
    }

    // MARK: - Game Requests

    // 게임 요청 보내기
    func sendGameRequest(fromUserId: String, toUserId: String) async throws -> String {
        let requestId = gameRequestsRef.child(toUserId).childByAutoId().key ?? UUID().uuidString

        let gameRequest = GameRequest(
            requestId: requestId,
            fromUserId: fromUserId,
            toUserId: toUserId,
            status: .pending,
            timestamp: Int64(Date().timeIntervalSince1970 * 1000)
        )

        // Codable을 Dictionary로 변환
        let encoder = JSONEncoder()
        let data = try encoder.encode(gameRequest)
        let dictionary = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]

        // 받는 사람 경로에 저장
        try await gameRequestsRef
            .child(toUserId)
            .child(requestId)
            .setValue(dictionary)

        // 보낸 사람 경로에도 저장 (상태 확인용)
        try await gameRequestsRef
            .child(fromUserId)
            .child(requestId)
            .setValue(dictionary)

        print("Game request sent: \(requestId)")
        return requestId
    }

    // 받은 게임 요청 목록 실시간 감지
    func observeGameRequests(userId: String) -> AnyPublisher<[GameRequest], Error> {
        let subject = PassthroughSubject<[GameRequest], Error>()

        let handle = gameRequestsRef.child(userId).observe(.value) { snapshot in
            var requests: [GameRequest] = []

            print("🔵 observeGameRequests: checking requests for userId=\(userId)")
            print("🔵 Snapshot children count: \(snapshot.childrenCount)")

            for child in snapshot.children {
                if let snap = child as? DataSnapshot,
                   let dict = snap.value as? [String: Any] {

                    print("🔵 Found request snapshot: \(snap.key)")
                    print("🔵 Request dict: \(dict)")

                    // 직접 파싱
                    guard let requestId = dict["requestId"] as? String,
                          let fromUserId = dict["fromUserId"] as? String,
                          let toUserId = dict["toUserId"] as? String,
                          let statusString = dict["status"] as? String,
                          let timestamp = dict["timestamp"] as? Int64 else {
                        print("❌ Failed to parse request fields")
                        continue
                    }

                    // status 문자열을 enum으로 변환
                    guard let status = GameRequestStatus(rawValue: statusString) else {
                        print("❌ Invalid status: \(statusString)")
                        continue
                    }

                    // PENDING 상태이고 내가 받는 사람인 요청만
                    if status == .pending && toUserId == userId {
                        let gameId = dict["gameId"] as? String

                        let request = GameRequest(
                            requestId: requestId,
                            fromUserId: fromUserId,
                            toUserId: toUserId,
                            status: status,
                            timestamp: timestamp,
                            gameId: gameId
                        )

                        requests.append(request)
                        print("✅ Added request: \(requestId) from \(fromUserId)")
                    } else {
                        print("🔵 Skipping request: status=\(status), toUserId=\(toUserId)")
                    }
                }
            }

            print("🔵 Total requests found: \(requests.count)")
            subject.send(requests)
        } withCancel: { error in
            subject.send(completion: .failure(error))
        }

        return subject
            .handleEvents(receiveCancel: {
                self.gameRequestsRef.child(userId).removeObserver(withHandle: handle)
            })
            .eraseToAnyPublisher()
    }

    // 보낸 요청의 상태 확인
    func observeSentRequest(fromUserId: String, requestId: String) -> AnyPublisher<GameRequest?, Error> {
        let subject = PassthroughSubject<GameRequest?, Error>()

        let handle = gameRequestsRef.child(fromUserId).child(requestId).observe(.value) { snapshot in
            if let dict = snapshot.value as? [String: Any],
               let requestIdVal = dict["requestId"] as? String,
               let fromUserIdVal = dict["fromUserId"] as? String,
               let toUserIdVal = dict["toUserId"] as? String,
               let statusString = dict["status"] as? String,
               let timestamp = dict["timestamp"] as? Int64,
               let status = GameRequestStatus(rawValue: statusString) {

                let gameId = dict["gameId"] as? String

                let request = GameRequest(
                    requestId: requestIdVal,
                    fromUserId: fromUserIdVal,
                    toUserId: toUserIdVal,
                    status: status,
                    timestamp: timestamp,
                    gameId: gameId
                )

                print("Request update: id=\(requestId), status=\(request.status), gameId=\(request.gameId ?? "nil")")
                subject.send(request)
            } else {
                subject.send(nil)
            }
        } withCancel: { error in
            subject.send(completion: .failure(error))
        }

        return subject
            .handleEvents(receiveCancel: {
                self.gameRequestsRef.child(fromUserId).child(requestId).removeObserver(withHandle: handle)
            })
            .eraseToAnyPublisher()
    }

    // 게임 요청 응답 (수락/거절)
    func respondToGameRequest(userId: String, requestId: String, accept: Bool) async throws -> String? {
        print("🔵 respondToGameRequest called: userId=\(userId), requestId=\(requestId), accept=\(accept)")

        if accept {
            // 수락 시 게임 생성
            print("🔵 Reading request from path: /gameRequests/\(userId)/\(requestId)")
            let snapshot = try await gameRequestsRef.child(userId).child(requestId).getData()

            print("🔵 Snapshot exists: \(snapshot.exists())")
            print("🔵 Snapshot value: \(String(describing: snapshot.value))")

            // snapshot.value가 중첩된 구조일 수 있음
            // { requestId: { fromUserId: ..., toUserId: ... } } 형태
            var dict: [String: Any]?

            if let outerDict = snapshot.value as? [String: Any] {
                // 중첩된 경우: requestId를 키로 하는 딕셔너리
                if let firstValue = outerDict.values.first as? [String: Any] {
                    dict = firstValue
                    print("🔵 Using nested dictionary")
                } else {
                    dict = outerDict
                    print("🔵 Using flat dictionary")
                }
            }

            guard let requestDict = dict else {
                print("❌ Snapshot value is not a dictionary")
                throw NSError(domain: "GameRepository", code: -1, userInfo: [NSLocalizedDescriptionKey: "Request not found"])
            }

            print("🔵 Dictionary keys: \(requestDict.keys)")
            print("🔵 Dictionary: \(requestDict)")

            // 직접 파싱 (더 안전)
            guard let fromUserId = requestDict["fromUserId"] as? String,
                  let toUserId = requestDict["toUserId"] as? String else {
                print("❌ Failed to extract fromUserId or toUserId from dict")
                throw NSError(domain: "GameRepository", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid request data"])
            }

            print("🔵 Request decoded: from=\(fromUserId), to=\(toUserId)")

            let gameId = try await createGame(player1Id: fromUserId, player2Id: toUserId)
            print("🔵 Game created with ID: \(gameId)")

            // 상태와 게임 ID 업데이트
            let updates: [String: Any] = [
                "status": GameRequestStatus.accepted.rawValue,
                "gameId": gameId
            ]

            // 양쪽 경로 모두 업데이트
            print("🔵 Updating /gameRequests/\(userId)/\(requestId)")
            try await gameRequestsRef
                .child(userId)
                .child(requestId)
                .updateChildValues(updates)

            print("🔵 Updating /gameRequests/\(fromUserId)/\(requestId)")
            try await gameRequestsRef
                .child(fromUserId)
                .child(requestId)
                .updateChildValues(updates)

            print("✅ Game accepted and created: \(gameId)")
            return gameId

        } else {
            // 거절 또는 취소 시
            print("🔵 Rejecting/canceling request from path: /gameRequests/\(userId)/\(requestId)")
            let snapshot = try await gameRequestsRef.child(userId).child(requestId).getData()

            print("🔵 Snapshot exists: \(snapshot.exists())")

            // 중첩된 구조 처리
            var dict: [String: Any]?
            if let outerDict = snapshot.value as? [String: Any] {
                if let firstValue = outerDict.values.first as? [String: Any] {
                    dict = firstValue
                } else {
                    dict = outerDict
                }
            }

            if let requestDict = dict,
               let fromUserId = requestDict["fromUserId"] as? String,
               let toUserId = requestDict["toUserId"] as? String {

                print("🔵 Request decoded: from=\(fromUserId), to=\(toUserId)")

                // 양쪽 경로에서 모두 삭제
                print("🔵 Deleting /gameRequests/\(userId)/\(requestId)")
                try await gameRequestsRef
                    .child(userId)
                    .child(requestId)
                    .removeValue()

                print("🔵 Deleting /gameRequests/\(fromUserId)/\(requestId)")
                try await gameRequestsRef
                    .child(fromUserId)
                    .child(requestId)
                    .removeValue()

                print("🔵 Deleting /gameRequests/\(toUserId)/\(requestId)")
                try await gameRequestsRef
                    .child(toUserId)
                    .child(requestId)
                    .removeValue()

                print("✅ Request rejected/cancelled and removed: \(requestId)")
            } else {
                print("❌ Failed to decode request for rejection")
            }

            return nil
        }
    }

    // MARK: - Games

    // 게임 생성
    private func createGame(player1Id: String, player2Id: String) async throws -> String {
        let gameId = gamesRef.childByAutoId().key ?? UUID().uuidString

        let gameData = MultiplayerGameData(
            gameId: gameId,
            player1Id: player1Id,
            player2Id: player2Id
        )

        let encoder = JSONEncoder()
        let data = try encoder.encode(gameData)
        let dictionary = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]

        try await gamesRef.child(gameId).setValue(dictionary)

        print("Game created: \(gameId)")
        return gameId
    }

    // 게임 상태 실시간 감지
    func observeGameState(gameId: String) -> AnyPublisher<MultiplayerGameData?, Error> {
        let subject = PassthroughSubject<MultiplayerGameData?, Error>()

        let handle = gamesRef.child(gameId).observe(.value) { snapshot in
            if let dict = snapshot.value as? [String: Any],
               let jsonData = try? JSONSerialization.data(withJSONObject: dict),
               let gameData = try? JSONDecoder().decode(MultiplayerGameData.self, from: jsonData) {
                subject.send(gameData)
            } else {
                subject.send(nil)
            }
        } withCancel: { error in
            subject.send(completion: .failure(error))
        }

        return subject
            .handleEvents(receiveCancel: {
                self.gamesRef.child(gameId).removeObserver(withHandle: handle)
            })
            .eraseToAnyPublisher()
    }

    // 플레이어 이동 전송
    func sendPlayerMove(gameId: String, playerId: String) async throws {
        // 현재 게임 상태 가져오기
        let snapshot = try await gamesRef.child(gameId).getData()

        guard let dict = snapshot.value as? [String: Any],
              let jsonData = try? JSONSerialization.data(withJSONObject: dict),
              var gameData = try? JSONDecoder().decode(MultiplayerGameData.self, from: jsonData) else {
            throw NSError(domain: "GameRepository", code: -1, userInfo: [NSLocalizedDescriptionKey: "Game not found"])
        }

        // 물리 엔진으로 새 상태 계산
        let currentState = gameData.toSumoGameState()
        let timestamp = Int64(Date().timeIntervalSince1970 * 1000)
        let newState = physicsEngine.processMove(
            currentState: currentState,
            playerId: playerId,
            timestamp: timestamp
        )

        // MultiplayerGameData로 변환
        gameData = MultiplayerGameData.fromSumoGameState(
            gameId: gameId,
            player1Id: gameData.player1Id,
            player2Id: gameData.player2Id,
            state: newState,
            lastMovePlayerId: playerId
        )

        // Firebase에 저장
        let encoder = JSONEncoder()
        let newData = try encoder.encode(gameData)
        let newDictionary = try JSONSerialization.jsonObject(with: newData) as? [String: Any] ?? [:]

        try await gamesRef.child(gameId).setValue(newDictionary)
    }

    // 라운드 리셋
    func resetRound(gameId: String) async throws {
        let snapshot = try await gamesRef.child(gameId).getData()

        guard let dict = snapshot.value as? [String: Any],
              let jsonData = try? JSONSerialization.data(withJSONObject: dict),
              var gameData = try? JSONDecoder().decode(MultiplayerGameData.self, from: jsonData) else {
            throw NSError(domain: "GameRepository", code: -1, userInfo: [NSLocalizedDescriptionKey: "Game not found"])
        }

        // 라운드 리셋 (스코어 유지)
        let newState = physicsEngine.resetRound(
            currentScore1: gameData.player1Score,
            currentScore2: gameData.player2Score
        )

        gameData = MultiplayerGameData.fromSumoGameState(
            gameId: gameId,
            player1Id: gameData.player1Id,
            player2Id: gameData.player2Id,
            state: newState,
            lastMovePlayerId: ""
        )

        let encoder = JSONEncoder()
        let data = try encoder.encode(gameData)
        let dictionary = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]

        try await gamesRef.child(gameId).setValue(dictionary)
    }

    // 게임 종료
    func endGame(gameId: String) async throws {
        try await gamesRef.child(gameId).removeValue()
        print("Game ended: \(gameId)")
    }
}
