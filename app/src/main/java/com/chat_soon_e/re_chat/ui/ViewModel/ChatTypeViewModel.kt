package com.chat_soon_e.re_chat.ui.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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