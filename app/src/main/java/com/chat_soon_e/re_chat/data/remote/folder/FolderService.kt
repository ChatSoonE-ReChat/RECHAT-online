package com.chat_soon_e.re_chat.data.remote.folder

import android.util.Log
import retrofit2.Callback
import com.chat_soon_e.re_chat.ApplicationClass.Companion.retrofit
import com.chat_soon_e.re_chat.ui.view.FolderAPIView
import com.chat_soon_e.re_chat.ui.view.FolderListView
import com.chat_soon_e.re_chat.ui.view.HiddenFolderListView
import com.chat_soon_e.re_chat.ui.view.*
import retrofit2.Call
import retrofit2.Response

class FolderService {
    private val tag = "SERVICE/FOLDER"

    // 전체 폴더목록 가져오기 (숨김폴더 제외)
    fun getFolderList(folderListView: FolderListView, userIdx: Long) {
        val folderList = ArrayList<FolderList>()
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.getFolderList(userIdx).enqueue(object: Callback<FolderResponse> {
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

                                val folder = FolderList(folderIdx, folderName, folderImg)
                                folderList.add(folder)
                                Log.d(tag, "folderList: $folderList")
                            }
                        }
                        folderListView.onFolderListSuccess(folderList)
                        Log.d(tag, "$folderList")
                    }
                    else -> folderListView.onFolderListFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                folderListView.onFolderListFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 생성하기
    fun createFolder(folderView: FolderAPIView, userIdx: Long) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.createFolder(userIdx).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> folderView.onFolderAPISuccess()
                    else -> folderView.onFolderAPIFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                folderView.onFolderAPIFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 이름 바꾸기
    fun changeFolderName(folderView: FolderAPIView, userIdx: Long, folderIdx: Int, folder: FolderList) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.changeFolderName(userIdx, folderIdx, folder).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                Log.d(tag, "changeFolderName()/onResponse()/userIdx: $userIdx, folderIdx: $folderIdx, folder: $folder")
                Log.d(tag, "changeFolderName()/onResponse()/response.body(): ${response.body()}")
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> folderView.onFolderAPISuccess()
                    else -> folderView.onFolderAPIFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                folderView.onFolderAPIFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 아이콘 바꾸기
    fun changeFolderIcon(folderView: FolderAPIView, userIdx: Long, folderIdx: Int, folder: FolderList) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.changeFolerIcon(userIdx, folderIdx, folder).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> folderView.onFolderAPISuccess()
                    else -> folderView.onFolderAPIFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                folderView.onFolderAPIFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 삭제하기
    fun deleteFolder(folderView: FolderAPIView, userIdx: Long, folderIdx: Int) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.deleteFolder(userIdx, folderIdx).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> folderView.onFolderAPISuccess()
                    else -> folderView.onFolderAPIFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                folderView.onFolderAPIFailure(400, "네트워크 오류")
            }
        })
    }

    // 숨김 폴더목록 가져오기
    fun getHiddenFolderList(hiddenFolderListView: HiddenFolderListView, userIdx: Long) {
        val hiddenFolderList = ArrayList<FolderList>()
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

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
    }

    // 폴더 숨기기
    fun hideFolder(folderView: FolderAPIView, userIdx: Long, folderIdx: Int) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.hideFolder(userIdx, folderIdx).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> folderView.onFolderAPISuccess()
                    else -> folderView.onFolderAPIFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                folderView.onFolderAPIFailure(400, "네트워크 오류")
            }
        })
    }

    // 숨김 폴더 다시 해제하기
    fun unhideFolder(folderView: FolderAPIView, userIdx: Long, folderIdx: Int) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.unhideFolder(userIdx, folderIdx).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> folderView.onFolderAPISuccess()
                    else -> folderView.onFolderAPIFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                folderView.onFolderAPIFailure(400, "네트워크 오류")
            }
        })
    }
}