package com.chat_soon_e.re_chat.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chat_soon_e.re_chat.data.remote.chat.ChatList
import com.chat_soon_e.re_chat.data.remote.folder.FolderList
import com.chat_soon_e.re_chat.data.remote.folder.HiddenFolderList
import com.chat_soon_e.re_chat.ui.view.FolderListView
import com.chat_soon_e.re_chat.ui.view.GetChatListView
import com.chat_soon_e.re_chat.ui.view.HiddenFolderListView

class ChatListViewModel(getChatListView: GetChatListView, userIdx: Long): ViewModel() {
    private lateinit var chatListLiveData: MutableLiveData<List<ChatList>>
    private var chatListRepository = ChatListRepository()

    fun getChatListLiveData(getChatListView: GetChatListView, userIdx: Long): LiveData<List<ChatList>> {
        chatListLiveData = chatListRepository.getChatList(getChatListView, userIdx)
        return chatListLiveData
    }
}

class FolderListViewModel(folderListView: FolderListView, userIdx: Int): ViewModel() {
    private lateinit var folderListLiveData: MutableLiveData<List<FolderList>>
    private var folderListRepository = FolderListRepository()

    fun getFolderListLiveData(folderListView: FolderListView, userIdx: Long): LiveData<List<FolderList>> {
        folderListLiveData = folderListRepository.getFolderList(folderListView, userIdx)
        return folderListLiveData
    }
}

class HiddenFolderListViewModel(hiddenFolderListView: HiddenFolderListView, userIdx: Int): ViewModel() {
    private lateinit var hiddenFolderListLiveData: MutableLiveData<List<HiddenFolderList>>
    private var hiddenFolderListRepository = HiddenFolderListRepository()

    fun getHiddenFolderListLiveData(hiddenFolderListView: HiddenFolderListView, userIdx: Long): LiveData<List<HiddenFolderList>> {
        hiddenFolderListLiveData = hiddenFolderListRepository.getHiddenFolderList(hiddenFolderListView, userIdx)
        return hiddenFolderListLiveData
    }
}