package com.iamrakeshpanchal.nimusms.data.dao

import androidx.room.*
import com.iamrakeshpanchal.nimusms.data.entities.SmsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    
    @Query("SELECT * FROM sms_messages ORDER BY date DESC")
    fun getAllMessages(): Flow<List<SmsEntity>>
    
    @Query("SELECT * FROM sms_messages WHERE thread_id = :threadId ORDER BY date ASC")
    fun getMessagesByThread(threadId: Long): Flow<List<SmsEntity>>
    
    @Query("SELECT * FROM sms_messages WHERE is_otp = 1 AND date >= :since")
    fun getOtpMessages(since: Long): List<SmsEntity>
    
    @Query("SELECT * FROM sms_messages WHERE is_promotional = 1")
    fun getPromotionalMessages(): Flow<List<SmsEntity>>
    
    @Query("SELECT * FROM sms_messages WHERE needs_action = 1 AND action_deadline > :now")
    fun getActionableMessages(now: Long): List<SmsEntity>
    
    @Query("SELECT * FROM sms_messages WHERE group_id = :groupId")
    fun getMessagesByGroup(groupId: Long): Flow<List<SmsEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SmsEntity): Long
    
    @Update
    suspend fun updateMessage(message: SmsEntity)
    
    @Delete
    suspend fun deleteMessage(message: SmsEntity)
    
    @Query("DELETE FROM sms_messages WHERE is_otp = 1 AND date < :expiryTime")
    suspend fun deleteExpiredOtps(expiryTime: Long)
    
    @Query("SELECT DISTINCT address FROM sms_messages WHERE is_promotional = 0")
    fun getNonPromotionalSenders(): Flow<List<String>>
    
    @Query("SELECT COUNT(*) FROM sms_messages WHERE date >= :startOfDay AND date <= :endOfDay")
    suspend fun getMessagesCountForDay(startOfDay: Long, endOfDay: Long): Int
    
    @Query("SELECT COUNT(*) FROM sms_messages WHERE is_otp = 1 AND date >= :startOfDay AND date <= :endOfDay")
    suspend fun getOtpCountForDay(startOfDay: Long, endOfDay: Long): Int
    
    @Query("SELECT COUNT(*) FROM sms_messages WHERE is_promotional = 1 AND date >= :startOfDay AND date <= :endOfDay")
    suspend fun getPromotionalCountForDay(startOfDay: Long, endOfDay: Long): Int
}
