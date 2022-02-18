package com.chat_soon_e.re_chat.ui

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.data.remote.chat.BlockedChatList
import com.chat_soon_e.re_chat.data.remote.chat.ChatService
import com.chat_soon_e.re_chat.databinding.ActivityBlockListBinding
import com.chat_soon_e.re_chat.ui.view.ChatView
import com.chat_soon_e.re_chat.ui.view.GetBlockedChatListView
import com.chat_soon_e.re_chat.utils.getID

class BlockListActivity:BaseActivity<ActivityBlockListBinding>(ActivityBlockListBinding::inflate), GetBlockedChatListView, ChatView{
    private lateinit var chatService: ChatService

    lateinit var blockListRVAdapter: BlockListRVAdapter
    lateinit var database: AppDatabase
    private var blockedList = ArrayList<BlockedChatList>()
    private val userID = getID()
    private val tag = "ACT/BLOCK-LIST"

    override fun initAfterBinding() {
        chatService = ChatService()
        // 초기 설정
        initData()
        initClickListener()
    }

    private fun initData() {
        blockListRVAdapter = BlockListRVAdapter(this, object: BlockListRVAdapter.MyClickListener {
            override fun onRemoveChat(blockList: BlockedChatList) {
                // 삭제 오류 검토
                chatService.unblock(this@BlockListActivity,  userID, blockList.blockedName, blockList.groupName)
//                chatService.getBlockedChatList(this@BlockListActivity, userID)
            }
        })

        chatService.getBlockedChatList(this, userID)
    }

    private fun initRecyclerView() {
        blockListRVAdapter.addItem(this.blockedList)
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.blockListRecyclerView.layoutManager = linearLayoutManager
        binding.blockListRecyclerView.adapter = blockListRVAdapter
    }

    private fun initClickListener() {
        binding.blockListBackIv.setOnClickListener {
            finish()
        }
    }

    override fun onGetBlockedChatListSuccess(blockedChatList: ArrayList<BlockedChatList>) {
        Log.d(tag, "onGetBlockedChatListSuccess()/blockedChatList: $blockedChatList")
        this.blockedList.clear()
        this.blockedList.addAll(blockedChatList)
        initRecyclerView()
    }

    override fun onGetBlockedChatListFailure(code: Int, message: String) {
        Log.d(tag, "onGetBlockedChatListFailure()/code: $code, message: $message")
    }

    override fun onChatSuccess() {
        Log.d(tag, "onChatSuccess()")
    }

    override fun onChatFailure(code: Int, message: String) {
        Log.d(tag, "onChatFailure()/code: $code, message: $message")
    }
}