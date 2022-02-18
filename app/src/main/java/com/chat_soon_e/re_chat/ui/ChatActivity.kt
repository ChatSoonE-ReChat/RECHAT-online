package com.chat_soon_e.re_chat.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Insets
import android.graphics.Point

import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.databinding.ActivityChatBinding
import com.chat_soon_e.re_chat.utils.getID
import com.chat_soon_e.re_chat.databinding.ItemFolderListBinding
import com.chat_soon_e.re_chat.data.remote.chat.ChatList
import com.chat_soon_e.re_chat.data.remote.chat.ChatService
import com.chat_soon_e.re_chat.data.remote.folder.FolderList
import com.chat_soon_e.re_chat.data.remote.folder.FolderService
import com.chat_soon_e.re_chat.databinding.ItemChatBinding
import com.chat_soon_e.re_chat.ui.view_model.ChatTypeViewModel

import com.chat_soon_e.re_chat.ui.view.ChatView
import com.chat_soon_e.re_chat.ui.view.FolderListView
import com.chat_soon_e.re_chat.ui.view.GetChatView
import java.util.*
import kotlin.collections.ArrayList

//채팅의 폴더 리스트
// 순서
//1. 리사이클러뷰 초기화,(데이터 X)
//2. 서버 API 데이터 받아오기
//3. recyclerview 데이터 넘기기

