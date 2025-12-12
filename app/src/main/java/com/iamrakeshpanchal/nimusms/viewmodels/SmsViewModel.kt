package com.iamrakeshpanchal.nimusms.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamrakeshpanchal.nimusms.data.AppDatabase
import com.iamrakeshpanchal.nimusms.data.entities.SmsEntity
import com.iamrakeshpanchal.nimusms.data.entities.GroupEntity
import com.iamrakeshpanchal.nimusms.utils.OtpDetector
import com.iamrakeshpanchal.nimusms.utils.PromotionalFilter
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class SmsViewModel(database: AppDatabase) : ViewModel() {
    
    private val smsDao = database.smsDao()
    private val groupDao = database.groupDao()
    
    private val _messages = MutableLiveData<List<SmsEntity>>()
    val messages: LiveData<List<SmsEntity>> = _messages
    
    private val _groups = MutableLiveData<List<GroupEntity>>()
    val groups: LiveData<List<GroupEntity>> = _groups
    
    private val otpDetector = OtpDetector()
    private val promotionalFilter = PromotionalFilter()
    
    fun loadMessages() {
        viewModelScope.launch {
            smsDao.getAllMessages().collect { messages ->
                _messages.postValue(messages)
            }
        }
    }
    
    fun loadGroups() {
        viewModelScope.launch {
            groupDao.getAllGroups().collect { groups ->
                _groups.postValue(groups)
            }
        }
    }
    
    suspend fun processIncomingSms(address: String, body: String, timestamp: Long) {
        viewModelScope.launch {
            // Detect OTP
            val otpInfo = otpDetector.detectOtp(body)
            
            // Check if promotional
            val isPromotional = promotionalFilter.isPromotional(body, address)
            
            // Check if contact has WhatsApp
            val hasWhatsApp = checkWhatsAppContact(address)
            
            // Check for actionable items
            val (needsAction, actionDeadline) = detectActionableItem(body)
            
            // Determine group
            val groupId = determineGroup(address, body)
            
            val sms = SmsEntity(
                threadId = generateThreadId(address),
                address = address,
                body = body,
                date = timestamp,
                type = 1, // Received
                isOtp = otpInfo != null,
                otpCode = otpInfo?.code,
                otpExpiry = otpInfo?.expiry,
                isPromotional = isPromotional,
                hasWhatsApp = hasWhatsApp,
                needsAction = needsAction,
                actionDeadline = actionDeadline,
                groupId = groupId
            )
            
            smsDao.insertMessage(sms)
            
            // Send notification if not promotional
            if (!isPromotional) {
                sendNotification(sms, otpInfo)
            }
        }
    }
    
    private fun generateThreadId(address: String): Long {
        return address.hashCode().toLong()
    }
    
    private suspend fun checkWhatsAppContact(address: String): Boolean {
        // TODO: Implement contact check
        return false
    }
    
    private fun detectActionableItem(body: String): Pair<Boolean, Long?> {
        val actionKeywords = listOf(
            "due", "payment", "bill", "reminder", 
            "deadline", "renew", "expire", "confirm"
        )
        
        val pattern = Pattern.compile(
            actionKeywords.joinToString("|", "\\b(", ")\\b"),
            Pattern.CASE_INSENSITIVE
        )
        
        return if (pattern.matcher(body).find()) {
            // Set deadline to 3 days from now if detected
            Pair(true, System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000)
        } else {
            Pair(false, null)
        }
    }
    
    private suspend fun determineGroup(address: String, body: String): Long? {
        val groups = groupDao.getAllGroups()
        groups.collect { groupList ->
            groupList.forEach { group ->
                when (group.ruleType) {
                    "keyword" -> {
                        if (body.contains(group.ruleValue, true)) {
                            return group.id
                        }
                    }
                    "sender" -> {
                        if (address.contains(group.ruleValue)) {
                            return group.id
                        }
                    }
                    "regex" -> {
                        if (Pattern.matches(group.ruleValue, body)) {
                            return group.id
                        }
                    }
                }
            }
        }
        return null
    }
    
    private fun sendNotification(sms: SmsEntity, otpInfo: OtpDetector.OtpInfo?) {
        // TODO: Implement notification with bubble support
    }
}
