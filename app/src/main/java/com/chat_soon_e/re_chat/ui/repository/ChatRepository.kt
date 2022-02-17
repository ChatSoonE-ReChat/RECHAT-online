package com.chat_soon_e.re_chat.ui.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.chat_soon_e.re_chat.ApplicationClass
import com.chat_soon_e.re_chat.data.remote.chat.ChatList
import com.chat_soon_e.re_chat.data.remote.chat.ChatResponse
import com.chat_soon_e.re_chat.data.remote.chat.ChatRetrofitInterface
import com.chat_soon_e.re_chat.ui.view.GetChatView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatRepository{
    private lateinit var chatLiveData: MutableLiveData<List<ChatList>>
    private val tag = "REPO/CHAT"

    // 갠톡 or 단톡 채팅 가져오기
    fun getChat(getChatView: GetChatView, userIdx: Long, chatIdx: Int, groupName: String?) :MutableLiveData<List<ChatList>>{
        val chatList = ArrayList<ChatList>()
        val chatService = ApplicationClass.retrofit.create(ChatRetrofitInterface::class.java)

        // 응답 처리
        chatService.getChat(userIdx, chatIdx, groupName).enqueue(object: Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> {
                        // 응답 성공했을 때 response parameters parsing
                        val jsonArray = resp.result
                        if(jsonArray != null) {
                            // JsonArray Parsing
                            for(i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val chatIdx = jsonElement.asJsonObject.get("chatIdx").asInt
                                val nickname = jsonElement.asJsonObject.get("nickname").asString
                                val profileImgUrl = if(jsonElement.asJsonObject.get("profileImgUrl").isJsonNull) null else jsonElement.asJsonObject.get("profileImgUrl").asString
                                val message = jsonElement.asJsonObject.get("message").asString
                                val postTime = jsonElement.asJsonObject.get("post_time").asString
                                val groupName = if(jsonElement.asJsonObject.get("groupName").isJsonNull) null else jsonElement.asJsonObject.get("groupName").asString

                                val chat = ChatList(chatIdx, nickname, profileImgUrl, postTime, message, groupName, 1)
                                chatList.add(chat)
                                Log.d(tag, "getChat()/chat: $chat")
                                Log.d(tag, "getChat()/chatList: $chatList")
                            }
                            chatLiveData.postValue(chatList)
                            getChatView.onGetChatSuccess(chatList)
                            Log.d(tag, "getChat()/onResponse()/success")
                        }
                    }
                    else -> {
                        chatLiveData.postValue(null)
                        getChatView.onGetChatFailure(resp.code, resp.message)
                    }
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                chatLiveData.postValue(null)
                getChatView.onGetChatFailure(400, "네트워크 오류")
            }
        })
        return chatLiveData
    }
}