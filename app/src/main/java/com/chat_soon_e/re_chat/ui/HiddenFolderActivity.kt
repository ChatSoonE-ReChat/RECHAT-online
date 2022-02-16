package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Insets
import android.graphics.Point
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.chat_soon_e.re_chat.R
<<<<<<< HEAD
import com.chat_soon_e.re_chat.data.entities.Icon
=======
import com.chat_soon_e.re_chat.data.local.Icon
>>>>>>> 0ea4d26316c344ef4fca88aabaf035355aaecd50
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.databinding.ActivityHiddenFolderBinding
import com.chat_soon_e.re_chat.databinding.ItemHiddenFolderBinding
import com.chat_soon_e.re_chat.databinding.ItemIconBinding
<<<<<<< HEAD
=======
import com.chat_soon_e.re_chat.ui.view.FolderAPIView
import com.chat_soon_e.re_chat.ui.view.HiddenFolderListView
>>>>>>> 0ea4d26316c344ef4fca88aabaf035355aaecd50
import com.chat_soon_e.re_chat.utils.getID
import com.google.gson.Gson
import java.io.ByteArrayOutputStream

class HiddenFolderActivity: BaseActivity<ActivityHiddenFolderBinding>(ActivityHiddenFolderBinding::inflate),
    HiddenFolderListView, FolderAPIView {
    private lateinit var database: AppDatabase
    private lateinit var hiddenFolderRVAdapter: HiddenFolderRVAdapter
    private lateinit var iconRVAdapter: ChangeIconRVAdapter
    private lateinit var mPopupWindow: PopupWindow
    private lateinit var folderService: FolderService

    // 기존 Folder RoomDB 대신 서버로부터 받은 HiddenFolderList data class를 사용하도록 했습니다.
    private var hiddenFolderList = ArrayList<HiddenFolderList>()
    private val tag = "ACT/HIDDEN-FOLDER"
    private val userID = getID()
    private var iconList = ArrayList<Icon>()

    override fun initAfterBinding() {
        Log.d(tag, "user id: $userID")
        database = AppDatabase.getInstance(this)!!
        folderService = FolderService()

        initFolder()
    }

    // 폴더 리스트 초기화
    private fun initFolder() {
        // RecyclerView 초기화
        hiddenFolderRVAdapter = HiddenFolderRVAdapter(this)
        binding.hiddenFolderListRecyclerView.adapter = hiddenFolderRVAdapter

        // 서버로부터 숨김 폴더 리스트를 받아오는 부분을 추가해야 합니다.
        folderService.getHiddenFolderList(this, userID)
//        database.folderDao().getHiddenFolder(userID).observe(this){
//            hiddenFolderRVAdapter.addFolderList(it as ArrayList<Folder>)
//        }

        hiddenFolderRVAdapter.setMyItemClickListener(object: HiddenFolderRVAdapter.MyItemClickListener {
            // 숨김 폴더 다시 해제하기
            override fun onShowFolder(folderIdx: Int) {
                folderService.unhideFolder(this@HiddenFolderActivity, userID, folderIdx)
            }

            // 폴더 삭제하기
            override fun onRemoveFolder(folderIdx: Int) {
                folderService.deleteFolder(this@HiddenFolderActivity, userID, folderIdx)
            }

            // 폴더 클릭 시 이동
            override fun onFolderClick(view: View, position: Int) {
                // 선택한 폴더의 포지션을 가져와서
                val selectedFolder = hiddenFolderRVAdapter.getSelectedFolder(position)
                val selectedFolderJson = Gson().toJson(selectedFolder)

                // FolderContentActivity로 해당 폴더 정보 보내기
                val intent = Intent(this@HiddenFolderActivity, FolderContentActivity::class.java)
                intent.putExtra("folderData", selectedFolderJson)
                startActivity(intent)
            }

            // 폴더 롱클릭 시 팝업 메뉴
            override fun onFolderLongClick(popupMenu: PopupMenu) {
                popupMenu.show()
            }

            // 폴더 이름 롱클릭 시 이름 변경
            override fun onFolderNameLongClick(itemHiddenFolderBinding: ItemHiddenFolderBinding, folderIdx: Int) {
                changeFolderName(itemHiddenFolderBinding, folderIdx)
            }
        })
    }

    // 이름 바꾸기 팝업 윈도우
    @SuppressLint("InflateParams")
    fun changeFolderName(itemHiddenFolderBinding: ItemHiddenFolderBinding, folderIdx:Int) {
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_name, null)

        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)
        mPopupWindow.animationStyle = 0
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.hiddenFolderBackgroundView.visibility = View.VISIBLE

        // 기존 폴더 이름을 팝업 윈도우의 EditText에 넘겨준다.
        var text: String = itemHiddenFolderBinding.itemHiddenFolderTv.text.toString()
        mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).setText(text)

        database = AppDatabase.getInstance(this@HiddenFolderActivity)!!

        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_change_name_button).setOnClickListener {
            // 바뀐 폴더 이름을 뷰와 RoomDB에 각각 적용해준다.
            text = mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).text.toString()
            itemHiddenFolderBinding.itemHiddenFolderTv.text = text

            // 폴더 이름 바꾸기
            folderService.changeFolderName(this@HiddenFolderActivity, userID, folderIdx, text)
            mPopupWindow.dismiss()  // 팝업 윈도우 종료
        }
    }

    // 아이콘 바꾸기 팝업 윈도우
    @SuppressLint("InflateParams")
    fun changeIcon(itemHiddenFolderBinding: ItemHiddenFolderBinding, folderIdx:Int) {
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.6f).toInt()
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_icon, null)

        mPopupWindow = PopupWindow(popupView, width, height)
        mPopupWindow.animationStyle = 0
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.hiddenFolderBackgroundView.visibility = View.VISIBLE

        // 데이터베이스로부터 아이콘 리스트 불러와 연결해주기
        database = AppDatabase.getInstance(this@HiddenFolderActivity)!!
        iconList = database.iconDao().getIconList() as ArrayList
        iconRVAdapter = ChangeIconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter = iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object: ChangeIconRVAdapter.MyItemClickListener {
            // 아이콘을 선택했을 경우
            override fun onIconClick(itemIconBinding: ItemIconBinding, iconPosition: Int) {//icon 포지션
                // 선택한 아이콘으로 폴더 이미지 변경
                val selectedIcon = iconList[iconPosition]
                itemHiddenFolderBinding.itemHiddenFolderIv.setImageResource(selectedIcon.iconImage)

                val iconBitmap = BitmapFactory.decodeResource(resources, selectedIcon.iconImage)
                val baos = ByteArrayOutputStream()
                iconBitmap.compress(Bitmap.CompressFormat.PNG, 70, baos)

                val iconBitmapAsByte = baos.toByteArray()
                val iconBitmapAsString = Base64.encodeToString(iconBitmapAsByte, Base64.DEFAULT)

                // 폴더 아이콘 바꾸기
                folderService.changeFolderIcon(this@HiddenFolderActivity, userID, folderIdx, iconBitmapAsString)
                mPopupWindow.dismiss()  // 팝업 윈도우 종료
            }
        })
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

    // 팝업 윈도우 종료 시
    inner class PopupWindowDismissListener(): PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.hiddenFolderBackgroundView.visibility = View.INVISIBLE
        }
    }

    // 뒤로 가기 버튼 클릭 시
    override fun onBackPressed() {
        finish()
    }

    // 숨김 폴더목록 가져오기 성공했을 때
    override fun onHiddenFolderListSuccess(hiddenFolderList: ArrayList<HiddenFolderList>) {
        Log.d(tag, "onHiddenFolderListSuccesS()")
        this.hiddenFolderList = hiddenFolderList
    }

    // 숨김 폴더목록 가져오기 실패했을 때
    override fun onHiddenFolderListFailure(code: Int, message: String) {
        Log.d(tag, "code: $code, message: $message")

        // 사용자에게 서버와의 네트워크가 불안정하다는 것을 알려주는 팝업창 같은 걸 띄우는 게 어떨까?
    }

    // folder API 성공했을 때
    override fun onFolderAPISuccess() {
        Log.d(tag, "onFolderAPISuccess()")
    }

    // folder API 실패했을 때
    override fun onFolderAPIFailure(code: Int, message: String) {
        Log.d(tag, "code: $code, messgae: $message")

        // 사용자에게 서버와의 네트워크가 불안정하다는 것을 알려주는 팝업창 같은 걸 띄우는 게 어떨까?
    }
}