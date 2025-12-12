package com.iamrakeshpanchal.nimusms.data.entities

import androidx.room.*
import java.util.*

@Entity(
    tableName = "sms_messages",
    indices = [
        Index(value = ["thread_id"]),
        Index(value = ["address"]),
        Index(value = ["date"]),
        Index(value = ["group_id"]),
        Index(value = ["is_otp", "date"])
    ]
)
data class SmsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "thread_id")
    val threadId: Long,
    
    @ColumnInfo(name = "address")
    val address: String,
    
    @ColumnInfo(name = "body")
    val body: String,
    
    @ColumnInfo(name = "date")
    val date: Long = Date().time,
    
    @ColumnInfo(name = "type")
    val type: Int, // 1=received, 2=sent
    
    @ColumnInfo(name = "read")
    val read: Boolean = false,
    
    @ColumnInfo(name = "is_otp")
    val isOtp: Boolean = false,
    
    @ColumnInfo(name = "otp_code")
    val otpCode: String? = null,
    
    @ColumnInfo(name = "otp_expiry")
    val otpExpiry: Long? = null,
    
    @ColumnInfo(name = "group_id")
    val groupId: Long? = null,
    
    @ColumnInfo(name = "is_promotional")
    val isPromotional: Boolean = false,
    
    @ColumnInfo(name = "has_whatsapp")
    val hasWhatsApp: Boolean = false,
    
    @ColumnInfo(name = "needs_action")
    val needsAction: Boolean = false,
    
    @ColumnInfo(name = "action_deadline")
    val actionDeadline: Long? = null
)
