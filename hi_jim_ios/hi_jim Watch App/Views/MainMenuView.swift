//
//  MainMenuView.swift
//  hi_jim Watch App
//
//  게임 선택 메인 메뉴
//

import SwiftUI

struct MainMenuView: View {
    @State private var selectedGame: GameType? = nil

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                Text("게임 선택")
                    .font(.headline)
                    .padding(.bottom, 8)

                // 스모 게임 버튼
                NavigationLink(destination: GameLobbyView()) {
                    Text("스모 게임")
                        .font(.body)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                }
                .buttonStyle(.bordered)
                .tint(.blue)

                // 묵찌빠 게임 버튼
                NavigationLink(destination: MukjjippaGameLobbyView()) {
                    Text("묵찌빠 게임")
                        .font(.body)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                }
                .buttonStyle(.bordered)
                .tint(.green)
            }
            .padding()
        }
    }
}

#Preview {
    MainMenuView()
}
