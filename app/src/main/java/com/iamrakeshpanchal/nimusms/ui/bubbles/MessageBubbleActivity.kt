package com.iamrakeshpanchal.nimusms.ui.bubbles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iamrakeshpanchal.nimusms.databinding.ActivityBubbleBinding

class MessageBubbleActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBubbleBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBubbleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val message = intent.getStringExtra("message") ?: ""
        val sender = intent.getStringExtra("sender") ?: "Unknown"
        
        binding.textSender.text = sender
        binding.textMessage.text = message
    }
}
