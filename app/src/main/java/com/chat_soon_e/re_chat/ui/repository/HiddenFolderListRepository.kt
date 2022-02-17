package com.chat_soon_e.re_chat.ui.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.chat_soon_e.re_chat.ApplicationClass
import com.chat_soon_e.re_chat.data.remote.folder.FolderList
import com.chat_soon_e.re_chat.data.remote.folder.FolderResponse
import com.chat_soon_e.re_chat.data.remote.folder.FolderRetrofitInterface
import com.chat_soon_e.re_chat.ui.view.HiddenFolderListView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HiddenFolderListRepository {
    private lateinit var hiddenFolderListLiveData: MutableLiveData<List<FolderList>>
    private val tag = "REPO/HIDDEN-FOLDER-LIST"

    fun getHiddenFolderList(hiddenFolderListView: HiddenFolderListView, userIdx: Long): MutableLiveData<List<FolderList>> {
        val folderService = ApplicationClass.retrofit.create(FolderRetrofitInterface::class.java)
        val hiddenFolderList = ArrayList<FolderList>()

        folderService.getHiddenFolderList(userIdx).enqueue(object: Callback<FolderResponse> {
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
                                val folderName = jsonElement.asJsonObject.get("folderName").asString
                                val folderImg = if(jsonElement.asJsonObject.get("folderImg").isJsonNull) null else jsonElement.asJsonObject.get("folderImg").asString

                                Log.d(tag, "folderImg: ${folderImg.isNullOrEmpty()}")
                                Log.d(tag, "folderImg: $folderImg")

                                val hiddenFolder = FolderList(folderIdx, folderName, folderImg)
                                hiddenFolderList.add(hiddenFolder)
                                Log.d(tag, "hiddenFolderList: $hiddenFolderList")
                            }
                        }
                        hiddenFolderListLiveData.postValue(hiddenFolderList)
                        hiddenFolderListView.onHiddenFolderListSuccess(hiddenFolderList)
                        Log.d(tag, "$hiddenFolderList")
                    }
                    else -> hiddenFolderListView.onHiddenFolderListFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                hiddenFolderListView.onHiddenFolderListFailure(400, "네트워크 오류")
            }
        })

        return hiddenFolderListLiveData
    }
}