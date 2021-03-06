package com.chat_soon_e.re_chat.data.local

import android.content.Context
import androidx.room.*
import com.chat_soon_e.re_chat.ApplicationClass.Companion.APP_DATABASE

// 따로 불러와서 작업하는 것들은 만들어두지 않았기 때문에
// RoomDB를 사용해야 된다고 할 때는 추가적으로 설정해줘야 한다.
@Database(entities = [Icon::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun iconDao(): IconDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase? {
            if (instance == null) {
                synchronized(AppDatabase::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        APP_DATABASE
                    ).allowMainThreadQueries().
                    fallbackToDestructiveMigration().build()
                }
            }
            return instance
        }
    }
}