package com.chat_soon_e.re_chat.ui

import androidx.recyclerview.widget.LinearLayoutManager
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.data.remote.chat.BlockedChatList
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
        //모든 차단된 목록을 가져온다.
        database = AppDatabase.getInstance(this)!!
    }

    private fun initRecyclerView() {
        database = AppDatabase.getInstance(this)!!
        chatService=ChatService()

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.blockListRecyclerView.layoutManager = linearLayoutManager
        blockListRVAdapter= BlockListRVAdapter(this, blockedList, object:BlockListRVAdapter.MyClickListener{
            override fun onRemoveChat(blockList: BlockedChatList) {
                chatService.unblock(this@BlockListActivity,  userID, blockList.blockedName, blockList.groupName)
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
        TODO("Not yet implemented")
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