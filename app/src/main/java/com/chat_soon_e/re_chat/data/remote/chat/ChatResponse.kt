package com.chat_soon_e.re_chat.data.remote.chat

import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.*

// 자바의 데이터 클래스 멤버변수 이름은 camelCase 사용
// default
data class ChatResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: JsonArray?
)

//채팅 추가하기
data class Chat(
    @SerializedName("nickname") var chatName: String,
    @SerializedName("groupName") var groupName: String?,
    @SerializedName("profileImgUrl") var profileImgUrl: String?,
    @SerializedName("message") var message: String,
    @SerializedName("postTime") var postTime: String
)

// 차단된 톡방 목록 가져오기
data class BlockedChatList(
    @SerializedName("blocked_name") val blockedName: String,
    @SerializedName("blocked_profileImg") val blockedProfileImg: String?,
    @SerializedName("groupName") val groupName: String?,
    @SerializedName("status") val status: String
)

// 전체 채팅목록 가져오기 (메인화면), 채팅 가져오기
data class ChatList(
    @SerializedName("chatIdx") var chatIdx: Int,
    @SerializedName("chat_name") var chatName: String,
    @SerializedName("profile_image") var profileImg: String?,
    @SerializedName("latest_time") var latestTime: String?,
    @SerializedName("latest_message") var latestMessage: String,
    @SerializedName("groupName") var groupName: String?,
): Serializable {
    @PrimaryKey(autoGenerate = true) var id: Int? = 0
    @Ignore
    var viewType: Int = 0
    @Ignore var isChecked: Boolean = false
}

object ChatListViewType {
    const val DEFAULT = 0
    const val CHOOSE = 1
}

// 폴더 안의 채팅 가져오기 -> FolderContent 활용
data class FolderChatResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code:Int,
    val message: String,
    val result:JsonArray?
)

// 폴더 안의 채팅 가져오기
data class FolderContent(
    @SerializedName("chatIdx") val chatIdx: Int,
    @SerializedName("folderName") val folderName: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImgUrl") val profileImgUrl: String?,
    @SerializedName("message") val message: String,
    @SerializedName("post_time") val postTime: String
)