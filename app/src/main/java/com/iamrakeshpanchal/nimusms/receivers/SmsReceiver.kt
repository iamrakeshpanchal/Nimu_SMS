package com.iamrakeshpanchal.nimusms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.iamrakeshpanchal.nimusms.NimuSMSApplication
import com.iamrakeshpanchal.nimusms.workers.SmsProcessingWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val bundle: Bundle? = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as Array<*>?
                if (pdus != null) {
                    val messages = arrayOfNulls<SmsMessage>(pdus.size)
                    
                    for (i in pdus.indices) {
                        val pdu = pdus[i] as ByteArray
                        messages[i] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            SmsMessage.createFromPdu(pdu, bundle.getString("format"))
                        } else {
                            SmsMessage.createFromPdu(pdu)
                        }
                    }
                    
                    // Process each message
                    for (sms in messages) {
                        sms?.let { message ->
                            val address = message.displayOriginatingAddress
                            val body = message.displayMessageBody
                            val timestamp = message.timestampMillis
                            
                            // Use WorkManager for background processing
                            val workRequest = OneTimeWorkRequestBuilder<SmsProcessingWorker>()
                                .setInputData(
                                    androidx.work.Data.Builder()
                                        .putString("address", address)
                                        .putString("body", body)
                                        .putLong("timestamp", timestamp)
                                        .build()
                                )
                                .build()
                            
                            WorkManager.getInstance(context).enqueue(workRequest)
                        }
                    }
                    
                    // Abort broadcast if we're default SMS app
                    if (isDefaultSmsApp(context)) {
                        abortBroadcast()
                    }
                }
            }
        }
    }
    
    private fun isDefaultSmsApp(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as android.app.role.RoleManager
            roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_SMS)
        } else {
            context.packageName == android.provider.Telephony.Sms.getDefaultSmsPackage(context)
        }
    }
}
