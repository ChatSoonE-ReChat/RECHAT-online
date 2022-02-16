package com.chat_soon_e.re_chat.ui

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
<<<<<<< HEAD
import com.chat_soon_e.re_chat.ApplicationClass.Companion.ACTIVE
import com.chat_soon_e.re_chat.data.entities.Chat
import com.chat_soon_e.re_chat.data.local.AppDatabase
=======
import com.chat_soon_e.re_chat.data.remote.chat.Chat
import com.chat_soon_e.re_chat.data.remote.chat.ChatService
import com.chat_soon_e.re_chat.ui.view.ChatView
>>>>>>> 0ea4d26316c344ef4fca88aabaf035355aaecd50
import com.chat_soon_e.re_chat.utils.getID
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.M)
class MyNotificationListener: NotificationListenerService(), ChatView {
    private lateinit var chatService: ChatService
    private var userID = getID()
    private val tag = "MYNOTIFICATION"

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(tag, "onListenerConnected()")
        Log.d(tag, "userID: $userID")
    }

    // 새로운 알림 올 때마다 발생한다.
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // 이 밑에 부분은 서버와 어떻게 통신하는지 잘 모르겠어서 일단 주석 처리
//        if(userID.toInt() == -1) {
//            if(AppDatabase.getInstance(this)!!.userDao().getUsers() == null)
//                Log.d(tag, "login error, 잘못된 접근")
//            else{
//                val data=AppDatabase.getInstance(this)!!.userDao().getUsers()
//
//                userID = if(data == null) {
//                    saveID(-1L) //오류 났을시 임시로 해주는 것
//                    getID()
//                } else {
//                    saveID(data[0].kakaoUserIdx)
//                    getID()
//                }
//            }
//        }

        super.onNotificationPosted(sbn)
        val notification: Notification = sbn.notification
        val packageName: String = sbn.packageName
        chatService = ChatService()

        if(packageName != null && packageName == "com.kakao.talk") {
<<<<<<< HEAD
=======
            // 데이터베이스 연결
>>>>>>> 0ea4d26316c344ef4fca88aabaf035355aaecd50
            val extras = sbn.notification.extras
            val name = extras.getString(Notification.EXTRA_TITLE)   // 발신자
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)  // 내용
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)   // 그룹톡일 경우

            // postTime, mill -> Date 변환
            // server에 저장할 때 ISO 8601 형식의 Date를 보내줘야 한다.
            val millisecond = sbn.postTime
            val date = Date(millisecond)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.KOREAN)
            val dateAsString = simpleDateFormat.format(date)
            val dateAsDate = simpleDateFormat.parse(dateAsString)

            Log.d(tag, "millisecond: $millisecond")     // 1644656599649 형식
            Log.d(tag, "date: $date")                   // Sat Feb 12 18:03:19 GMT+09:00 2022 형식 (이렇게 넣으면 서버에서 null 처리)
            Log.d(tag, "dateAsString: $dateAsString")   // 2022-02-12T18:04:28+09:00 형식 (이렇게 넣어야 서버에서 제대로 날짜를 인식, 따라서 String 형식으로 보내줘야 한다.)
            Log.d(tag, "dateAsDate: $dateAsDate")       // Sat Feb 12 18:07:14 GMT+09:00 2022 형식 (이렇게 넣으면 서버에서 null 처리)

            // Icon, cache에 png 전환 후 저장
            val largeIcon: Icon? = notification.getLargeIcon()

            // 알림 메세지(264개의 메세지 등) 제외 대화 내용 DB 저장
