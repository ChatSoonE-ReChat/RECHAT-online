package com.chat_soon_e.re_chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.data.remote.chat.BlockedChatList
import com.chat_soon_e.re_chat.data.remote.chat.Chat
import com.chat_soon_e.re_chat.data.remote.chat.ChatService
import com.chat_soon_e.re_chat.databinding.ActivityBlockListBinding
import com.chat_soon_e.re_chat.ui.view.ChatView
import com.chat_soon_e.re_chat.ui.view.GetBlockedChatListView
import com.chat_soon_e.re_chat.utils.getID

class BlockListActivity:BaseActivity<ActivityBlockListBinding>(ActivityBlockListBinding::inflate), GetBlockedChatListView, ChatView{
    lateinit var blockListRVAdapter: BlockListRVAdapter
    lateinit var database: AppDatabase
    private var blockedList = ArrayList<BlockedChatList>()
    private val userID = getID()
    private lateinit var chatService: ChatService

    override fun initAfterBinding() {
        //초기 설정
        initData()
        initRecyclerView()
        initClickListener()
    }

    private fun initData() {
//        //모든 차단된 목록을 가져온다.
//        database = AppDatabase.getInstance(this)!!
//
//        database.chatDao().getBlockedChatList(userID).observe(this) {
//            blockedList.clear()
//            blockedList.addAll(it)
//        }

        // ViewModel: Blocked List 가져오기
//        val blockedListViewModel=ViewModelProvider(this).get(BlockChatViewModel::class.java)
//        blockedListViewModel.getBlockChatLiveData(this, userID).observe(this){
//            blockedList.clear()
//            blockedList.addAll(it)
//        }

        // ServerAPI: BlockedList 가져오기
        chatService.getBlockedChatList(this, userID)
    }

    private fun initRecyclerView() {
        database = AppDatabase.getInstance(this)!!
        chatService=ChatService()

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.blockListRecyclerView.layoutManager = linearLayoutManager
        blockListRVAdapter= BlockListRVAdapter(this, blockedList, object:BlockListRVAdapter.MyClickListener{
            override fun onRemoveChat(blockList: BlockedChatList) {
//                if(blockList.groupName==null||blockList.groupName=="null")//개인톡
//                    database.chatDao().unblockOneChat(userID, blockList.blockedName)
//                else
//                    database.chatDao().unblockOrgChat(userID, blockList.groupName)
                // 삭제 오류 검토
                chatService.unblock(this@BlockListActivity,  userID, blockList.blockedName, blockList.groupName)
                chatService.getBlockedChatList(this@BlockListActivity, userID)
            }
        })
        binding.blockListRecyclerView.adapter=blockListRVAdapter
    }

    private fun initClickListener() {
        binding.blockListBackIv.setOnClickListener {
            finish()
        }
    }

    override fun onGetBlockedChatListSuccess(blockedChatList: ArrayList<BlockedChatList>) {
        blockedList.clear()
        blockedList.addAll(blockedChatList)
    }

    override fun onGetBlockedChatListFailure(code: Int, message: String) {
        TODO("Not yet implemented")
    }

    override fun onChatSuccess() {
        TODO("Not yet implemented")
    }

    override fun onChatFailure(code: Int, message: String) {
        TODO("Not yet implemented")
    }
}