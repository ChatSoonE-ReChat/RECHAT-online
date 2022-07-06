package com.chatsoone.rechat.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass
import com.chatsoone.rechat.ApplicationClass.Companion.ACT
import com.chatsoone.rechat.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.chatsoone.rechat.ApplicationClass.Companion.showToast
import com.chatsoone.rechat.NotificationListener
import com.chatsoone.rechat.R
import com.chatsoone.rechat.base.BaseActivity
import com.chatsoone.rechat.data.entity.Icon
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.data.remote.ChatList
import com.chatsoone.rechat.data.remote.FolderList
import com.chatsoone.rechat.data.remote.chat.ChatService
import com.chatsoone.rechat.data.remote.folder.FolderService
import com.chatsoone.rechat.databinding.ActivityMainBinding
import com.chatsoone.rechat.databinding.ItemFolderListBinding
import com.chatsoone.rechat.databinding.ItemIconBinding
import com.chatsoone.rechat.ui.*
import com.chatsoone.rechat.ui.adapter.FolderListRVAdapter
import com.chatsoone.rechat.ui.adapter.IconRVAdapter
import com.chatsoone.rechat.ui.explain.ExplainActivity
import com.chatsoone.rechat.ui.main.blocklist.BlockListFragment
import com.chatsoone.rechat.ui.main.folder.MyFolderFragment
import com.chatsoone.rechat.ui.main.hiddenfolder.MyHiddenFolderFragment
import com.chatsoone.rechat.ui.main.home.HomeFragment
import com.chatsoone.rechat.ui.pattern.CreatePatternActivity
import com.chatsoone.rechat.ui.pattern.InputPatternActivity
import com.chatsoone.rechat.ui.setting.PrivacyInfoActivity
import com.chatsoone.rechat.ui.view.ChatView
import com.chatsoone.rechat.ui.view.FolderAPIView
import com.chatsoone.rechat.ui.view.FolderListView
import com.chatsoone.rechat.ui.view.GetChatListView
import com.chatsoone.rechat.ui.viewmodel.ChatTypeViewModel
import com.chatsoone.rechat.ui.viewmodel.ItemViewModel
import com.chatsoone.rechat.ui.viewmodel.LockViewModel
import com.chatsoone.rechat.util.getID
import com.chatsoone.rechat.util.permissionGrantred
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import java.io.ByteArrayOutputStream

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    NavigationView.OnNavigationItemSelectedListener, ChatView, FolderListView, FolderAPIView {
    private lateinit var database: AppDatabase
    private lateinit var selectedItemList: ArrayList<ChatList>
    private lateinit var mPopupWindow: PopupWindow

    // RecyclerView adapter
    private lateinit var folderListRVAdapter: FolderListRVAdapter
    private lateinit var iconRVAdapter: IconRVAdapter

    // Service
    private lateinit var chatService: ChatService
    private lateinit var folderService: FolderService

    private var userID = getID()
    private var iconList = ArrayList<Icon>()
    private var folderList = ArrayList<FolderList>()
    private var permission: Boolean = true

    private val chatViewModel: ChatTypeViewModel by viewModels<ChatTypeViewModel>()
    private val selectedItemViewModel: ItemViewModel by viewModels<ItemViewModel>()
    private val lockViewModel: LockViewModel by viewModels()

    // 광고
    private lateinit var mAdview: AdView
    private lateinit var adRequest: AdRequest

    override fun initAfterBinding() {
        chatService = ChatService()
        folderService = FolderService()

        initIcon()
        initChatViewModel()
        initSelectedItemViewModel()
        lockViewModel.setMode(0)
    }

    // 아이콘 초기화
    private fun initIcon() {
        database = AppDatabase.getInstance(this)!!
        iconList = database.iconDao().getIconList() as ArrayList

        if (iconList.isEmpty()) {
            // 아이콘 목록 추가
            database.iconDao().insert(Icon(R.drawable.folder_bear))
            database.iconDao().insert(Icon(R.drawable.folder_cat))
            database.iconDao().insert(Icon(R.drawable.folder_dog))
            database.iconDao().insert(Icon(R.drawable.folder_rabbit))

            database.iconDao().insert(Icon(R.drawable.folder_exctie))
            database.iconDao().insert(Icon(R.drawable.folder_love))
            database.iconDao().insert(Icon(R.drawable.folder_twinkle))

            database.iconDao().insert(Icon(R.drawable.folder_spring))
            database.iconDao().insert(Icon(R.drawable.folder_summer))
            database.iconDao().insert(Icon(R.drawable.folder_fall))
            database.iconDao().insert(Icon(R.drawable.folder_winter))

            database.iconDao().insert(Icon(R.drawable.folder_simple_babypink))
            database.iconDao().insert(Icon(R.drawable.folder_simple_pink))
            database.iconDao().insert(Icon(R.drawable.folder_simple_skyblue))
            database.iconDao().insert(Icon(R.drawable.folder_simple_blue))
            database.iconDao().insert(Icon(R.drawable.folder_simple_yellow))
            database.iconDao().insert(Icon(R.drawable.folder_simple_orange))
            database.iconDao().insert(Icon(R.drawable.folder_simple_gray))
            database.iconDao().insert(Icon(R.drawable.folder_simple_more_gray))

            iconList = database.iconDao().getIconList() as ArrayList
        }
    }

    private fun initChatViewModel() {
        // observe mode
        chatViewModel.mode.observe(this) {
            when (it) {
                0 -> setDefaultMode()
                1 -> setChooseMode()
                else -> setFolderMode()
            }
            Log.d(ACT, "MAIN/mode: $it")
        }
    }

    // 기본 모드
    private fun setDefaultMode() {
        binding.mainLayout.mainBnvCenterDefaultIv.visibility = View.VISIBLE
        binding.mainLayout.mainBnvCenterChooseIv.visibility = View.GONE
        binding.mainLayout.mainBnvCenterFolderIv.visibility = View.GONE
    }

    // 선택 모드
    private fun setChooseMode() {
        binding.mainLayout.mainBnvCenterDefaultIv.visibility = View.GONE
        binding.mainLayout.mainBnvCenterChooseIv.visibility = View.VISIBLE
        binding.mainLayout.mainBnvCenterFolderIv.visibility = View.GONE
    }

    // 폴더 모드
    private fun setFolderMode() {
        binding.mainLayout.mainBnvCenterDefaultIv.visibility = View.GONE
        binding.mainLayout.mainBnvCenterChooseIv.visibility = View.GONE
        binding.mainLayout.mainBnvCenterFolderIv.visibility = View.VISIBLE
    }

    private fun initSelectedItemViewModel() {
        selectedItemViewModel.list.observe(this) {
            selectedItemList = it
            Log.d(ACT, "MAIN/selectedItemList: $selectedItemList")
        }
    }

    override fun onStart() {
        super.onStart()

        initAds()
        initBottomNavigationView()
        initDrawerLayout()
        initClickListener()
    }

    // 광고 초기화
    private fun initAds() {
        MobileAds.initialize(this)
        val headerView = binding.mainNavigationView.getHeaderView(0)
        mAdview = headerView.findViewById<AdView>(R.id.adViews)
        adRequest = AdRequest.Builder().build()
        mAdview.loadAd(adRequest)
    }

    private fun initBottomNavigationView() {
        val correctSPF = getSharedPreferences("lock_correct", MODE_PRIVATE)
        // 잠금 모드이면
        if (correctSPF.getInt("correct", 0) == 1) {
            changeFragmentOnMain(MyHiddenFolderFragment())
        } else {
            changeFragmentOnMain(HomeFragment())
        }

        binding.mainLayout.mainBnv.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.main_bnv_home -> {
                    // 전체 채팅
                    val editor = correctSPF.edit()
                    editor.putInt("correct", 3) // 임의의 값
                    editor.apply()

                    changeFragmentOnMain(HomeFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.main_bnv_block_list -> {
                    // 차단 목록
                    val editor = correctSPF.edit()
                    editor.putInt("correct", 3) // 임의의 값
                    editor.apply()

                    changeFragmentOnMain(BlockListFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.main_bnv_folder -> {
                    // 보관함
                    val editor = correctSPF.edit()
                    editor.putInt("correct", 3) // 임의의 값
                    editor.apply()

                    changeFragmentOnMain(MyFolderFragment())

                    return@setOnItemSelectedListener true
                }

                R.id.main_bnv_hidden_folder -> {
                    // 숨긴 보관함
                    val lockSPF = getSharedPreferences("lock", 0)
                    val pattern = lockSPF.getString("pattern", "0")

                    // 패턴 모드 설정
                    // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
                    // 1: 메인 화면의 설정창 -> 변경 모드
                    // 2: 폴더 화면의 설정창 -> 변경 모드
                    // 3: 메인 화면 폴더 리스트에서 숨김 폴더 클릭 시
                    val modeSPF = getSharedPreferences("mode", 0)
                    val editor = modeSPF.edit()

                    // 여기서는 0번 모드
                    editor.putInt("mode", 0)
                    editor.apply()

                    if (pattern.equals("0")) {   // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                        Toast.makeText(this, "패턴이 설정되어 있지 않습니다.\n패턴을 설정해주세요.", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(this@MainActivity, CreatePatternActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, InputPatternActivity::class.java))
                    }

                    // 올바른 패턴인지 확인
                    // 1: 올바른 패턴
                    // 2: 올바르지 않은 패턴
                    initHiddenFolder()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    private fun initDrawerLayout() {
        binding.mainNavigationView.setNavigationItemSelectedListener(this)

        val menuItem = binding.mainNavigationView.menu.findItem(R.id.navi_setting_alarm_item)
        val drawerSwitch =
            menuItem.actionView.findViewById(R.id.main_drawer_alarm_switch) as SwitchCompat

        // 알림 권한 허용 여부에 따라 스위치 초기 상태 지정
        if (permissionGrantred(this)) {
            // 알림 권한이 허용되어 있는 경우
            drawerSwitch.toggle()
            drawerSwitch.isChecked = true
            permission = true
        } else {
            // 알림 권한이 허용되어 있지 않은 경우
            drawerSwitch.isChecked = false
            permission = false
        }

        drawerSwitch.setOnClickListener {
            if (drawerSwitch.isChecked) {
                // 알림 권한을 허용했을 때
                permission = true
                Log.d(ACT, "MAIN/drawerSwitch.isChecked == true")
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))

                if (permissionGrantred(this)) {
                    Toast.makeText(this, "알림 권한을 허용합니다.", Toast.LENGTH_SHORT).show()
                    Log.d(ACT, "MAIN/inPermission")
                    startService(Intent(this, NotificationListener::class.java))
                }
            } else {
                // 알림 권한을 허용하지 않았을 때
                permission = false
                Log.d(ACT, "MAIN/drawerSwitch.isChecked == false")
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                if (!permissionGrantred(this)) {
                    stopService(Intent(this, NotificationListener::class.java))
                    Toast.makeText(this, "알림 권한을 허용하지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initClickListener() {
        // 설정 메뉴창을 여는 메뉴 아이콘 클릭시 설정 메뉴창 열리도록
        binding.mainLayout.mainSettingMenuIv.setOnClickListener {
            if (!binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                // 설정 메뉴창이 닫혀있을 때
                mAdview.loadAd(adRequest)
                binding.mainDrawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // 설정 메뉴창에 있는 메뉴 아이콘 클릭했을 때 설정 메뉴 닫히도록
        val headerView = binding.mainNavigationView.getHeaderView(0)
        headerView.findViewById<ImageView>(R.id.main_drawer_setting_menu_iv).setOnClickListener {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }

        // 선택 모드일 때
        binding.mainLayout.mainBnvCenterChooseIv.setOnClickListener {
            if (selectedItemList.isNotEmpty()) {
                openPopupWindow()
            }
        }

        // 폴더 모드일 때
        binding.mainLayout.mainBnvCenterFolderIv.setOnClickListener {
            setFolderName()
        }
    }

    // 폴더 초기화
    private fun initFolder() {
        folderListRVAdapter = FolderListRVAdapter(this)
        folderService.getFolderList(this, userID)
    }

    // 숨긴 폴더 초기화
    private fun initHiddenFolder() {
        val spf = getSharedPreferences("lock_correct", 0)
        Log.d("mainspfcheck", "spf is ${spf.getInt("correct", 0)}")

        if (spf.getInt("correct", 0) == 1) {
            changeFragmentOnMain(MyHiddenFolderFragment())
            Log.d("mainspfcheck", "this is hidden")
        } else if (spf.getInt("correct", 0) == -1) {
            changeFragmentOnMain(MyFolderFragment())
        } else {
            changeFragmentOnMain(BlockListFragment())
        }
    }

    // 설정 메뉴 창의 네비게이션 드로어의 아이템들에 대한 이벤트를 처리
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 알림 설정
            R.id.navi_setting_alarm_item -> {
                showToast(this, "알림 설정")
            }

            // 패턴 변경하기
            R.id.navi_setting_pattern_item -> {
                val lockSPF = getSharedPreferences("lock", 0)
                val pattern = lockSPF.getString("pattern", "0")

                // 앱 삭제할때 같이 DB 저장 X
                // 패턴 모드 설정
                // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
                // 1: 메인 화면의 설정창 -> 변경 모드
                // 2: 폴더 화면의 설정창 -> 변경 모드
                // 3: 메인 화면 폴더로 보내기 -> 숨김 폴더 눌렀을 경우
                val modeSPF = getSharedPreferences("mode", 0)
                val editor = modeSPF.edit()
                editor.putInt("mode", 1)
                editor.apply()

                if (pattern.equals("0")) {   // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                    startNextActivity(CreatePatternActivity::class.java)
                } else {    // 패턴이 설정되어 있는 경우 입력 페이지로 (보안을 위해)
                    startNextActivity(InputPatternActivity::class.java)
                }
            }
            // 사용 방법 도움말
            R.id.navi_setting_helper_item -> {
                ApplicationClass.mSharedPreferences = getSharedPreferences("explain", MODE_PRIVATE)
                val editor = ApplicationClass.mSharedPreferences.edit()
                editor.putInt("explain_from_menu", 1)
                editor.apply()

                startNextActivity(ExplainActivity::class.java)
            }

            // 개인정보 처리방침
            R.id.navi_setting_privacy_item -> {
                startNextActivity(PrivacyInfoActivity::class.java)
            }
        }
        return false
    }

    // 폴더로 보내기 팝업 윈도우
    @SuppressLint("InflateParams")
    private fun openPopupWindow() {
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.4f).toInt()

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_to_folder, null)
        mPopupWindow = PopupWindow(popupView, width, height)

        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.mainLayout.mainBgV.visibility = View.VISIBLE

        // 폴더 목록 recycler view 초기화
        initFolderListRecyclerView(popupView)
    }

    // 폴더/보관함 보여주는 recycler view 초기화
    private fun initFolderListRecyclerView(popupView: View) {
        folderListRVAdapter = FolderListRVAdapter(this)

        val folderListRV =
            popupView.findViewById<RecyclerView>(R.id.popup_window_to_folder_menu_recycler_view)

        folderListRV.adapter = folderListRVAdapter
        folderListRVAdapter.setMyItemClickListener(object :
            FolderListRVAdapter.MyItemClickListener {
            override fun onFolderClick(itemBinding: ItemFolderListBinding, itemPosition: Int) {
                // 이동하고 싶은 폴더/보관함 클릭했을 때 해당 폴더/보관함으로 채팅 이동
                val selectedFolder = folderList[itemPosition]

                val folderIdx = folderList[itemPosition].folderIdx

                for (i in selectedItemList) {
                    chatService.addChatToFolder(
                        this@MainActivity,
                        userID,
                        i.chatIdx,
                        folderList[itemPosition]
                    )
                }

                // 팝업 윈도우 종료
                mPopupWindow.dismiss()
            }
        })
    }

    // 새폴더 이름 설정
    @SuppressLint("InflateParams")
    private fun setFolderName() {
        val size = windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.4f).toInt()

        val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_set_folder_name, null)
        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)

        mPopupWindow.animationStyle = 0
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.mainLayout.mainBgV.visibility = View.VISIBLE
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_set_name_button)
            .setOnClickListener {
                // 작성한 폴더 이름을 반영한 새폴더를 만들어준다.
                val name =
                    mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_set_name_et).text.toString()

                // 팝업 윈도우 종료
                mPopupWindow.dismiss()

                // 작성한 폴더 이름을 setFolderIcon 함수로 넘겨준다.
                setFolderIcon(name)
            }
        chatViewModel.setMode(2)
    }

    // 새폴더 아이콘 설정
    @SuppressLint("InflateParams")
    private fun setFolderIcon(name: String) {
        // 팝업 윈도우 사이즈를 잘못 맞추면 아이템들이 안 뜨므로 하드 코딩으로 사이즈 조정해주기
        // 아이콘 16개 (기본)
        val size = windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.6f).toInt()

        // 아이콘 바꾸기 팝업 윈도우
        val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_icon, null)
        mPopupWindow = PopupWindow(popupView, width, height)

        mPopupWindow.animationStyle = 0        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.mainLayout.mainBgV.visibility = View.VISIBLE
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // RecyclerView 초기화
        iconRVAdapter = IconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter =
            iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object : IconRVAdapter.MyItemClickListener {
            // 아이콘을 하나 선택했을 경우
            override fun onIconClick(itemIconBinding: ItemIconBinding, iconPosition: Int) {
                val selectedIcon = iconList[iconPosition]

                val iconBitmap = BitmapFactory.decodeResource(resources, selectedIcon.iconImage)
                val baos = ByteArrayOutputStream()
                iconBitmap.compress(Bitmap.CompressFormat.PNG, 70, baos)

                val iconBitmapAsByte = baos.toByteArray()
                val iconBitmapAsString = Base64.encodeToString(iconBitmapAsByte, Base64.DEFAULT)

                val newFolderPosition = folderList.size - 1
                val value = TypedValue()
                resources.getValue(selectedIcon.iconImage, value, true)

                if (value.string != null) {
                    folderService.changeFolderName(
                        this@MainActivity,
                        userID,
                        folderList[newFolderPosition].folderIdx,
                        FolderList(
                            folderList[newFolderPosition].folderIdx,
                            name,
                            value.string.toString()
                        )
                    )
                    folderService.changeFolderIcon(
                        this@MainActivity,
                        userID,
                        folderList[newFolderPosition].folderIdx,
                        FolderList(
                            folderList[newFolderPosition].folderIdx,
                            name,
                            value.string.toString()
                        )
                    )
                }

                mPopupWindow.dismiss()
            }
        })
        chatViewModel.setMode(2)
    }

    // 드로어가 나와있을 때 뒤로 가기 버튼을 한 경우 뒤로 가기 버튼에 대한 이벤트를 처리
    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawers()
        } else if (chatViewModel.mode.value == 1) {
            chatViewModel.setMode(mode = 0)
        } else {
            super.onBackPressed()
        }
    }

    // 팝업창 닫을 때
    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            if (chatViewModel.mode.value == 0)
                chatViewModel.setMode(0)    // 혹은 바로 setDefaultMode() 가능
            else if (chatViewModel.mode.value == 2)
                chatViewModel.setMode(2)
            binding.mainLayout.mainBgV.visibility = View.INVISIBLE
        }
    }

    override fun onChatSuccess() {
        Log.d(ACT, "MAIN/onChatSuccess")
    }

    override fun onChatFailure(code: Int, message: String) {
        Log.d(ACT, "MAIN/onChatFailure/code: $code, message: $message")
    }

    override fun onFolderListSuccess(folderList: ArrayList<FolderList>) {
        Log.d(ACT, "MAIN/onFolderListSuccess/folderList: $folderList")
        this.folderList.clear()
        this.folderList.addAll(folderList)
        openPopupWindow()
    }

    override fun onFolderListFailure(code: Int, message: String) {
        Log.d(ACT, "MAIN/onFolderListFailure/code: $code, message: $message")
    }

    override fun onFolderAPISuccess() {
        Log.d(ACT, "MAIN/onFolderAPISuccess")
        initFolder()
    }

    override fun onFolderAPIFailure(code: Int, message: String) {
        Log.d(ACT, "MAIN/onFolderAPIFailure/code: $code, message: $message")
    }
}
