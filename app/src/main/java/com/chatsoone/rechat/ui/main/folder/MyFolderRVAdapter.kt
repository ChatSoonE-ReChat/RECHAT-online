package com.chatsoone.rechat.ui.main.folder

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.R
import com.chatsoone.rechat.data.remote.FolderList
import com.chatsoone.rechat.databinding.ItemMyFolderBinding

class MyFolderRVAdapter(private val fragment: MyFolderFragment, private val mContext: Context) :
    RecyclerView.Adapter<MyFolderRVAdapter.ViewHolder>() {
    private lateinit var binding: ItemMyFolderBinding
    private lateinit var mPopupMenu: PopupMenu
    private val folderList = ArrayList<FolderList>()

    // 클릭 인터페이스
    interface MyItemClickListener {
        fun onRemoveFolder(idx: Int)
        fun onHideFolder(idx: Int)
        fun onFolderNameLongClick(binding: ItemMyFolderBinding, position: Int, folderIdx: Int)
        fun onFolderClick(view: View, position: Int)
        fun onFolderLongClick(popup: PopupMenu)
    }

    // 리스너 객체를 저장하는 변수
    private lateinit var mItemClickListener: MyItemClickListener

    // 리스너 객체를 외부에서 전달받는 함수
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    // 뷰홀더를 생성해줘야 할 때 호출되는 함수
    // 아이템 뷰 객체를 만들어서 뷰홀더에 던져준다.
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMyFolderBinding =
            ItemMyFolderBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    // 뷰홀더에 데이터 바인딩을 해줘야 할 때마다 호출되는 함수
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(folderList[position])
        binding = holder.binding

        // 폴더 이름 롱클릭 시 이름 변경할 수 있도록
        holder.binding.itemMyFolderTv.setOnLongClickListener {
            mItemClickListener.onFolderNameLongClick(
                holder.binding,
                position,
                folderList[position].folderIdx
            )
            return@setOnLongClickListener false
        }

        // 폴더 클릭 시 해당 폴더로 이동할 수 있도록
        holder.binding.itemMyFolderIv.setOnClickListener {
            mItemClickListener.onFolderClick(holder.binding.itemMyFolderIv, position)
        }

        // 폴더 아이템 롱클릭 시 팝업 메뉴 뜨도록
        holder.binding.itemMyFolderIv.setOnLongClickListener {
            // 팝업 메뉴: 이름 바꾸기, 아이콘 바꾸기, 삭제하기, 숨기기
            mPopupMenu = PopupMenu(
                fragment.context,
                holder.itemView,
                Gravity.START,
                0,
                R.style.MyFolderOptionPopupMenuTheme
            )
            mPopupMenu.menuInflater.inflate(R.menu.menu_folder_option, mPopupMenu.menu)
            mPopupMenu.setOnMenuItemClickListener { item ->
                when (item?.itemId) {
                    R.id.popup_folder_edit_menu_1 -> {
                        // 이름 바꾸기
                        fragment.changeFolderName(
                            holder.binding,
                            position,
                            folderList[position].folderIdx
                        )
                    }

                    R.id.popup_folder_edit_menu_2 -> {
                        // 아이콘 바꾸기
                        fragment.changeIcon(
                            holder.binding,
                            position,
                            folderList[position].folderIdx
                        )
                    }

                    R.id.popup_folder_edit_menu_3 -> {
                        // 삭제하기
                        mItemClickListener.onRemoveFolder(folderList[position].folderIdx)
                        removeFolder(position)
                    }

                    R.id.popup_folder_edit_menu_4 -> {
                        // 숨기기
                        mItemClickListener.onHideFolder(folderList[position].folderIdx)
                        removeFolder(position)
                    }
                }
                false
            }
            mItemClickListener.onFolderLongClick(mPopupMenu)
            return@setOnLongClickListener false
        }
    }

    // 데이터셋의 크기 반환
    override fun getItemCount(): Int = folderList.size

    // 데이터 연결
    @SuppressLint("NotifyDataSetChanged")
    fun addFolderList(folderList: ArrayList<FolderList>) {
        this.folderList.clear()
        this.folderList.addAll(folderList)
        notifyDataSetChanged()
    }

    // index를 바꿔줘야 한다.
    // 선택된 index를 지우고, 끝에 추가해준다.
    private fun removeFolder(position: Int) {
        folderList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount);
    }

    fun getSelectedFolder(position: Int): FolderList {
        return folderList[position]
    }

    // 뷰홀더
    inner class ViewHolder(val binding: ItemMyFolderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(folder: FolderList) {
            binding.itemMyFolderTv.text = folder.folderName

            if (folder.folderImg != null) {
                val folderImgID = getFolderImgResource(folder.folderImg)
                if (folderImgID != 0) binding.itemMyFolderIv.setImageResource(folderImgID)
                else binding.itemMyFolderIv.setImageResource(R.drawable.folder_bear)
            } else binding.itemMyFolderIv.setImageResource(R.drawable.folder_bear)
        }
    }

    private fun getFolderImgResource(folderImgString: String): Int {
        // res/drawable/파일명.png
        val folderImgStringArray = folderImgString.split("/", ".")
        val folderImgName = folderImgStringArray[2]

        return mContext.resources.getIdentifier(folderImgName, "drawable", mContext.packageName)
    }
}