class ChatActivity: BaseActivity<ActivityChatBinding>(ActivityChatBinding::inflate),
    ChatView, GetChatView, FolderListView {
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation
    private lateinit var chatRVAdapter: ChatRVAdapter
    private lateinit var folderListRVAdapter: FolderListRVAdapter
    private lateinit var chatListData: ChatList
    private lateinit var mPopupWindow: PopupWindow
    private lateinit var folderService: FolderService
    private lateinit var chatService: ChatService

    private val chatTypeViewModel: ChatTypeViewModel by viewModels()
    private var isFabOpen = false    // FAB(FloatingActionButton)가 열렸는지 체크해주는 변수
    private val userID = getID()
    private val tag = "ACT/CHAT"
    private var chatList = ArrayList<ChatList>()
    private var folderList = ArrayList<FolderList>()

    override fun initAfterBinding() {
        Log.d(tag, "onChatAct $userID")
        folderService = FolderService()
        chatService = ChatService()

//        initFab()
        initData()
        initClickListener()
    }

    // FAB 애니메이션 초기화
    private fun initFab() {
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
    }

    // MainActivity로 부터 데이터를 가져온다.
    private fun initData() {
        if (intent.hasExtra("chatListJson")) {
            chatListData = intent.getSerializableExtra("chatListJson") as ChatList
            if (chatListData.groupName == null || chatListData.groupName == "null")
                binding.chatNameTv.text = chatListData.chatName
            else
                binding.chatNameTv.text = chatListData.groupName
            Log.d("chatListInitData", chatListData.toString())
        }
        initChat()
    }

    // 전체폴더 목록 가져오기 (숨김폴더 제외)
    private fun initFolder() {
        folderListRVAdapter = FolderListRVAdapter(this)
        folderService.getFolderList(this, userID)
    }

    // 갠톡 or 단톡 가져오기
    private fun initChat() {
        val size = windowManager.currentWindowMetricsPointCompat()
        chatRVAdapter = ChatRVAdapter(this, size, object : ChatRVAdapter.MyItemClickListener {
            // 채팅 삭제
            override fun onRemoveChat() {
                Log.d("chatPositionCheck", "지우려는 채팅들 chatLIst: $chatList")

                // Server API: 채팅들 지우기
                // 선택된 chatIdx들 모두 가져와서 지우기
                val selectedList = chatRVAdapter.getSelectedItemList()
                for(i in selectedList){
                    chatService.deleteChat(this@ChatActivity, userID, i)
                }
            }

            // 선택 모드
            override fun onChooseChatClick(view: View, position: Int) {
                chatRVAdapter.setChecked(position)
                Log.d("chatPositionCheck", "selected position $position")
            }
        })
        chatService.getChat(this, userID, chatListData.chatIdx, chatListData.groupName)
   }

    // RecyclerView
    private fun initRecyclerView() {
        chatRVAdapter.addItem(this.chatList)

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.chatChatRecyclerView.layoutManager = linearLayoutManager

        chatTypeViewModel.mode.observe(this) {
            if (it == 0) {
                // 일반 모드
                chatRVAdapter.clearSelectedItemList()
                chatRVAdapter.addItem(chatList)
            } else {
                // 선택 모드
                chatRVAdapter.clearSelectedItemList()
                chatRVAdapter.addItem(chatList)
            }
            // 모든 데이터의 viewType 바꿔주기
            chatRVAdapter.setViewType(currentMode = it)
        }

        // 어댑터 연결
        binding.chatChatRecyclerView.adapter = chatRVAdapter

        // 폴더 선택 모드를 해제하기 위해
        binding.chatCancelFab.setOnClickListener {
            binding.chatMainFab.setImageResource(R.drawable.navi_center_cloud)
            binding.chatCancelFab.startAnimation(fabClose)
            ObjectAnimator.ofFloat(binding.chatCancelFab, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.chatDeleteFab, "translationY", 0f).apply { start() }
//            binding.chatCancelFab.startAnimation(fabClose)
//            binding.chatDeleteFab.startAnimation(fabClose)
            binding.chatCancelFab.visibility = View.INVISIBLE
            binding.chatDeleteFab.visibility = View.INVISIBLE
            binding.chatCancelFab.isClickable = false
            binding.chatDeleteFab.isClickable = false
            isFabOpen = false
            binding.chatBackgroundView.visibility = View.INVISIBLE
            binding.chatUpdateIv.visibility = View.VISIBLE

            // 일반 모드로
            chatRVAdapter.clearSelectedItemList()
            chatTypeViewModel.setMode(mode = 0)
        }
    }

    private fun initClickListener() {
        // 메인 FAB 버튼 눌렀을 때
        binding.chatMainFab.setOnClickListener {
            if (chatTypeViewModel.mode.value == 0) {
                chatTypeViewModel.setMode(mode = 1)
            } else {
                chatTypeViewModel.setMode(mode = 0)
            }

            if (isFabOpen) {
                // fab 버튼이 열려있는 경우 (선택 모드에서 클릭했을 때)
                // 폴더로 보내는 팝업창을 띄운다.
                // 여기서 view는 클릭된 뷰를 의미한다.
                initFolder()
            } else {
                // fab 버튼이 닫혀있는 경우 (일반 모드에서 클릭했을 때)
                binding.chatMainFab.setImageResource(R.drawable.navi_center_cloud_move)
                binding.chatCancelFab.startAnimation(fabOpen)
                ObjectAnimator.ofFloat(binding.chatCancelFab, "translationY", -500f).apply { start() }
                ObjectAnimator.ofFloat(binding.chatDeleteFab, "translationY", -200f).apply { start() }
//                binding.chatCancelFab.startAnimation(fabOpen)
//                binding.chatDeleteFab.startAnimation(fabOpen)
                binding.chatCancelFab.visibility = View.VISIBLE
                binding.chatDeleteFab.visibility = View.VISIBLE
                binding.chatCancelFab.isClickable = true
                binding.chatDeleteFab.isClickable = true
                binding.chatUpdateIv.visibility = View.GONE
                isFabOpen = true
            }
        }

        // 삭제하는 경우
        binding.chatDeleteFab.setOnClickListener {
            val data = chatRVAdapter.removeChat()
            if (data != null)
                chatListData = data
            chatRVAdapter.clearSelectedItemList()
            chatTypeViewModel.setMode(mode = 0)

            binding.chatMainFab.setImageResource(R.drawable.navi_center_cloud)
            ObjectAnimator.ofFloat(binding.chatCancelFab, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.chatDeleteFab, "translationY", 0f).apply { start() }
            binding.chatCancelFab.visibility = View.INVISIBLE
            binding.chatDeleteFab.visibility = View.INVISIBLE
            binding.chatCancelFab.isClickable = false
            binding.chatDeleteFab.isClickable = false
            isFabOpen = false
            binding.chatBackgroundView.visibility = View.INVISIBLE
            binding.chatUpdateIv.visibility = View.VISIBLE

            // 일반 모드로
            chatRVAdapter.clearSelectedItemList()
            chatTypeViewModel.setMode(mode = 0)
        }

        // 뒤로 가기 아이콘 클릭 시
        binding.chatBackIv.setOnClickListener {
            finish()
        }

        // 새로고침 아이콘 클릭 시
        binding.chatUpdateIv.setOnClickListener {
            initChat()
        }
    }
    override fun onBackPressed() {
//        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    // 폴더로 보내기 팝업 윈도우
    @SuppressLint("InflateParams")
    private fun popupWindowToFolderMenu() {
        // 채팅 폴더 이동시 필요한 폴더 목록 folderList
        // 팝업 윈도우 사이즈를 잘못 맞추면 아이템들이 안 뜨므로 하드 코딩으로 사이즈 조정해주기
        // 아이콘 16개 (기본)
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.4f).toInt()

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_to_folder_menu, null)
        mPopupWindow = PopupWindow(popupView, width, height)

        mPopupWindow.animationStyle = 0        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true         // 외부 영역 선택 시 팝업 윈도우 종료
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.chatBackgroundView.visibility = View.VISIBLE

        // RecyclerView 구분선
        val recyclerView =
            popupView.findViewById<RecyclerView>(R.id.popup_window_to_folder_menu_recycler_view)
        val dividerItemDecoration =
            DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        // RecyclerView 초기화
        // 더미 데이터와 어댑터 연결
        folderListRVAdapter.addFolderList(this.folderList)
        recyclerView.adapter = folderListRVAdapter
        folderListRVAdapter.setMyItemClickListener(object :
            FolderListRVAdapter.MyItemClickListener {
            override fun onFolderClick(itemBinding: ItemFolderListBinding, itemPosition: Int) {
                // 이동하고 싶은 폴더 클릭 시 폴더로 채팅 이동 (뷰에는 그대로 남아 있도록)
                // 폴더로 이동시키는 코드 작성
                val selectedChatIdx = chatRVAdapter.getSelectedItemList()

                // Server API: 폴더에 한개의 채팅들 삽입
                for (i in selectedChatIdx) {
                    chatService= ChatService()
                    chatService.addChatToFolder(this@ChatActivity, userID, i, folderList[itemPosition])
                }
                mPopupWindow.dismiss()
                binding.chatBackgroundView.visibility = View.INVISIBLE
            }
        })
    }

    // 디바이스 크기에 사이즈를 맞추기 위한 함수
    private fun WindowManager.currentWindowMetricsPointCompat(): Point {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val windowInsets = currentWindowMetrics.windowInsets
            var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
            windowInsets.displayCutout?.run {
                insets = Insets.max(
                    insets,
                    Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom)
                )
            }
            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom
            Point(
                currentWindowMetrics.bounds.width() - insetsWidth,
                currentWindowMetrics.bounds.height() - insetsHeight
            )
        } else {
            Point().apply {
                defaultDisplay.getSize(this)
            }
        }
    }

    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.chatBackgroundView.visibility = View.INVISIBLE
        }
    }

    override fun onGetChatFailure(code: Int, message: String) {
        // 실패시
        Log.d(tag, "onGetChatFailure()/code: $code, message: $message")
    }

    override fun onGetChatSuccess(chats: ArrayList<ChatList>) {
        // 성공시
        this.chatList.clear()
        this.chatList.addAll(chats)
        initRecyclerView()
    }

    override fun onFolderListSuccess(folderList: ArrayList<FolderList>) {
        // 성공 시
        Log.d(tag, "onFolderListSuccess()/folderList: $folderList")
        this.folderList.clear()
        this.folderList.addAll(folderList)
        popupWindowToFolderMenu()
    }

    override fun onFolderListFailure(code: Int, message: String) {
        // 실패시
        // 폴더 리스트를 "null"로 설정해줘야 할까? 채팅 폴더이동이 끝나고 "RVAdapter"에 넣는 부분 때문에
        Log.d(tag, "onFolderListFailure()/code: $code, message: $message")
    }

    override fun onChatSuccess() {
        Log.d(tag, "onChatSuccess()")
    }

    override fun onChatFailure(code: Int, message: String) {
        Log.d(tag, "onChatFailure()/code: $code, message: $message")
    }
}