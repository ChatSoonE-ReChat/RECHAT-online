package com.chatsoone.rechat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "IconTable")
data class Icon(
    @SerializedName("iconImage") val iconImage: Int
) {
    @PrimaryKey(autoGenerate = true)
    var idx: Int = 0
}
