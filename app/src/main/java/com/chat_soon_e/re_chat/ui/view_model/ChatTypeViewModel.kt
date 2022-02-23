package com.chat_soon_e.re_chat.ui.view_model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//인터넷 연결 안되면 안바뀌는 부분인가!
class ChatTypeViewModel: ViewModel() {
    private var _mode = MutableLiveData<Int>()
    val mode get() = _mode  // 0: 일반, 1: 선택

    init {
        _mode.value = 0
    }
//요걸로 써야함!
    fun setMode(mode: Int) {
        _mode.value = mode
    }
}