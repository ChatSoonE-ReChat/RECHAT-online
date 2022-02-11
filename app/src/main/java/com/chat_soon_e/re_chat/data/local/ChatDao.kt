package com.chat_soon_e.re_chat.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.chat_soon_e.re_chat.data.entities.Chat
import com.chat_soon_e.re_chat.data.entities.ChatList

@Dao
interface ChatDao {
    @Insert
    fun insert(chat: Chat)

    @Update
    fun update(chat: Chat)

    @Delete
    fun delete(chat: Chat)

    //하나의 톡 삭제
    @Query("DELETE FROM ChatTable WHERE chatIdx = :chatIdx")
    fun deleteByChatIdx(chatIdx: Long)

    //갠톡 전체 삭제
    @Query("DELETE FROM ChatTable WHERE OtherUserIdx= :otherUserIdx AND groupName is 'null'")
    fun deleteOneChat(otherUserIdx: Long)

    //단톡 전체 삭제
    @Query("DELETE FROM ChatTable WHERE groupName = :groupName AND otherUserIdx IN (SELECT CD.otherUserIdx FROM (SELECT C.otherUserIdx fROM ChatTable C INNER JOIN OtherUserTable OU ON C.otherUserIdx=OU.otherUserIdx WHERE OU.kakaoUserIdx = :use_id AND C.groupName = :groupName) CD)")
    fun deleteOrgChat(use_id: Long, groupName:String)

    //해당 chatIdx 대화 가져오기
    @Query("SELECT * FROM ChatTable WHERE chatIdx = :chatIdx")
    fun getChatByChatIdx(chatIdx: Long):Chat

    //해당 chatIdx isNew 바꾸기
    @Query("UPDATE ChatTable SET isNew= :status WHERE chatIdx= :chatIdx")
    fun updateIsNew(chatIdx: Long, status: Int)

    // 상대방 대화 가져오기
    @Query("SELECT * FROM ChatTable WHERE otherUserIdx = :idx")
    fun getChatByIdx(idx: Int): List<Chat>


    //MainActivity 최근 대화 목록 다 가져오기 -- local db 내용에 맞춰서 설정하기
    @Query("SELECT CM.chatIdx, CL.chatName AS nickName, CL.profileImg AS profileImg, CL.latestTime AS postTime, CM.message, CM.groupName, CM.isNew\n" +
            "FROM\n" +
            "    (SELECT (CASE WHEN C.groupName == 'null' THEN OU.nickname ELSE C.groupName END) AS chatName,\n" +
            "            (CASE WHEN C.groupName == 'null' THEN OU.image ELSE NULL END) AS profileImg,\n" +
            "            MAX(C.postTime) as latestTime\n" +
            "    FROM ChatTable C INNER JOIN OtherUserTable OU on C.otherUserIdx = OU.otherUserIdx\n" +
            "    WHERE OU.kakaoUserIdx = :userIdx AND C.status != 'DELETED'\n" +
            "    GROUP BY chatName, profileImg) CL\n" +
            "    INNER JOIN\n" +
            "    (SELECT DISTINCT (CASE WHEN C.groupName == 'null' THEN OU.nickname ELSE C.groupName END) AS chatName, C.chatIdx, C.message, C.postTime, C.groupName, C.isNew\n" +
            "    FROM ChatTable C INNER JOIN OtherUserTable OU on C.otherUserIdx = OU.otherUserIdx\n" +
            "    WHERE OU.kakaoUserIdx = :userIdx AND C.status != 'DELETED') CM\n" +
            "    ON CL.chatName = CM.chatName AND CL.latestTime = CM.postTime\n" +
            " ORDER BY postTime DESC;")
    fun getRecentChat(userIdx:Long):LiveData<List<ChatList>>

    //갠톡, 검증된
    @Query("SELECT C.chatIdx, OU.nickname as nickName, C.groupName, OU.image as profileImg, C.message, C.postTime, C.isNew\n" +
            "    FROM ChatTable AS C INNER JOIN OtherUserTable AS OU on C.otherUserIdx = OU.otherUserIdx\n" +
            "    WHERE OU.kakaoUserIdx = :userIdx AND C.status != 'DELETED' AND C.otherUserIdx IN (SELECT otherUserIdx FROM ChatTable WHERE chatIdx = :chatIdx) AND groupName is 'null'\n" +
            "ORDER BY C.postTime DESC")
    fun getOneChatList(userIdx:Long, chatIdx:Long):LiveData<List<ChatList>>


    //단톡, 검증된
    @Query("SELECT C.chatIdx, OU.nickname as nickName, C.groupName, OU.image as profileImg, C.message, C.postTime, C.isNew" +
            " FROM ChatTable C INNER JOIN OtherUserTable OU on C.otherUserIdx = OU.otherUserIdx" +
            " WHERE OU.kakaoUserIdx = :userIdx AND C.status != 'DELETED' AND groupName = (SELECT groupName FROM ChatTable WHERE chatIdx = :chatIdx)" +
            " ORDER BY C.postTime DESC")
    fun getOrgChatList(userIdx:Long, chatIdx: Long):LiveData<List<ChatList>>


    //모든 챗 목록
    @Query("SELECT * FROM ChatTable")
    fun getChatList():List<Chat>

    //사용자의 챗 목록을 가져오기
    @Query("SELECT C.otherUserIdx,C.groupName,C.message,C.postTime,C.folderIdx,C.status,C.viewType,C.isChecked,C.chatIdx  FROM ChatTable C INNER JOIN OtherUserTable OU ON OU.otherUserIdx=C.otherUserIdx AND OU.kakaoUserIdx= :user_id ORDER BY C.postTime DESC")
    fun getUserAllChat(user_id:Long):LiveData<List<Chat>>

    //폴더의 모든 챗 가져오기
    @Query("SELECT C.postTime, C.folderIdx, C.chatIdx, C.otherUserIdx, C.isChecked, C.message, C.groupName, C.status, C.isNew, C.viewType FROM ChatTable C INNER JOIN OtherUserTable OU ON C.otherUserIdx=OU.otherUserIdx INNER JOIN FolderContentTable FC ON C.chatIdx=FC.chatIdx INNER JOIN FolderTable F ON FC.folderIdx=F.idx WHERE OU.kakaoUserIdx= :user_id AND FC.folderIdx=:folder_id ORDER BY C.postTime DESC")
    fun getFolderChat(user_id:Long, folder_id:Int):List<Chat>

    @Query("SELECT chatIdx FROM ChatTable")
    fun getChatIdxList(): List<Int>

    @Query("SELECT otherUserIdx FROM ChatTable WHERE chatIdx = :chatIdx")
    fun getChatOtherIdx(chatIdx: Long):Int
}