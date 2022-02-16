package com.chat_soon_e.re_chat.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.chat_soon_e.re_chat.ApplicationClass
import com.chat_soon_e.re_chat.data.remote.folder.FolderList
import com.chat_soon_e.re_chat.data.remote.folder.FolderResponse
import com.chat_soon_e.re_chat.data.remote.folder.FolderRetrofitInterface
import com.chat_soon_e.re_chat.ui.view.FolderListView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FolderListRepository {
    private lateinit var folderListLiveData: MutableLiveData<List<FolderList>>
    private val tag = "REPO/FOLDER-LIST"

    fun getFolderList(folderListView: FolderListView, userIdx: Long): MutableLiveData<List<FolderList>> {
        val folderService = ApplicationClass.retrofit.create(FolderRetrofitInterface::class.java)
        val folderList = ArrayList<FolderList>()

        folderService.getFolderList(userIdx).enqueue(object : Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!
                Log.d(tag, "getFolderList()/onResponse()")

                when(resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if(jsonArray != null) {
                            // JsonArray parsing
                            for(i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val folderIdx = jsonElement.asJsonObject.get("folderIdx").asInt
                                val folderName = jsonElement.asJsonObject.get("folder_name").asString
                                val folderImg = if(jsonElement.asJsonObject.get("folderImg").isJsonNull) null else jsonElement.asJsonObject.get("folderImg").asString

                                Log.d(tag, "folderImg: ${folderImg.isNullOrEmpty()}")
                                Log.d(tag, "folderImg: $folderImg")

                                val folder = FolderList(folderIdx, folderName, folderImg)
                                folderList.add(folder)
                                Log.d(tag, "folderList: $folderList")
                            }
                        }
                        folderListLiveData.postValue(folderList)
                        folderListView.onFolderListSuccess(folderList)
                        Log.d(tag, "$folderList")
                    }
                    else -> {
                        folderListLiveData.postValue(null)
                        folderListView.onFolderListFailure(resp.code, resp.message)
                    }
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                folderListLiveData.postValue(null)
                folderListView.onFolderListFailure(400, "네트워크 오류")
            }
        })

        return folderListLiveData
    }
}