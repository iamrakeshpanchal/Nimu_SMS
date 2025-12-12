package com.iamrakeshpanchal.nimusms.utils

import java.util.regex.Pattern
import java.util.*

class OtpDetector {
    
    data class OtpInfo(
        val code: String,
        val expiry: Long? = null
    )
    
    private val otpPatterns = listOf(
        Pattern.compile("\\b\\d{4}\\b"),  // 4-digit OTP
        Pattern.compile("\\b\\d{6}\\b"),  // 6-digit OTP
        Pattern.compile("\\b\\d{8}\\b")   // 8-digit OTP
    )
    
    private val otpKeywords = listOf(
        "otp", "one time password", "verification code",
        "secure code", "auth code", "login code"
    )
    
    private val validityPatterns = listOf(
        Pattern.compile("(?i)valid for (\\d+) (min|minutes|hour|hours)"),
        Pattern.compile("(?i)expires in (\\d+) (min|minutes|hour|hours)"),
        Pattern.compile("(?i)(\\d+) (min|minutes|hour|hours) valid")
    )
    
    fun detectOtp(message: String): OtpInfo? {
        // Check if message contains OTP keywords
        val containsOtpKeyword = otpKeywords.any { keyword ->
            message.contains(keyword, ignoreCase = true)
        }
        
        if (!containsOtpKeyword) return null
        
        // Find OTP code
        for (pattern in otpPatterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val otpCode = matcher.group()
                val expiry = extractValidityPeriod(message)
                return OtpInfo(otpCode, expiry)
            }
        }
        
        return null
    }
    
    private fun extractValidityPeriod(message: String): Long? {
        for (pattern in validityPatterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val duration = matcher.group(1).toInt()
                val unit = matcher.group(2).toLowerCase(Locale.ROOT)
                
                val multiplier = when {
                    unit.contains("hour") -> 60 * 60 * 1000L
                    else -> 60 * 1000L  // minutes
                }
                
                return System.currentTimeMillis() + (duration * multiplier)
            }
        }
        
        // Default OTP validity: 24 hours
        return System.currentTimeMillis() + 24 * 60 * 60 * 1000
    }
}
