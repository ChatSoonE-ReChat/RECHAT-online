package com.chat_soon_e.re_chat.ui.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chat_soon_e.re_chat.data.remote.chat.BlockedChatList
import com.chat_soon_e.re_chat.data.remote.chat.ChatList
import com.chat_soon_e.re_chat.data.remote.chat.FolderContent
import com.chat_soon_e.re_chat.data.remote.folder.FolderList
import com.chat_soon_e.re_chat.data.remote.folder.HiddenFolderList
import com.chat_soon_e.re_chat.ui.HiddenFolderListRepository
import com.chat_soon_e.re_chat.ui.repository.*
import com.chat_soon_e.re_chat.ui.view.*

class ChatListViewModel(getChatListView: GetChatListView, userIdx: Long): ViewModel() {
    private lateinit var chatListLiveData: MutableLiveData<List<ChatList>>
    private var chatListRepository = ChatListRepository()

    fun getChatListLiveData(getChatListView: GetChatListView, userIdx: Long): LiveData<List<ChatList>> {
        chatListLiveData = chatListRepository.getChatList(getChatListView, userIdx)
        return chatListLiveData
    }
}

class ChatViewModel:ViewModel(){
    private lateinit var chatLiveData:MutableLiveData<List<ChatList>>
    private var chatRepository = ChatRepository()

    fun getChatLiveData(getChatView: GetChatView, userIdx: Long, chatIdx:Int, groupName: String?):LiveData<List<ChatList>>{
        chatLiveData=chatRepository.getChat(getChatView, userIdx, chatIdx, groupName)
        return chatLiveData
    }
}

class FolderListViewModel(getFolderListView: FolderListView, userIdx: Int): ViewModel() {
    private lateinit var folderListLiveData: MutableLiveData<List<FolderList>>
    private var folderListRepository = FolderListRepository()

    fun getFolderListLiveData(folderListView: FolderListView, userIdx: Long): LiveData<List<FolderList>> {
        folderListLiveData = folderListRepository.getFolderList(folderListView, userIdx)
        return folderListLiveData
    }
}

class FolderContentViewModel(getFolderContentView:GetFolderContentView, userIdx:Long, folderIdx:Int):ViewModel(){
    private lateinit var folderContentLiveData: MutableLiveData<List<FolderContent>>
    private var folderContentRepository= FolderContentRepository()

    fun getFolderContentLiveData(getFolderContentView: GetFolderContentView, userIdx: Long, folderIdx: Int):LiveData<List<FolderContent>>{
        folderContentLiveData=folderContentRepository.getFolderContent(getFolderContentView, userIdx, folderIdx)
        return folderContentLiveData
    }
}

class BlockChatViewModel(getBlockedChatListView:GetBlockedChatListView, userIdx:Long, folderIdx:Int):ViewModel(){
    private lateinit var blockChatLiveData: MutableLiveData<List<BlockedChatList>>
    private var blockChatRepository= BlockedChatListRepository()

    fun getBlockChatLiveData(getBlockedChatListView: GetBlockedChatListView, userIdx: Long):LiveData<List<BlockedChatList>>{
        blockChatLiveData= blockChatRepository.getBlockedChatList(getBlockedChatListView, userIdx)
        return blockChatLiveData
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