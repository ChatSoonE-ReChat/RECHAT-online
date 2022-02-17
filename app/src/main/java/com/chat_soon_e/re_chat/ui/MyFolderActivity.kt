package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.databinding.ActivityMyFolderBinding
import com.chat_soon_e.re_chat.databinding.ItemMyFolderBinding
import com.google.android.material.navigation.NavigationView
import androidx.recyclerview.widget.RecyclerView
import com.chat_soon_e.re_chat.data.local.Icon
import com.chat_soon_e.re_chat.data.remote.folder.FolderList
import com.chat_soon_e.re_chat.data.remote.folder.FolderService
import com.chat_soon_e.re_chat.databinding.ItemIconBinding
import com.chat_soon_e.re_chat.ui.view_model.FolderListViewModel
import com.chat_soon_e.re_chat.ui.view.*
import com.chat_soon_e.re_chat.utils.getID
import com.google.gson.Gson
import java.io.ByteArrayOutputStream

class MyFolderActivity: BaseActivity<ActivityMyFolderBinding>(ActivityMyFolderBinding::inflate),
    NavigationView.OnNavigationItemSelectedListener, FolderListView, FolderAPIView {
    private lateinit var database: AppDatabase
    private lateinit var folderRVAdapter: MyFolderRVAdapter
    private lateinit var iconRVAdapter: ChangeIconRVAdapter
    private lateinit var mPopupWindow: PopupWindow
    private lateinit var folderService: FolderService

    private var folderList = ArrayList<FolderList>()
    private var iconList = ArrayList<Icon>()
    private val userID = getID()
    private val tag = "ACT/MYFOLDER"

    // Popupwindow와 RecyclerView 연결을 위해 선언
    private lateinit var itemBinding: ItemMyFolderBinding

    override fun initAfterBinding() {
        Log.d("AlluserIDCheck", "onChatAct $userID")

        folderService = FolderService()
        database = AppDatabase.getInstance(this)!!
        iconList = database.iconDao().getIconList() as ArrayList   // 아이콘 받아오기

//        getFolderListLiveData()
        initDrawerLayout()          // 설정 메뉴창 설정
        initClickListener()         // 여러 click listener 초기화
    }

    override fun onResume() {
        super.onResume()
        initFolder()
    }

//    // 폴더 초기화
//    private fun getFolderListLiveData() {
//        // 전체폴더 목록 가져오기 (숨김폴더 제외)
//        folderService.getFolderList(this, userID)
//
//        val folderListViewModel = ViewModelProvider(this).get(FolderListViewModel::class.java)
//        folderListViewModel.getFolderListLiveData(this, userID).observe(this) {
//            Log.d(tag, "folderList: $folderList")
//            folderList = it as ArrayList<FolderList>
//        }
//    }

    private fun initFolder() {
        // 전체폴더 목록 가져오기 (숨김폴더 제외)
        folderRVAdapter = MyFolderRVAdapter(this)
        folderService.getFolderList(this, userID)
    }

    // RecyclerView
    private fun initRecyclerView() {
        // RecyclerView 초기화
        folderRVAdapter.addFolderList(this.folderList)
        binding.myFolderContent.myFolderFolderListRecyclerView.adapter = folderRVAdapter

//        initFolder()
//        val folderListViewModel = ViewModelProvider(this).get(FolderListViewModel::class.java)
//        folderListViewModel.getFolderListLiveData(this, userID).observe(this) {
//            folderRVAdapter.addFolderList(it as ArrayList<FolderList>)
//        }

        // click listener
        folderRVAdapter.setMyItemClickListener(object: MyFolderRVAdapter.MyItemClickListener {
            // 폴더 이름 롱클릭 시 폴더 이름 변경
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onFolderNameLongClick(binding: ItemMyFolderBinding, position: Int, folderIdx: Int) {
                itemBinding = binding
                changeFolderName(itemBinding, position, folderIdx)
            }

            // 폴더 아이콘 클릭 시 해당 폴더로 이동
            override fun onFolderClick(view: View, position: Int) {
                val selectedFolder = folderRVAdapter.getSelectedFolder(position)

                // folder삽입시 status변경! null아님!!!!!!!!
                val gson = Gson()
                val folderJson = gson.toJson(selectedFolder)

                // 폴더 정보를 보내기
                val intent = Intent(this@MyFolderActivity, FolderContentActivity::class.java)
                intent.putExtra("folderData", folderJson)
                startActivity(intent)
            }

            // 폴더 아이콘 롱클릭 시 팝업 메뉴 뜨도록
            override fun onFolderLongClick(popup: PopupMenu) {
                popup.show()
            }

            // 폴더 삭제하기
            override fun onRemoveFolder(idx: Int) {
                folderService.deleteFolder(this@MyFolderActivity, userID, idx)
            }

            // 폴더 숨기기
            @SuppressLint("NotifyDataSetChanged")
            override fun onHideFolder(idx: Int) {
                folderService.hideFolder(this@MyFolderActivity, userID, idx)
                // 폴더를 숨긴다. 아마 그러면 데이터가 바뀔 것
                folderRVAdapter.notifyDataSetChanged()
            }
        })

        // 원래 여기 LiveData 부분 하나 더 있었는데 위에도 하나 있길래 일단 지움
        // 만약 오류나거나 제대로 반영이 안 된다면 여기에 추가하면 될 듯
    }

    // 설정 메뉴 창을 띄우는 DrawerLayout 초기화
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initDrawerLayout() {
        binding.myFolderNavigationView.setNavigationItemSelectedListener(this)

        val menuItem = binding.myFolderNavigationView.menu.findItem(R.id.navi_setting_alarm_item)
        val drawerSwitch = menuItem.actionView.findViewById(R.id.main_drawer_alarm_switch) as SwitchCompat

        // 알림 권한 허용 여부에 따라 스위치(토글) 초기 상태 지정
        if(permissionGrantred()) {
            // 알림 권한이 허용되어 있는 경우
            drawerSwitch.toggle()
            drawerSwitch.isChecked = true
        } else {
            // 알림 권한이 허용되어 있지 않은 경우
            drawerSwitch.isChecked = false
        }

        // 스위치(토글)를 눌렀을 때, 즉 스위치 체크 상태[방향]가 변했을 때 처리해주는 리스너
        drawerSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 알림 권한을 허용했을 때 코드를 작성해주시면 됩니다.
                Toast.makeText(this, "알림 권한을 허용합니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 알림 권한을 허용하지 않았을 때 코드를 작성해주시면 됩니다.
                Toast.makeText(this, "알림 권한을 허용하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 권한 체크
    private fun permissionGrantred(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(this).any {
                enabledPackageName -> enabledPackageName == packageName
        }
    }

    // 설정 메뉴 창의 네비게이션 드로어의 아이템들에 대한 이벤트를 처리
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            // 설정 메뉴창의 아이템(목록)들을 클릭했을 때
            // 알림 설정
            R.id.navi_setting_alarm_item -> {
                Toast.makeText(this, "알림 설정", Toast.LENGTH_SHORT).show()
            }

            // 패턴 변경하기
            R.id.navi_setting_pattern_item -> {
                val lockSPF = getSharedPreferences("lock", 0)
                val pattern = lockSPF.getString("pattern", "0")

                // 패턴 모드 설정
                // 0: 숨긴 폴더 목록을 확인하기 위한 입력 모드
                // 1: 메인 화면의 설정창 -> 변경 모드
                // 2: 폴더 화면의 설정창 -> 변경 모드
                // 3: 메인 화면 폴더 리스트에서 숨김 폴더 클릭 시
                val modeSPF = getSharedPreferences("mode", 0)
                val editor = modeSPF.edit()

                // 여기서는 2번 모드
                editor.putInt("mode", 2)
                editor.apply()

                if(pattern.equals("0")) {   // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                    startNextActivity(CreatePatternActivity::class.java)
                } else {    // 패턴이 설정되어 있는 경우 입력 페이지로 (보안을 위해)
                    startNextActivity(InputPatternActivity::class.java)
                }
            }

            // 공유하기
            R.id.navi_setting_share_item -> {
                Toast.makeText(this, "공유하기", Toast.LENGTH_SHORT).show()
            }

            // 앱 리뷰하기
            R.id.navi_setting_review_item -> {
                Toast.makeText(this, "앱 리뷰하기", Toast.LENGTH_SHORT).show()
            }

            // 이메일 문의
            R.id.navi_setting_email_item -> {
                Toast.makeText(this, "이메일 문의", Toast.LENGTH_SHORT).show()
            }

            // 사용 방법 도움말
            R.id.navi_setting_helper_item -> {
                Toast.makeText(this, "사용 방법 도움말", Toast.LENGTH_SHORT).show()
            }

            // 개인정보 처리방침
            R.id.navi_setting_privacy_item -> {
                Toast.makeText(this, "개인정보 처리방침", Toast.LENGTH_SHORT).show()
            }

            else -> Toast.makeText(this, "잘못된 항목입니다.", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    // 드로어가 나와있을 때 뒤로 가기 버튼을 한 경우 뒤로 가기 버튼에 대한 이벤트를 처리
    override fun onBackPressed() {
        if(binding.myFolderDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.myFolderDrawerLayout.closeDrawers()
        } else {
            finish()
        }
    }

    // click listener
    private fun initClickListener() {
        // 설정 메뉴창을 여는 메뉴 아이콘 클릭시 설정 메뉴창 열리도록
        binding.myFolderContent.myFolderMenuIv.setOnClickListener {
            if(!binding.myFolderDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                // 설정 메뉴창이 닫혀있을 때
                binding.myFolderDrawerLayout.openDrawer(GravityCompat.START)
            }
        }

        binding.myFolderContent.myFolderAllChatIv.setOnClickListener {
            startNextActivity(MainActivity::class.java)
        }

        // 하단 중앙의 버튼을 눌렀을 때
        binding.myFolderContent.myFolderCreateNewFolderIv.setOnClickListener {
            // popupFolderBottomMenu()
            // 폴더 생성하기
            folderService.createFolder(this, userID)
//            getFolderListLiveData()
            setFolderName()
        }

        // 숨김 보관함 목록
        binding.myFolderContent.myFolderToHiddenFolderIv.setOnClickListener {
            // 숨긴 폴더 목록 보기
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

            if(pattern.equals("0")) {   // 패턴이 설정되어 있지 않은 경우 패턴 설정 페이지로
                Toast.makeText(this@MyFolderActivity, "패턴이 설정되어 있지 않습니다.\n패턴을 설정해주세요.", Toast.LENGTH_SHORT).show()
                startNextActivity(CreatePatternActivity::class.java)
            } else {
                startNextActivity(InputPatternActivity::class.java)
            }
        }

        // 설정 메뉴창에 있는 메뉴 아이콘 클릭시 설정 메뉴창 닫히도록
        val headerView = binding.myFolderNavigationView.getHeaderView(0)
        headerView.setOnClickListener {
            binding.myFolderDrawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    // 이름 바꾸기 팝업 윈도우를 띄워서 폴더 이름을 변경할 수 있도록 해준다.
    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun changeFolderName(itemBinding: ItemMyFolderBinding, position: Int, folderIdx:Int) {

        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.4f).toInt()

        // 이름 바꾸기 팝업 윈도우
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_name, null)
        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)

        mPopupWindow.animationStyle = 0        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true         // 외부 영역 선택 시 팝업 윈도우 종료
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderContent.myFolderBackgroundView.visibility = View.VISIBLE    // 뒷배경 흐려지게
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // 기존 폴더 이름을 팝업 윈도우의 EditText에 넘겨준다.
        var text: String = itemBinding.itemMyFolderTv.text.toString()
        mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).setText(text)

        // RoomDB
        database = AppDatabase.getInstance(this@MyFolderActivity)!!
        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_change_name_button).setOnClickListener {
            text = mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).text.toString()
            itemBinding.itemMyFolderTv.text = text

            Log.d(tag, "changeFolderName()/text: $text")

            val folderImg = folderList[position].folderImg

            // 폴더 이름 바꾸기
            folderService.changeFolderName(this, userID, folderIdx, FolderList(folderIdx, text, folderImg))

            // 팝업 윈도우 종료
            mPopupWindow.dismiss()

            // 뒷배경 원래대로
            binding.myFolderContent.myFolderBackgroundView.visibility = View.INVISIBLE
        }
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun changeIcon(itemBinding: ItemMyFolderBinding, position: Int, folderIdx: Int) {
        // 팝업 윈도우 사이즈를 잘못 맞추면 아이템들이 안 뜨므로 하드 코딩으로 사이즈 조정해주기
        // 아이콘 16개 (기본)
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.6f).toInt()

        // 아이콘 바꾸기 팝업 윈도우
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_icon, null)
        mPopupWindow = PopupWindow(popupView, width, height)

        mPopupWindow.animationStyle = 0        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true

        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderContent.myFolderBackgroundView.visibility = View.VISIBLE    // 뒷배경 흐려지게
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // RecyclerView 초기화
        iconRVAdapter = ChangeIconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter = iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object: ChangeIconRVAdapter.MyItemClickListener {
            // 아이콘을 하나 선택했을 경우
            override fun onIconClick(itemIconBinding: ItemIconBinding, iconPosition: Int) {//해당 파라미터는 아이콘 DB!
                // 선택한 아이콘으로 폴더 이미지 변경
                val selectedIcon = iconList[iconPosition]
                itemBinding.itemMyFolderIv.setImageResource(selectedIcon.iconImage)

                val iconBitmap = BitmapFactory.decodeResource(resources, selectedIcon.iconImage)
                val baos = ByteArrayOutputStream()
                iconBitmap.compress(Bitmap.CompressFormat.PNG, 70, baos)

                val iconBitmapAsByte = baos.toByteArray()
                val iconBitmapAsString = Base64.encodeToString(iconBitmapAsByte, Base64.DEFAULT)

                // 폴더 아이콘 바꾸기
                folderService.changeFolderIcon(this@MyFolderActivity, userID, folderIdx, FolderList(folderIdx, folderList[position].folderName, iconBitmapAsString))

                // 팝업 윈도우 종료
                mPopupWindow.dismiss()
            }
        })
    }

    // 새폴더 이름 설정
    @SuppressLint("InflateParams")
    private fun setFolderName() {
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.4f).toInt()

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_set_folder_name, null)
        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)

        mPopupWindow.animationStyle = 0        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true         // 외부 영역 선택 시 팝업 윈도우 종료
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderContent.myFolderBackgroundView.visibility = View.VISIBLE    // 뒷배경 흐려지게
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_set_name_button).setOnClickListener {
            // 작성한 폴더 이름을 반영한 새폴더를 만들어준다.
            val name = mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_set_name_et).text.toString()

            // 팝업 윈도우 종료
            mPopupWindow.dismiss()

            // 작성한 폴더 이름을 setFolderIcon 함수로 넘겨준다.
            setFolderIcon(name)
        }
    }

    // 새폴더 아이콘 설정
    @SuppressLint("InflateParams")
    private fun setFolderIcon(name: String) {
        // 팝업 윈도우 사이즈를 잘못 맞추면 아이템들이 안 뜨므로 하드 코딩으로 사이즈 조정해주기
        // 아이콘 16개 (기본)
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.6f).toInt()

        // 아이콘 바꾸기 팝업 윈도우
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_icon, null)
        mPopupWindow = PopupWindow(popupView, width, height)

        mPopupWindow.animationStyle = 0        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderContent.myFolderBackgroundView.visibility = View.VISIBLE    // 뒷배경 흐려지게
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // RecyclerView 초기화
        iconRVAdapter = ChangeIconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter = iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object: ChangeIconRVAdapter.MyItemClickListener {
            // 아이콘을 하나 선택했을 경우
            override fun onIconClick(itemIconBinding: ItemIconBinding, iconPosition: Int) {
                val selectedIcon = iconList[iconPosition]

                val iconBitmap = BitmapFactory.decodeResource(resources, selectedIcon.iconImage)
                val baos = ByteArrayOutputStream()
                iconBitmap.compress(Bitmap.CompressFormat.PNG, 70, baos)
                val iconBitmapAsByte = baos.toByteArray()
                val iconBitmapAsString = Base64.encodeToString(iconBitmapAsByte, Base64.DEFAULT)

                // Bitmap bigPictureBitmap  = BitmapFactory.decodeResource(context.getResources(), R.drawable.i_hero);

                val newFolderIdx = folderList.size

                // 폴더 이름 바꾸기
                folderService.changeFolderName(this@MyFolderActivity, userID, folderList[newFolderIdx].folderIdx, FolderList(newFolderIdx, name, iconBitmapAsString))

                // 폴더 아이콘 바꾸기
                folderService.changeFolderIcon(this@MyFolderActivity, userID, folderList[newFolderIdx].folderIdx, FolderList(newFolderIdx, name, iconBitmapAsString))

                // 팝업 윈도우 종료
                mPopupWindow.dismiss()
            }
        })
    }

    // 디바이스 크기에 사이즈를 맞추기 위한 함수
    private fun WindowManager.currentWindowMetricsPointCompat(): Point {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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

    inner class PopupWindowDismissListener(): PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.myFolderContent.myFolderBackgroundView.visibility = View.INVISIBLE
        }
    }

    override fun onFolderAPISuccess() {
        Log.d(tag, "onFolderAPISuccess()")
    }

    override fun onFolderAPIFailure(code: Int, message: String) {
        Log.d(tag, "onFolderAPIFailure()/code: $code, message: $message")
    }

    override fun onFolderListSuccess(folderList: ArrayList<FolderList>) {
        Log.d(tag, "onFolderListSuccess()/folderList: $folderList")
        this.folderList.clear()
        this.folderList.addAll(folderList)
        initRecyclerView()
    }

    override fun onFolderListFailure(code: Int, message: String) {
        Log.d(tag, "onFolderListFailure()/code: $code, message: $message")
    }
}