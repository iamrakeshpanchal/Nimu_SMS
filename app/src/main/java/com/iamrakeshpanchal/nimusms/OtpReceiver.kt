package com.iamrakeshpanchal.nimusms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class OtpReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
            
            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // SMS retrieved successfully
                    val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                    Log.d("NimuSMS", "OTP received: $message")
                    
                    // Extract OTP from message
                    message?.let { extractOtp(context, it) }
                }
                
                CommonStatusCodes.TIMEOUT -> {
                    Log.d("NimuSMS", "OTP timeout")
                }
            }
        }
    }
    
    private fun extractOtp(context: Context, message: String) {
        // Extract 4-6 digit OTP from message
        val otpRegex = "\\b\\d{4,6}\\b".toRegex()
        val otp = otpRegex.find(message)?.value
        
        otp?.let {
            Log.d("NimuSMS", "Extracted OTP: $otp")
            // You can send this OTP to your activity via broadcast or event bus
            // For now, just log it
        }
    }
}
