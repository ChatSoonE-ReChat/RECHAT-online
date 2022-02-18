package com.chat_soon_e.re_chat.ui

import android.graphics.Insets
import android.graphics.Point
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.data.remote.chat.ChatService
import com.chat_soon_e.re_chat.data.remote.chat.FolderContent
import com.chat_soon_e.re_chat.data.remote.folder.FolderList
import com.chat_soon_e.re_chat.data.remote.folder.FolderService
import com.chat_soon_e.re_chat.databinding.ActivityFolderContentBinding
import com.chat_soon_e.re_chat.ui.view.ChatView
import com.chat_soon_e.re_chat.ui.view.GetFolderContentView
import com.chat_soon_e.re_chat.utils.getID
import com.google.gson.Gson

// 내 폴더 안의 채팅 리스트를 가져와 주는 것
class FolderContentActivity: BaseActivity<ActivityFolderContentBinding>(ActivityFolderContentBinding::inflate), GetFolderContentView, ChatView {
    private lateinit var database: AppDatabase
    private lateinit var folderContentRVAdapter: FolderContentRVAdapter
    private lateinit var folderService: FolderService
    private lateinit var chatService: ChatService

    private var folderContentList = ArrayList<FolderContent>()
    lateinit var folderInfo: FolderList
    private val userID = getID()
    private val tag = "ACT/FOLDER-CONTENT"

    override fun initAfterBinding() {
        Log.d("AlluserIDCheck", "onChatAct $userID")
        folderService = FolderService()
        chatService = ChatService()

        initData()
        initClickListener()
    }

    // FolderContent 데이터 초기화
    private fun initData(){
        // 전 페이지에서 데이터 가져오는 부분
        if(intent.hasExtra("folderData")) {
            val folderJson = intent.getStringExtra("folderData")
            folderInfo = Gson().fromJson(folderJson, FolderList::class.java)
            binding.folderContentNameTv.text = folderInfo.folderName
            Log.d(tag, "data: $folderInfo")
        }

        initFolderContent()
    }

    private fun initFolderContent() {
        // 휴대폰 윈도우 사이즈를 가져온다.
        val size = windowManager.currentWindowMetricsPointCompat()
        // RecyclerView click listener 초기화
        folderContentRVAdapter = FolderContentRVAdapter(this, size, object: FolderContentRVAdapter.MyClickListener {
            // 채팅 삭제
            override fun onRemoveChat(chatIdx: Int) {
                chatService.deleteChatFromFolder(this@FolderContentActivity, userID, chatIdx, folderInfo.folderIdx)
            }

            // 채팅 롱클릭 시 팝업 메뉴
            override fun onChatLongClick(popupMenu: PopupMenu) {
                popupMenu.show()
            }
        })
        chatService.getFolderContent(this, userID, folderInfo.folderIdx)
    }

    // RecyclerView 초기화
    private fun initRecyclerView() {
        folderContentRVAdapter.addItem(this.folderContentList)
        binding.folderContentRecyclerView.adapter = folderContentRVAdapter

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.folderContentRecyclerView.layoutManager = linearLayoutManager
    }

    // 디바이스 크기에 사이즈를 맞추기 위한 함수
    private fun WindowManager.currentWindowMetricsPointCompat(): Point {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val windowInsets = currentWindowMetrics.windowInsets
            var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
            windowInsets.displayCutout?.run {
                insets = Insets.max(insets, Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom))
            }
            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom
            Point(currentWindowMetrics.bounds.width() - insetsWidth, currentWindowMetrics.bounds.height() - insetsHeight)
        } else{
            Point().apply {
                defaultDisplay.getSize(this)
            }
        }
    }

    private fun initClickListener() {
        // 뒤로 가기 버튼 눌렀을 때
        binding.folderContentBackIv.setOnClickListener {
            finish()
        }
    }

    override fun onGetFolderContentSuccess(folderContents: ArrayList<FolderContent>) {
        Log.d(tag, "onGetFolderContentSuccess()/folderContents: $folderContents")
        this.folderContentList.clear()
        this.folderContentList.addAll(folderContents)
        initRecyclerView()
    }

    override fun onGetFolderContentFailure(code: Int, message: String) {
        Log.d(tag, "onGetFolderContentFailure()/code: $code, message: $message")
    }

    override fun onChatSuccess() {
        Log.d(tag, "onChatSuccess()")
    }

    override fun onChatFailure(code: Int, message: String) {
        Log.d(tag, "onChatFailure()/code: $code, message: $message")
    }
}