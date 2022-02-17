package com.chat_soon_e.re_chat.ui.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.chat_soon_e.re_chat.ApplicationClass
import com.chat_soon_e.re_chat.data.remote.chat.ChatResponse
import com.chat_soon_e.re_chat.data.remote.chat.ChatRetrofitInterface
import com.chat_soon_e.re_chat.data.remote.chat.FolderContent
import com.chat_soon_e.re_chat.data.remote.folder.FolderList
import com.chat_soon_e.re_chat.ui.view.GetFolderContentView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FolderContentRepository {
    private lateinit var folderContentLiveData: MutableLiveData<List<FolderContent>>
    private val tag = "REPO/FOLDER-CONTENT"

    fun getFolderContent(getFolderContentView: GetFolderContentView, userIdx: Long, folderIdx: Int): MutableLiveData<List<FolderContent>>{
        val folderContentList = ArrayList<FolderContent>()
        val chatService = ApplicationClass.retrofit.create(ChatRetrofitInterface::class.java)

        chatService.getFolderContent(userIdx, folderIdx).enqueue(object: Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if(jsonArray != null) {
                            // JsonArray parsing
                            for(i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val chatIdx = jsonElement.asJsonObject.get("chatIdx").asInt
                                val folderName = jsonElement.asJsonObject.get("folderName").asString
                                val nickname = jsonElement.asJsonObject.get("nickname").asString
                                val profileImg = if(jsonElement.asJsonObject.get("profileImgUrl").isJsonNull) null else jsonElement.asJsonObject.get("profileImgUrl").asString
                                val message = jsonElement.asJsonObject.get("message").asString
                                val chatDate = if(jsonElement.asJsonObject.get("chat_date").isJsonNull) null else jsonElement.asJsonObject.get("chat_date").asString
                                val postTime = jsonElement.asJsonObject.get("post_time").asString

                                Log.d(tag, "postTime: ${postTime.isNullOrEmpty()}")
                                Log.d(tag, "postTime: $postTime")

                                val folderContent = FolderContent(chatIdx, folderName, nickname, profileImg, message, null, postTime)
                                Log.d(tag, "folderContent: $folderContent")
                                folderContentList.add(folderContent)
                            }
                        }
                        folderContentLiveData.postValue(folderContentList)
                        getFolderContentView.onGetFolderContentSuccess(folderContentList)
                    }
                    else -> {
                        folderContentLiveData.postValue(null)
                        getFolderContentView.onGetFolderContentFailure(resp.code, resp.message)
                    }
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                folderContentLiveData.postValue(null)
                getFolderContentView.onGetFolderContentFailure(400, "네트워크 오류")
            }
        })
        return folderContentLiveData
    }
}