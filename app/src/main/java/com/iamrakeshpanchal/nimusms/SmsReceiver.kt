package com.iamrakeshpanchal.nimusms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import android.widget.Toast

class SmsReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NimuSMS", "SMS received: ${intent.action}")
        
        when (intent.action) {
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                // Handle incoming SMS
                handleIncomingSMS(context, intent)
            }
            Telephony.Sms.Intents.SMS_DELIVER_ACTION -> {
                // Handle SMS delivery
                Toast.makeText(context, "SMS delivered to Nimu SMS", Toast.LENGTH_SHORT).show()
            }
            "android.provider.Telephony.SMS_DELIVER" -> {
                // For default SMS app
                Toast.makeText(context, "Nimu SMS is now default!", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun handleIncomingSMS(context: Context, intent: Intent) {
        val smsMessages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("pdus") as? Array<*> ?: emptyArray()
        }
        
        for (message in smsMessages) {
            if (message is android.telephony.SmsMessage) {
                val sender = message.displayOriginatingAddress ?: "Unknown"
                val body = message.displayMessageBody ?: ""
                
                Log.d("NimuSMS", "New SMS from $sender: $body")
                
                // Show notification
                showNotification(context, sender, body)
                
                // You can also update your SMS list here
            }
        }
    }
    
    private fun showNotification(context: Context, sender: String, body: String) {
        // Simple toast for now, can be expanded to Notification
        Toast.makeText(context, "New SMS from $sender", Toast.LENGTH_SHORT).show()
    }
}