<<<<<<< HEAD
            // 음악 메세지(id==2016) 차단
            if(name!=null && sbn.id!=2016){
                if

                    // 이미 있던 유저라면
                    var blocked: String?
                    if(subText == null) {
                        // 갠톡이라면
                        if(database.otherUserDao().checkOneBlock(userID, otherUser.otherUserIdx) == null) {
                            database.chatDao().insert(chat)

                            // Server API: 채팅 추가하기
//                            val remoteChat = com.chat_soon_e.re_chat.data.remote.chat.Chat(otherUser.nickname, null, null, text.toString(), dateAsString)
//                            val chatService = ChatService()
//                            chatService.addChat(this, userID, remoteChat)
                        }
                    } else{
                        // 단톡이라면
                        if(database.otherUserDao().checkOrgBlock(userID, subText.toString()) == null) {
                            database.chatDao().insert(chat)
                            // Server API: 채팅 추가하기
//                            val remoteChat = com.chat_soon_e.re_chat.data.remote.chat.Chat(otherUser.nickname, subText.toString(), null, text.toString(), dateAsString)
//                            val chatService = ChatService()
//                            chatService.addChat(this, userID, remoteChat)
                        }
                    }
                }
                else{
                    // 새로운 유저라면 일단 그냥 넣기, 차단이 됬는지 안됬는지 어떻게 알아? 새로운 유저인데!
                    var fileName = ""
                    val other: OtherUser

                    if(largeIcon != null) {
                        fileName = saveCache(convertIconToBitmap(largeIcon), name + "_" + millisecond.toString())
                        database.otherUserDao().insert(OtherUser(name.toString(), fileName, ACTIVE, userID))
                        other = database.otherUserDao().getOtherUserByNameId(name.toString(), userID)!!
                        database.chatDao().insert(Chat(other.otherUserIdx, subText.toString(),text.toString(), dateAsString, -1, ACTIVE))


//                        if(subText == null) {
//                            // 단톡이 아닌 경우 groupName == null
//                            // Server API: 채팅 추가하기
//                            val remoteChat = com.chat_soon_e.re_chat.data.remote.chat.Chat(other.nickname, null, fileName, text.toString(), dateAsString)
//                            val chatService = ChatService()
//                            chatService.addChat(this, userID, remoteChat)
//                        } else {
//                            // 단톡인 경우
//                            // Server API: 채팅 추가하기
//                            val remoteChat = com.chat_soon_e.re_chat.data.remote.chat.Chat(other.nickname, subText.toString(), fileName, text.toString(), dateAsString)
//                            val chatService = ChatService()
//                            chatService.addChat(this, userID, remoteChat)
//                        }
                    } else {
                        database.otherUserDao().insert(OtherUser(name.toString(), null, ACTIVE, userID))
                        other = database.otherUserDao().getOtherUserByNameId(name.toString(), userID)!!
                        database.chatDao().insert(Chat(other.otherUserIdx, subText.toString(),text.toString(), dateAsString, -1, ACTIVE))


                        if(subText == null) {
                            // 단톡이 아닌 경우 groupName == null
                            // Server API: 채팅 추가하기
                            val remoteChat = com.chat_soon_e.re_chat.data.remote.chat.Chat(other.nickname, null, fileName, text.toString(), dateAsString)
                            val chatService = ChatService()
                            chatService.addChat(this, userID, remoteChat)
                        } else {
                            // 단톡인 경우
                            // Server API: 채팅 추가하기
                            val remoteChat = com.chat_soon_e.re_chat.data.remote.chat.Chat(other.nickname, subText.toString(), fileName, text.toString(), dateAsString)
                            val chatService = ChatService()
                            chatService.addChat(this, userID, remoteChat)
                        }
                    }
=======
            // 음악 메세지(id == 2016) 차단
            if(name!=null && sbn.id != 2016) {
                val fileName = if(largeIcon != null) saveCache(convertIconToBitmap(largeIcon), name + "_" + millisecond.toString()) else null

                // 이미 있던 유저인지, 새로운 유저인지는 서버측에서 알아서 처리해줄 것
                // 갠톡인지, 단톡인지만 구분해주자.
                if(subText == null) {
                    // 갠톡이라면
                    val remoteChat = Chat(name, null, fileName, text.toString(), dateAsString)
                    chatService.addChat(this, userID, remoteChat)
                } else {
                    // 단톡이라면
                    val remoteChat = Chat(name, subText.toString(), fileName, text.toString(), dateAsString)
                    chatService.addChat(this, userID, remoteChat)
>>>>>>> 0ea4d26316c344ef4fca88aabaf035355aaecd50
                }
            }

            Log.d(
                "Notif", "onNotificationPosted ~ " +
                        " id: " + sbn.id +
                        " name: " + name +
                        " text : " + text
                        + "subtext: "+ subText+
                        "postTime: "+date.toString()
            )
        }

    }

    //Icon을 Bitmap으로 전환
    private fun convertIconToBitmap(icon: Icon): Bitmap {
        val drawable = icon.loadDrawable(this)
        return drawable.toBitmap()
    }

    // Bitmap을 캐시 디렉토리에 저장, 파일 이름 저장
    private fun saveCache(bitmap:Bitmap, name:String):String{
        val storage=cacheDir//cacheDir 경로
        val tempFile= File(storage, name)
        try{
            tempFile.createNewFile()
            val out= FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()

        }catch (e: FileNotFoundException){
            Log.e("MyTag","FileNotFoundException : " + e.message);
        }catch (e: IOException) {
            Log.e("MyTag", "IOException : " + e.message)
        }
        return name
    }

    // 채팅 넣어주는 걸 성공한 경우
    override fun onChatSuccess() {
        Log.d(tag, "onChatSuccess()")
    }

    // 실패한 경우
   override fun onChatFailure(code: Int, message: String) {
        when (code) {
            2100 -> Log.d(tag, message)
            2202 -> Log.d(tag, message)
            else -> Log.d(tag, message)
        }
    }
}