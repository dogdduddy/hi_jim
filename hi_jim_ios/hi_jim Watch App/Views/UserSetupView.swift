//
//  UserSetupView.swift
//  hi_jim Watch App
//
//  사용자 선택 화면 (최초 실행 시 또는 설정에서)
//

import SwiftUI

struct UserSetupView: View {
    @State private var selectedUserId: String = UserConstants.CURRENT_USER_ID
    @Binding var isPresented: Bool

    var body: some View {
        VStack(spacing: 20) {
            Text("사용자 선택")
                .font(.headline)

            VStack(spacing: 12) {
                Button {
                    selectUser(UserConstants.USER_1)
                } label: {
                    HStack {
                        Image(systemName: selectedUserId == UserConstants.USER_1 ? "checkmark.circle.fill" : "circle")
                        Text(UserConstants.USER_1)
                        Spacer()
                    }
                    .padding()
                    .background(selectedUserId == UserConstants.USER_1 ? Color.blue.opacity(0.3) : Color.gray.opacity(0.2))
                    .cornerRadius(10)
                }
                .buttonStyle(.plain)

                Button {
                    selectUser(UserConstants.USER_2)
                } label: {
                    HStack {
                        Image(systemName: selectedUserId == UserConstants.USER_2 ? "checkmark.circle.fill" : "circle")
                        Text(UserConstants.USER_2)
                        Spacer()
                    }
                    .padding()
                    .background(selectedUserId == UserConstants.USER_2 ? Color.blue.opacity(0.3) : Color.gray.opacity(0.2))
                    .cornerRadius(10)
                }
                .buttonStyle(.plain)
            }

            Button("확인") {
                UserConstants.CURRENT_USER_ID = selectedUserId
                isPresented = false
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }

    private func selectUser(_ userId: String) {
        selectedUserId = userId
    }
}

#Preview {
    UserSetupView(isPresented: .constant(true))
}
