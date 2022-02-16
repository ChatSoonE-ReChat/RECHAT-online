package com.chat_soon_e.re_chat.ui.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.chat_soon_e.re_chat.ApplicationClass
import com.chat_soon_e.re_chat.data.remote.chat.BlockedChatList
import com.chat_soon_e.re_chat.data.remote.chat.ChatResponse
import com.chat_soon_e.re_chat.data.remote.chat.ChatRetrofitInterface
import com.chat_soon_e.re_chat.ui.view.GetBlockedChatListView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BlockedChatListRepository {

    private lateinit var blockedChatListLiveData: MutableLiveData<List<BlockedChatList>>
    private val tag = "REPO/BLOCKED-LIST"

    // 차단된 톡방 목록 가져오기
    fun getBlockedChatList(getBlockedChatListView: GetBlockedChatListView, userIdx: Long):MutableLiveData<List<BlockedChatList>> {
        val blockedChatList = ArrayList<BlockedChatList>()
        val chatService = ApplicationClass.retrofit.create(ChatRetrofitInterface::class.java)

        chatService.getBlockedChatList(userIdx).enqueue(object: Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                val resp = response.body()!!
                Log.d(tag, "getBlockedChatList()/onResponse()")

                when(resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if(jsonArray != null) {
                            // JsonArray parsing
                            for(i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val blockedName = jsonElement.asJsonObject.get("blocked_name").asString
                                val blockedProfileImg = jsonElement.asJsonObject.get("blocked_profileImg").asString
                                val groupName = if(jsonElement.asJsonObject.get("groupName").isJsonNull) null else jsonElement.asJsonObject.get("groupName").asString
                                val status = jsonElement.asJsonObject.get("status").asString

                                Log.d(tag, "postTime: ${groupName.isNullOrEmpty()}")
                                Log.d(tag, "postTime: $groupName")

                                val blockedChat = BlockedChatList(blockedName, blockedProfileImg, groupName, status)
                                Log.d(tag, "blockedChatList: $blockedChatList")
                                blockedChatList.add(blockedChat)
                            }
                        }
                        blockedChatListLiveData.postValue(blockedChatList)
                        getBlockedChatListView.onGetBlockedChatListSuccess(blockedChatList)
                    }
                    else -> {
                        blockedChatListLiveData.postValue(null)
                        getBlockedChatListView.onGetBlockedChatListFailure(resp.code, resp.message)
                    }
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                blockedChatListLiveData.postValue(null)
                getBlockedChatListView.onGetBlockedChatListFailure(400, "네트워크 오류")
            }
        })
        return blockedChatListLiveData
    }
}