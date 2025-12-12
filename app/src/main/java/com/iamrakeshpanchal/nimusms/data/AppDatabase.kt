package com.iamrakeshpanchal.nimusms.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.iamrakeshpanchal.nimusms.data.dao.SmsDao
import com.iamrakeshpanchal.nimusms.data.dao.GroupDao
import com.iamrakeshpanchal.nimusms.data.entities.SmsEntity
import com.iamrakeshpanchal.nimusms.data.entities.GroupEntity

@Database(
    entities = [SmsEntity::class, GroupEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun smsDao(): SmsDao
    abstract fun groupDao(): GroupDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nimu_sms_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
