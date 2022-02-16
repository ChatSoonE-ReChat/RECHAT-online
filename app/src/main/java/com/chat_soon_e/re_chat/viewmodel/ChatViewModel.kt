package com.chat_soon_e.re_chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat_soon_e.re_chat.data.remote.chat.ChatList
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


//chat에 관련된 모든 liveData를 관리하는 곳
class ChatViewModel:ViewModel() {
    private val _chatList=MutableLiveData<ArrayList<ChatList>>()

    // 외부 접근 가능 데이터
    val chatList:LiveData<ArrayList<ChatList>>
        get()=_chatList

    // 채팅 가져오기, 서버로부터 가져옴
    fun getChatList(){
        viewModelScope.launch {
            // repository(서버 API)를 통해 데이터 얻어옴
            val chatListLiveData=Reposi.get
            withContext(Main){
                _chatList.value=chatListLiveData
            }
        }
    }



}