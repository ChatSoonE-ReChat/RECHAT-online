package com.chat_soon_e.re_chat.data.remote.user

import com.google.gson.annotations.SerializedName

// 자바의 데이터 클래스 멤버변수 이름은 camelCase 사용
// 카카오 유저 추가하기
data class UserResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code:Int,
    @SerializedName("message") val message: String
)

data class User(
    @SerializedName("kakaoUserIdx") val kakaoUserIdx: Long
)