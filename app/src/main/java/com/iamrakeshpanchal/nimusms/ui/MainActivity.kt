package com.iamrakeshpanchal.nimusms.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.iamrakeshpanchal.nimusms.NimuSMSApplication
import com.iamrakeshpanchal.nimusms.databinding.ActivityMainBinding
import com.iamrakeshpanchal.nimusms.viewmodels.SmsViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SmsViewModel
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            setupDefaultSmsApp()
        } else {
            Toast.makeText(
                this, 
                "SMS permissions are required for the app to function",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel
        viewModel = SmsViewModel(NimuSMSApplication.database)
        
        // Check and request permissions
        checkPermissions()
        
        // Setup observers
        setupObservers()
    }
    
    private fun checkPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            setupDefaultSmsApp()
        }
    }
    
    private fun setupDefaultSmsApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (packageName != Telephony.Sms.getDefaultSmsPackage(this)) {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                startActivity(intent)
            }
        }
        
        // Load messages
        lifecycleScope.launch {
            viewModel.loadMessages()
        }
    }
    
    private fun setupObservers() {
        viewModel.messages.observe(this) { messages ->
            // Update RecyclerView with messages
            // TODO: Implement adapter
        }
        
        viewModel.groups.observe(this) { groups ->
            // Update groups chips
            // TODO: Implement groups UI
        }
    }
}
