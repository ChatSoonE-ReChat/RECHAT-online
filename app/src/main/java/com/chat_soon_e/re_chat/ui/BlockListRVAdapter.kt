package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.chat_soon_e.re_chat.ApplicationClass
import com.chat_soon_e.re_chat.ApplicationClass.Companion.loadBitmap
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.data.remote.chat.BlockedChatList
import com.chat_soon_e.re_chat.databinding.ItemBlockBinding
import com.chat_soon_e.re_chat.databinding.ItemChatBinding

class BlockListRVAdapter(
    private val mContext: BlockListActivity,
    private val blockList: ArrayList<BlockedChatList>,
    private val param: BlockListRVAdapter.MyClickListener
): RecyclerView.Adapter<BlockListRVAdapter.ViewHolder>() {
    var chatList = ArrayList<BlockedChatList>()
    interface MyClickListener {
        fun onRemoveChat(blockList:BlockedChatList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockListRVAdapter.ViewHolder {
        val binding: ItemBlockBinding = ItemBlockBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(blockList[position])
    }

    override fun getItemCount(): Int = blockList.size
    //AddData
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(block: List<BlockedChatList>){//차단 목록 업데이트
        blockList.clear()
        blockList.addAll(block as ArrayList)

        notifyDataSetChanged()
    }

    private fun removeBlock(position: Int) {//roomdb지우고 ui의 리스트틀 삭제한다.
        blockList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount);
    }

    // 디폴트 뷰홀더
    inner class ViewHolder(val binding: ItemBlockBinding): RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(block: BlockedChatList) {
            binding.itemBlockCancelIv.setOnClickListener {
                param.onRemoveChat(blockList[position])
                removeBlock(position)
            }
            if(block.groupName!=null&&block.groupName!="null")
                binding.itemBlockNameTv.text=block.groupName
            else
                binding.itemBlockNameTv.text=block.blockedName

            if(block.blockedProfileImg!=null&&block.blockedProfileImg!="null")
                binding.itemBlockProfileIv.setImageBitmap(loadBitmap(block.blockedProfileImg, mContext))

        }
    }
}