package com.chat_soon_e.re_chat.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName="IconTable")
data class Icon(
    @SerializedName("iconImage") val iconImage: Int
) {
    @PrimaryKey(autoGenerate = true) var idx: Int = 0
}