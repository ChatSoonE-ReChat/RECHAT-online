package com.chat_soon_e.re_chat.ui

import android.graphics.Insets
import android.graphics.Point
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.data.remote.chat.ChatService
import com.chat_soon_e.re_chat.data.remote.chat.FolderContent
import com.chat_soon_e.re_chat.data.remote.folder.FolderList
import com.chat_soon_e.re_chat.data.remote.folder.FolderService
import com.chat_soon_e.re_chat.databinding.ActivityFolderContentBinding
import com.chat_soon_e.re_chat.ui.view_model.FolderContentViewModel
import com.chat_soon_e.re_chat.ui.view.ChatView
import com.chat_soon_e.re_chat.ui.view.GetFolderContentView
import com.chat_soon_e.re_chat.utils.getID
import com.google.gson.Gson

// 내 폴더 안의 채팅 리스트를 가져와 주는 것
class FolderContentActivity: BaseActivity<ActivityFolderContentBinding>(ActivityFolderContentBinding::inflate), GetFolderContentView, ChatView{
    private lateinit var database: AppDatabase
    private lateinit var folderContentRVAdapter: FolderContentRVAdapter
    lateinit var folderInfo: FolderList
    private val userID = getID()
    private val tag = "ACT/FOLDER-CONTENT"
    private lateinit var folderService:FolderService
    private lateinit var chatService: ChatService

    override fun initAfterBinding() {
        Log.d("AlluserIDCheck", "onChatAct $userID")

        initData()
        initRecyclerView()
        initClickListener()
    }

    // FolderContent 데이터 초기화
    private fun initData(){
        database = AppDatabase.getInstance(this)!!

        // 전 페이지에서 데이터 가져오는 부분
        if(intent.hasExtra("folderData")) {
            val folderJson = intent.getStringExtra("folderData")
            folderInfo = Gson().fromJson(folderJson, FolderList::class.java)
            binding.folderContentNameTv.text = folderInfo.folderName
            Log.d(tag, "data: $folderInfo")
        }
    }
    //해당 폴더를 눌렀을떄 요기로 오게 된다

    // RecyclerView 초기화
    private fun initRecyclerView() {
        // 휴대폰 윈도우 사이즈를 가져온다.
        val size = windowManager.currentWindowMetricsPointCompat()

        database = AppDatabase.getInstance(this)!!
        folderService= FolderService()

        // FolderContent 데이터를 RecyclerView 어댑터와 연결
        // userID: kakaoUserIdx, folderInfo.idx: folder index
//        database.folderContentDao().getFolderChat(userID, folderInfo.idx).observe(this) {
//            folderContentRVAdapter.addItem(it)
//            Log.d("folderDatacheck: ", it.toString())
//        }

        // ViewModel: Folder 안의 채팅들을 불러오기. FolderViewModel
        val folderContentViewModel=ViewModelProvider(this).get(FolderContentViewModel::class.java)
        folderContentViewModel.getFolderContentLiveData(this, userID, folderInfo.folderIdx).observe(this){
            folderContentRVAdapter.addItem(it)
        }


        // RecyclerView click listener 초기화
        folderContentRVAdapter = FolderContentRVAdapter(this, size, object: FolderContentRVAdapter.MyClickListener {
            // 채팅 삭제
            override fun onRemoveChat(chatIdx: Int) {
                chatService.deleteChatFromFolder(this@FolderContentActivity, userID, chatIdx, folderInfo.folderIdx)
                //database.folderContentDao().deleteChat(folderInfo.idx, chatIdx)
            }

            // 채팅 롱클릭 시 팝업 메뉴
            override fun onChatLongClick(popupMenu: PopupMenu) {
                popupMenu.show()
            }
        })
        binding.folderContentRecyclerView.adapter = folderContentRVAdapter
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
        TODO("Not yet implemented")
    }

    override fun onGetFolderContentFailure(code: Int, message: String) {
        TODO("Not yet implemented")
    }

    override fun onChatSuccess() {
        TODO("Not yet implemented")
    }

    override fun onChatFailure(code: Int, message: String) {
        TODO("Not yet implemented")
    }
}