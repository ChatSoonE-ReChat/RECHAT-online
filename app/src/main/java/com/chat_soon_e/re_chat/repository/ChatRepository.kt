package com.chat_soon_e.re_chat.repository

import com.chat_soon_e.re_chat.data.remote.chat.ChatList

interface ChatRepository {
    suspend fun getChatList(userIdx: Long, chatIdx: Int, groupName: String?):ArrayList<ChatList>
}