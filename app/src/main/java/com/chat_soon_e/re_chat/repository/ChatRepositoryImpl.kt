package com.chat_soon_e.re_chat.repository

import com.chat_soon_e.re_chat.data.remote.chat.ChatList
import com.chat_soon_e.re_chat.data.remote.chat.ChatService

class ChatRepositoryImpl:ChatRepository {
    val chatService:ChatService=ChatService()
    // 채팅들 가져옴
    override suspend fun getChatList(
        userIdx: Long,
        chatIdx: Int,
        groupName: String?
    ): ArrayList<ChatList> {
        chatService.getChat(userIdx, chatIdx)
    }
}