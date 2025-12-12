package com.iamrakeshpanchal.nimusms

import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.*
import com.iamrakeshpanchal.nimusms.data.AppDatabase
import com.iamrakeshpanchal.nimusms.workers.BackupWorker
import com.iamrakeshpanchal.nimusms.workers.OtpCleanupWorker
import com.iamrakeshpanchal.nimusms.workers.SummaryWorker
import java.util.concurrent.TimeUnit

class NimuSMSApplication : Application() {
    
    companion object {
        lateinit var database: AppDatabase
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database
        database = AppDatabase.getDatabase(this)
        
        // Setup WorkManager for scheduled tasks
        setupWorkManager()
        
        // Request SMS role if not set
        if (!isDefaultSmsApp()) {
            requestSmsRole()
        }
    }
    
    private fun setupWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // Daily backup at 2 AM
        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            24, TimeUnit.HOURS,
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        // OTP cleanup every 6 hours
        val otpCleanupRequest = PeriodicWorkRequestBuilder<OtpCleanupWorker>(
            6, TimeUnit.HOURS
        ).build()
        
        // Daily summary at 9 PM
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
        }
        val summaryRequest = PeriodicWorkRequestBuilder<SummaryWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(
                calendar.timeInMillis - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(this).apply {
            enqueueUniquePeriodicWork(
                "daily_backup",
                ExistingPeriodicWorkPolicy.KEEP,
                backupRequest
            )
            enqueueUniquePeriodicWork(
                "otp_cleanup",
                ExistingPeriodicWorkPolicy.KEEP,
                otpCleanupRequest
            )
            enqueueUniquePeriodicWork(
                "daily_summary",
                ExistingPeriodicWorkPolicy.KEEP,
                summaryRequest
            )
        }
    }
    
    private fun isDefaultSmsApp(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            roleManager.isRoleHeld(RoleManager.ROLE_SMS)
        } else {
            packageName == android.provider.Telephony.Sms.getDefaultSmsPackage(this)
        }
    }
    
    private fun requestSmsRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ContextCompat.startActivity(this, intent, null)
        }
    }
}
