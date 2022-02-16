package com.chat_soon_e.re_chat.ui.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chat_soon_e.re_chat.ApplicationClass.Companion.retrofit
import com.chat_soon_e.re_chat.data.remote.chat.ChatList
import com.chat_soon_e.re_chat.data.remote.chat.ChatResponse
import com.chat_soon_e.re_chat.data.remote.chat.ChatRetrofitInterface
import com.chat_soon_e.re_chat.ui.view.GetChatListView
import com.chat_soon_e.re_chat.ui.view.GetChatView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatListRepository {
    private lateinit var chatListLiveData: MutableLiveData<List<ChatList>>
    private val tag = "REPO/CHAT-LIST"

    fun getChatList(getChatListView: GetChatListView, userIdx: Long): MutableLiveData<List<ChatList>> {
        val chatService = retrofit.create(ChatRetrofitInterface::class.java)
        val chatList = ArrayList<ChatList>()

        chatService.getChatList(userIdx).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                val resp = response.body()!!

                when (resp.code) {
                    1000 -> {
                        val jsonArray = resp.result

                        if (jsonArray != null) {
                            for (i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val chatIdx = jsonElement.asJsonObject.get("chatIdx").asInt
                                val nickname = jsonElement.asJsonObject.get("chat_name").asString
                                val groupName = if (jsonElement.asJsonObject.get("groupName").isJsonNull) null else jsonElement.asJsonObject.get("groupName").asString
                                val profileImg = if (jsonElement.asJsonObject.get("profile_image").isJsonNull) null else jsonElement.asJsonObject.get("profile_image").asString
                                val postTime = jsonElement.asJsonObject.get("latest_time").asString
                                val message = jsonElement.asJsonObject.get("latest_message").asString

                                val chat = ChatList(chatIdx, nickname, profileImg, postTime, message, groupName, 1)
                                chatList.add(chat)
                            }
                        }
                        chatListLiveData.postValue(chatList)
                        getChatListView.onGetChatListSuccess(chatList)  // 이거 필요한가?
                    }
                    else -> {
                        chatListLiveData.postValue(null)
                        getChatListView.onGetChatListFailure(resp.code, resp.message)
                    }
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                chatListLiveData.postValue(null)
                getChatListView.onGetChatListFailure(400, "네트워크 오류")
            }
        })

        return chatListLiveData
    }
}