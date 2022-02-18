package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.chat_soon_e.re_chat.ApplicationClass.Companion.loadBitmap
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.data.remote.chat.Chat
import com.chat_soon_e.re_chat.databinding.ItemChatListChooseBinding
import com.chat_soon_e.re_chat.databinding.ItemChatListDefaultBinding
import com.chat_soon_e.re_chat.data.remote.chat.ChatList
import com.chat_soon_e.re_chat.data.remote.chat.ChatListViewType
import com.chat_soon_e.re_chat.data.remote.chat.ChatService
import com.chat_soon_e.re_chat.ui.view.ChatView
import com.chat_soon_e.re_chat.utils.getID
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainRVAdapter(private val context: Context, private val mItemClickListener: MyItemClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    ChatView {
    private lateinit var chatService: ChatService

    private var selectedItemList: SparseBooleanArray = SparseBooleanArray(0)
    private val userID = getID()
    private val tag = "RV/MAIN"

    var chatList = ArrayList<ChatList>()

    // 클릭 인터페이스
    interface MyItemClickListener {
        fun onDefaultChatClick(view: View, position: Int, chat: ChatList)
        fun onChooseChatClick(view: View, position: Int)
    }

    // 뷰홀더를 생성해줘야 할 때 호출되는 함수로, 아이템 뷰 객체를 만들어서 뷰 홀더에 던져준다.
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ChatListViewType.CHOOSE -> {
                ChooseViewHolder(
                    ItemChatListChooseBinding.inflate(
                        LayoutInflater.from(viewGroup.context), viewGroup, false
                    ),
                    mItemClickListener = mItemClickListener
                )
            }
            else -> {
                DefaultViewHolder(
                    ItemChatListDefaultBinding.inflate(
                        LayoutInflater.from(viewGroup.context), viewGroup, false
                    )
                )
            }
        }
    }

    // 뷰홀더에 데이터 바인딩을 해줘야 할 때마다 호출되는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(chatList[position].viewType) {
            ChatListViewType.CHOOSE -> {
                (holder as ChooseViewHolder).bind(chatList[position])
                (holder as ChooseViewHolder).itemView.isSelected = isItemSelected(position)
                Log.d(tag, "onBindViewHolder()")
            }
            else -> {
                (holder as DefaultViewHolder).bind(chatList[position])
                (holder as DefaultViewHolder).itemView.isSelected = isItemSelected(position)
                Log.d(tag, "onBindViewHolder()")
            }
        }
    }

    // selectedItemList 삭제
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("NotifyDataSetChanged")
    fun removeSelectedItemList() {
        chatService= ChatService()
        // checked 안 된 것들로 교체해서 Activity에는 선택 안 된 것들만 남게 한다.
        val selectedList = chatList.filter{ chatlist-> chatlist.isChecked as Boolean }
        // 선택한 채팅 삭제하기, 서버에서
        for(i in selectedList) {
            chatService.deleteChatList(this, userID, i.chatIdx, i.groupName)
        }
        notifyDataSetChanged()
    }

    // selectedItemList 차단
    @SuppressLint("NotifyDataSetChanged")
    fun blockSelectedItemList() {
        chatService=ChatService()
        // checked 안 된 것들로 교체해서 Activity에는 선택 안 된 것들만 남게 한다.
        val selectedList = chatList.filter{ chatlist-> chatlist.isChecked as Boolean }
        // 선택한 채팅목록/유저 차단하기
        for(i in selectedList) {
            chatService.block(this, userID, i.chatName, i.groupName)
        }
        notifyDataSetChanged()
    }

    // selectedItemList 초기화
    @SuppressLint("NotifyDataSetChanged")
    fun clearSelectedItemList() {
        selectedItemList.clear()
        notifyDataSetChanged()
    }

    // will toggle the selection of items
    private fun toggleItemSelected(view: View?, position: Int) {
        if(selectedItemList.get(position, false)) {
            selectedItemList.delete(position)
        } else {
            selectedItemList.put(position, true)
        }

        // 선택된 itmelist들의 로그
        Log.d(tag, "selectedItemList: $selectedItemList")
        notifyItemChanged(position)
    }

    fun setChecked(position: Int) {
        chatList[position].isChecked = !chatList[position].isChecked
        notifyItemChanged(position)
    }

    // 아이템 뷰가 선택되었는지를 알려주는 함수
    private fun isItemSelected(position: Int): Boolean {
        return selectedItemList.get(position, false)
    }

    // 데이터셋의 크기를 알려주는 함수
    override fun getItemCount(): Int {
        Log.d(tag, "chatList.size: ${chatList.size}")
        return this.chatList.size
    }

    // 직접 설정한 뷰타입으로 설정되게 만든다.
    override fun getItemViewType(position: Int): Int = chatList[position].viewType!!

    // 뷰타입 설정
    @SuppressLint("NotifyDataSetChanged")
    fun setViewType(currentMode: Int) {
        val newChatList = ArrayList<ChatList>()
        for(i in 0 until chatList.size) {
            if(currentMode == 0) { // 일반 모드 (= 이동 모드)
                chatList[i].viewType = ChatListViewType.DEFAULT
            } else { // 선택 모드
                chatList[i].viewType = ChatListViewType.CHOOSE
            }
            newChatList.add(chatList[i])
        }
        this.chatList = newChatList
        notifyDataSetChanged()
    }

    // Add Data
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(chats: List<ChatList>){
        chatList.clear()
        chatList.addAll(chats as ArrayList)
        Log.d(tag, "chatList in MainRVAdapter: $chatList")
        notifyDataSetChanged()
    }

    //선택된 chatIdx를 가져온다.
    fun getSelectedItem():ArrayList<ChatList>{
        //chatlist에서 checked 된 list들의 chatIdx를 저장하고 가져온다
        val TG="removeList"
        val selectedList = chatList.filter { chatlist-> chatlist.isChecked}
        return selectedList as ArrayList<ChatList>
    }

    // 디폴트 뷰홀더
    inner class DefaultViewHolder(private val binding: ItemChatListDefaultBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.itemChatListDefaultLayout.setOnClickListener {
                toggleItemSelected(null, position = bindingAdapterPosition)
                mItemClickListener.onDefaultChatClick(itemView, position = bindingAdapterPosition, chatList[bindingAdapterPosition])
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(chat: ChatList) {
            if(chat.profileImg != null && chat.profileImg!!.isNotEmpty() && chat.groupName != null ) binding.itemChatListProfileIv.setImageBitmap(loadBitmap(chat.profileImg!!, context))
            else if(chat.groupName !=null || chat.groupName!="null") binding.itemChatListProfileIv.setImageResource(R.drawable.ic_profile_black_no_circle)

            binding.itemChatListNameTv.text = chat.chatName
            binding.itemChatListContentTv.text = chat.latestMessage
            binding.itemChatListDateTimeTv.text = chat.latestTime?.let { convertDate(it) }

            Log.d(tag, "bind()/isNew: ${chat.isNew}")

            if(chat.isNew == 1) { // 새로 온 경우 NEW 표시
                binding.itemChatListNewCv.visibility = View.VISIBLE
            } else {
                binding.itemChatListNewCv.visibility = View.INVISIBLE
            }
        }
    }

    // 선택 모드 뷰홀더
    inner class ChooseViewHolder(private val binding: ItemChatListChooseBinding, private val mItemClickListener: MyItemClickListener)
        : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.itemChatListChooseLayout.setOnClickListener {
                toggleItemSelected(itemView, position = bindingAdapterPosition)
                mItemClickListener.onChooseChatClick(itemView, position = bindingAdapterPosition)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(chat: ChatList) {
            if(chat.profileImg != null && chat.profileImg!!.isNotEmpty() && chat.groupName != null ) binding.itemChatListProfileIv.setImageBitmap(loadBitmap(chat.profileImg!!, context))
            else if(chat.groupName !=null || chat.groupName!="null") binding.itemChatListProfileIv.setImageResource(R.drawable.ic_profile_black_no_circle)
            binding.itemChatListNameTv.text = chat.chatName
            binding.itemChatListContentTv.text = chat.latestMessage
            binding.itemChatListDateTimeTv.text = chat.latestTime?.let { convertDate(it) }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertDate(date :String): String {
        val str: String
        val today = Calendar.getInstance()
        Log.d(tag, "date: $date")

        // 2022-02-13T02:35:37+09:00
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val dateAsDate = simpleDateFormat.parse(date)
        Log.d(tag, "dateAsDate: $dateAsDate")

        val diffDay = (today.time.time - dateAsDate!!.time) / (60 * 60 * 24 * 1000)

        str = if(diffDay < 0) {
            // 오늘인 경우
            val sdf = SimpleDateFormat("a h:m")
            sdf.format(dateAsDate).toString()
        } else {
            val time = SimpleDateFormat("M월 d일")
            time.format(dateAsDate).toString()
//            binding.itemChatListDateTimeTv.text = chat.postTime
//            binding.itemChatListDateTimeTv.text = dateToString(chat.postTime)
        }

        return str
    }

    override fun onChatSuccess() {
        Log.d(tag, "onChatSuccess()")
    }

    override fun onChatFailure(code: Int, message: String) {
        Log.d(tag, "onChatFailure()/code: $code, message: $message")
    }
}