package com.iamrakeshpanchal.nimusms

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Telephony
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class SmsListActivity : AppCompatActivity() {
    
    private lateinit var listView: ListView
    private lateinit var emptyText: TextView
    private lateinit var smsCountText: TextView
    private lateinit var searchBox: EditText
    private lateinit var progressBar: ProgressBar
    
    private val smsList = ArrayList<SmsItem>()
    private val filteredList = ArrayList<SmsItem>()
    private lateinit var adapter: SmsAdapter
    
    data class SmsItem(
        val id: String,
        val address: String,
        val body: String,
        val date: Long,
        val type: Int,
        val read: Boolean
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create UI programmatically
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(10, 10, 10, 10)
        }
        
        // Top bar with SMS count and search
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
        }
        
        // SMS Count
        smsCountText = TextView(this).apply {
            text = "SMS: 0"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
        }
        topBar.addView(smsCountText)
        
        // Search icon and box
        val searchLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val searchIcon = TextView(this).apply {
            text = "🔍"
            textSize = 18f
            setPadding(10, 0, 5, 0)
        }
        searchLayout.addView(searchIcon)
        
        searchBox = EditText(this).apply {
            hint = "Find SMS..."
            textSize = 14f
            setSingleLine(true)
            layoutParams = LinearLayout.LayoutParams(
                200,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        searchLayout.addView(searchBox)
        
        topBar.addView(searchLayout)
        mainLayout.addView(topBar)
        
        // Progress bar for loading
        progressBar = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
        }
        mainLayout.addView(progressBar)
        
        // ListView for SMS
        listView = ListView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
            dividerHeight = 1
        }
        mainLayout.addView(listView)
        
        // Empty text
        emptyText = TextView(this).apply {
            text = "Loading SMS..."
            gravity = android.view.Gravity.CENTER
            textSize = 16f
            setPadding(0, 100, 0, 0)
        }
        mainLayout.addView(emptyText)
        
        // Settings button at BOTTOM
        val settingsBtn = Button(this).apply {
            text = "⚙️ Settings"
            setTextColor(0xFF000000.toInt())
            setOnClickListener {
                finish() // Go back to main screen
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 10
            }
        }
        mainLayout.addView(settingsBtn)
        
        setContentView(mainLayout)
        
        // Setup adapter
        adapter = SmsAdapter(this, filteredList)
        listView.adapter = adapter
        
        // Click to open SMS detail
        listView.setOnItemClickListener { parent, view, position, id ->
            Log.d("SMS_CLICK", "Clicked item at position: $position")
            val sms = filteredList[position]
            openSmsDetail(sms)
        }
        
        // Setup search
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterSMS(s.toString())
            }
        })
        
        // Load ALL SMS
        loadAllSMS()
    }
    
    private fun filterSMS(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(smsList)
        } else {
            val lowerQuery = query.lowercase(Locale.getDefault())
            for (sms in smsList) {
                if (sms.address.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                    sms.body.lowercase(Locale.getDefault()).contains(lowerQuery)) {
                    filteredList.add(sms)
                }
            }
        }
        adapter.notifyDataSetChanged()
        updateSmsCount()
    }
    
    private fun updateSmsCount() {
        smsCountText.text = "SMS: ${filteredList.size}"
        if (filteredList.isEmpty() && !searchBox.text.isNullOrEmpty()) {
            emptyText.text = "No matching SMS found"
            emptyText.visibility = View.VISIBLE
            listView.visibility = View.GONE
            progressBar.visibility = View.GONE
        } else if (filteredList.isEmpty()) {
            emptyText.text = "No SMS messages"
            emptyText.visibility = View.VISIBLE
            listView.visibility = View.GONE
            progressBar.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            listView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
    
    @SuppressLint("Range")
    private fun loadAllSMS() {
        emptyText.text = "Loading ALL SMS..."
        emptyText.visibility = View.VISIBLE
        listView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        
        Thread {
            try {
                // Load ALL SMS without limit
                val cursor: Cursor? = contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    arrayOf(
                        Telephony.Sms._ID,
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.BODY,
                        Telephony.Sms.DATE,
                        Telephony.Sms.TYPE,
                        Telephony.Sms.READ
                    ),
                    null, null, "${Telephony.Sms.DATE} DESC" // Remove LIMIT 100
                )
                
                cursor?.use {
                    smsList.clear()
                    var count = 0
                    while (it.moveToNext()) {
                        val id = it.getString(it.getColumnIndex(Telephony.Sms._ID))
                        val address = it.getString(it.getColumnIndex(Telephony.Sms.ADDRESS)) ?: "Unknown"
                        val body = it.getString(it.getColumnIndex(Telephony.Sms.BODY)) ?: ""
                        val date = it.getLong(it.getColumnIndex(Telephony.Sms.DATE))
                        val type = it.getInt(it.getColumnIndex(Telephony.Sms.TYPE))
                        val read = it.getInt(it.getColumnIndex(Telephony.Sms.READ)) == 1
                        
                        smsList.add(SmsItem(id, address, body, date, type, read))
                        count++
                        
                        // Update progress every 100 messages
                        if (count % 100 == 0) {
                            runOnUiThread {
                                smsCountText.text = "Loading... $count SMS"
                            }
                        }
                    }
                    
                    Log.d("SMS_LOAD", "Loaded $count SMS messages")
                }
                
                runOnUiThread {
                    filterSMS("") // Show all initially
                    Toast.makeText(this, "Loaded ${smsList.size} SMS", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    emptyText.text = "Error: ${e.message}"
                    progressBar.visibility = View.GONE
                }
            }
        }.start()
    }
    
    private fun openSmsDetail(sms: SmsItem) {
        Log.d("SMS_CLICK", "Opening detail for SMS ID: ${sms.id}")
        
        // Mark as read
        markAsRead(sms.id)
        
        // Open detail activity
        val intent = Intent(this, SmsDetailActivity::class.java).apply {
            putExtra("sms_id", sms.id)
            putExtra("address", sms.address)
            putExtra("body", sms.body)
            putExtra("date", sms.date)
            putExtra("type", sms.type)
        }
        startActivity(intent)
    }
    
    private fun markAsRead(smsId: String) {
        try {
            val values = ContentValues().apply { put(Telephony.Sms.READ, 1) }
            contentResolver.update(
                Telephony.Sms.CONTENT_URI,
                values,
                "${Telephony.Sms._ID} = ?",
                arrayOf(smsId)
            )
        } catch (e: Exception) {
            Log.e("SMS_READ", "Error marking as read: ${e.message}")
        }
    }
    
    private fun deleteSMS(position: Int) {
        val sms = filteredList[position]
        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Delete message from ${sms.address}?")
            .setPositiveButton("🗑️ Delete") { _, _ ->
                try {
                    contentResolver.delete(
                        Telephony.Sms.CONTENT_URI,
                        "${Telephony.Sms._ID} = ?",
                        arrayOf(sms.id)
                    )
                    smsList.removeAll { it.id == sms.id }
                    filteredList.removeAt(position)
                    adapter.notifyDataSetChanged()
                    updateSmsCount()
                    Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    inner class SmsAdapter(
        private val context: android.content.Context,
        private val items: List<SmsItem>
    ) : BaseAdapter() {
        
        @SuppressLint("SimpleDateFormat")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.sms_list_item, parent, false)
            
            val sms = items[position]
            val senderText = view.findViewById<TextView>(R.id.senderText)
            val bodyText = view.findViewById<TextView>(R.id.bodyText)
            val trashIcon = view.findViewById<TextView>(R.id.trashIcon)
            val typeIcon = view.findViewById<TextView>(R.id.typeIcon)
            
            // Get sender name
            val sender = if (sms.address.length > 15) sms.address.substring(0, 12) + "..." else sms.address
            
            // Format date: "12-Dec 14:30"
            val dateFormat = SimpleDateFormat("dd-MMM HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(sms.date))
            
            // First line: Sender + Date (BOLD)
            senderText.text = "$sender • $formattedDate"
            senderText.setTypeface(null, Typeface.BOLD)
            
            // Set type icon
            typeIcon.text = when(sms.type) {
                Telephony.Sms.MESSAGE_TYPE_INBOX -> "��"
                Telephony.Sms.MESSAGE_TYPE_SENT -> "📤"
                else -> "📱"
            }
            
            // Second line: First line of SMS body
            val firstLine = sms.body.split("\n").firstOrNull() ?: sms.body
            val truncatedBody = if (firstLine.length > 50) firstLine.substring(0, 47) + "..." else firstLine
            bodyText.text = truncatedBody
            
            // Unread indicator
            if (!sms.read) {
                senderText.setTextColor(0xFF0000FF.toInt()) // Blue for unread
            } else {
                senderText.setTextColor(0xFF000000.toInt()) // Black for read
            }
            
            // Trash icon click
            trashIcon.setOnClickListener {
                Log.d("SMS_CLICK", "Trash icon clicked at position: $position")
                deleteSMS(position)
            }
            
            // Make the entire item clickable
            view.isClickable = true
            view.isFocusable = true
            
            return view
        }
        
        override fun getCount(): Int = items.size
        
        override fun getItem(position: Int): SmsItem = items[position]
        
        override fun getItemId(position: Int): Long = position.toLong()
    }
}
EOFcat > app/src/main/java/com/iamrakeshpanchal/nimusms/SmsListActivity.kt << 'EOF'
package com.iamrakeshpanchal.nimusms

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Telephony
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class SmsListActivity : AppCompatActivity() {
    
    private lateinit var listView: ListView
    private lateinit var emptyText: TextView
    private lateinit var smsCountText: TextView
    private lateinit var searchBox: EditText
    private lateinit var progressBar: ProgressBar
    
    private val smsList = ArrayList<SmsItem>()
    private val filteredList = ArrayList<SmsItem>()
    private lateinit var adapter: SmsAdapter
    
    data class SmsItem(
        val id: String,
        val address: String,
        val body: String,
        val date: Long,
        val type: Int,
        val read: Boolean
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create UI programmatically
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(10, 10, 10, 10)
        }
        
        // Top bar with SMS count and search
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
        }
        
        // SMS Count
        smsCountText = TextView(this).apply {
            text = "SMS: 0"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
        }
        topBar.addView(smsCountText)
        
        // Search icon and box
        val searchLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val searchIcon = TextView(this).apply {
            text = "🔍"
            textSize = 18f
            setPadding(10, 0, 5, 0)
        }
        searchLayout.addView(searchIcon)
        
        searchBox = EditText(this).apply {
            hint = "Find SMS..."
            textSize = 14f
            setSingleLine(true)
            layoutParams = LinearLayout.LayoutParams(
                200,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        searchLayout.addView(searchBox)
        
        topBar.addView(searchLayout)
        mainLayout.addView(topBar)
        
        // Progress bar for loading
        progressBar = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
        }
        mainLayout.addView(progressBar)
        
        // ListView for SMS
        listView = ListView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
            dividerHeight = 1
        }
        mainLayout.addView(listView)
        
        // Empty text
        emptyText = TextView(this).apply {
            text = "Loading SMS..."
            gravity = android.view.Gravity.CENTER
            textSize = 16f
            setPadding(0, 100, 0, 0)
        }
        mainLayout.addView(emptyText)
        
        // Settings button at BOTTOM
        val settingsBtn = Button(this).apply {
            text = "⚙️ Settings"
            setTextColor(0xFF000000.toInt())
            setOnClickListener {
                finish() // Go back to main screen
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 10
            }
        }
        mainLayout.addView(settingsBtn)
        
        setContentView(mainLayout)
        
        // Setup adapter
        adapter = SmsAdapter(this, filteredList)
        listView.adapter = adapter
        
        // Click to open SMS detail
        listView.setOnItemClickListener { parent, view, position, id ->
            Log.d("SMS_CLICK", "Clicked item at position: $position")
            val sms = filteredList[position]
            openSmsDetail(sms)
        }
        
        // Setup search
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterSMS(s.toString())
            }
        })
        
        // Load ALL SMS
        loadAllSMS()
    }
    
    private fun filterSMS(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(smsList)
        } else {
            val lowerQuery = query.lowercase(Locale.getDefault())
            for (sms in smsList) {
                if (sms.address.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                    sms.body.lowercase(Locale.getDefault()).contains(lowerQuery)) {
                    filteredList.add(sms)
                }
            }
        }
        adapter.notifyDataSetChanged()
        updateSmsCount()
    }
    
    private fun updateSmsCount() {
        smsCountText.text = "SMS: ${filteredList.size}"
        if (filteredList.isEmpty() && !searchBox.text.isNullOrEmpty()) {
            emptyText.text = "No matching SMS found"
            emptyText.visibility = View.VISIBLE
            listView.visibility = View.GONE
            progressBar.visibility = View.GONE
        } else if (filteredList.isEmpty()) {
            emptyText.text = "No SMS messages"
            emptyText.visibility = View.VISIBLE
            listView.visibility = View.GONE
            progressBar.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            listView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
    
    @SuppressLint("Range")
    private fun loadAllSMS() {
        emptyText.text = "Loading ALL SMS..."
        emptyText.visibility = View.VISIBLE
        listView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        
        Thread {
            try {
                // Load ALL SMS without limit
                val cursor: Cursor? = contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    arrayOf(
                        Telephony.Sms._ID,
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.BODY,
                        Telephony.Sms.DATE,
                        Telephony.Sms.TYPE,
                        Telephony.Sms.READ
                    ),
                    null, null, "${Telephony.Sms.DATE} DESC" // Remove LIMIT 100
                )
                
                cursor?.use {
                    smsList.clear()
                    var count = 0
                    while (it.moveToNext()) {
                        val id = it.getString(it.getColumnIndex(Telephony.Sms._ID))
                        val address = it.getString(it.getColumnIndex(Telephony.Sms.ADDRESS)) ?: "Unknown"
                        val body = it.getString(it.getColumnIndex(Telephony.Sms.BODY)) ?: ""
                        val date = it.getLong(it.getColumnIndex(Telephony.Sms.DATE))
                        val type = it.getInt(it.getColumnIndex(Telephony.Sms.TYPE))
                        val read = it.getInt(it.getColumnIndex(Telephony.Sms.READ)) == 1
                        
                        smsList.add(SmsItem(id, address, body, date, type, read))
                        count++
                        
                        // Update progress every 100 messages
                        if (count % 100 == 0) {
                            runOnUiThread {
                                smsCountText.text = "Loading... $count SMS"
                            }
                        }
                    }
                    
                    Log.d("SMS_LOAD", "Loaded $count SMS messages")
                }
                
                runOnUiThread {
                    filterSMS("") // Show all initially
                    Toast.makeText(this, "Loaded ${smsList.size} SMS", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    emptyText.text = "Error: ${e.message}"
                    progressBar.visibility = View.GONE
                }
            }
        }.start()
    }
    
    private fun openSmsDetail(sms: SmsItem) {
        Log.d("SMS_CLICK", "Opening detail for SMS ID: ${sms.id}")
        
        // Mark as read
        markAsRead(sms.id)
        
        // Open detail activity
        val intent = Intent(this, SmsDetailActivity::class.java).apply {
            putExtra("sms_id", sms.id)
            putExtra("address", sms.address)
            putExtra("body", sms.body)
            putExtra("date", sms.date)
            putExtra("type", sms.type)
        }
        startActivity(intent)
    }
    
    private fun markAsRead(smsId: String) {
        try {
            val values = ContentValues().apply { put(Telephony.Sms.READ, 1) }
            contentResolver.update(
                Telephony.Sms.CONTENT_URI,
                values,
                "${Telephony.Sms._ID} = ?",
                arrayOf(smsId)
            )
        } catch (e: Exception) {
            Log.e("SMS_READ", "Error marking as read: ${e.message}")
        }
    }
    
    private fun deleteSMS(position: Int) {
        val sms = filteredList[position]
        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Delete message from ${sms.address}?")
            .setPositiveButton("🗑️ Delete") { _, _ ->
                try {
                    contentResolver.delete(
                        Telephony.Sms.CONTENT_URI,
                        "${Telephony.Sms._ID} = ?",
                        arrayOf(sms.id)
                    )
                    smsList.removeAll { it.id == sms.id }
                    filteredList.removeAt(position)
                    adapter.notifyDataSetChanged()
                    updateSmsCount()
                    Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    inner class SmsAdapter(
        private val context: android.content.Context,
        private val items: List<SmsItem>
    ) : BaseAdapter() {
        
        @SuppressLint("SimpleDateFormat")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.sms_list_item, parent, false)
            
            val sms = items[position]
            val senderText = view.findViewById<TextView>(R.id.senderText)
            val bodyText = view.findViewById<TextView>(R.id.bodyText)
            val trashIcon = view.findViewById<TextView>(R.id.trashIcon)
            val typeIcon = view.findViewById<TextView>(R.id.typeIcon)
            
            // Get sender name
            val sender = if (sms.address.length > 15) sms.address.substring(0, 12) + "..." else sms.address
            
            // Format date: "12-Dec 14:30"
            val dateFormat = SimpleDateFormat("dd-MMM HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(sms.date))
            
            // First line: Sender + Date (BOLD)
            senderText.text = "$sender • $formattedDate"
            senderText.setTypeface(null, Typeface.BOLD)
            
            // Set type icon
            typeIcon.text = when(sms.type) {
                Telephony.Sms.MESSAGE_TYPE_INBOX -> "��"
                Telephony.Sms.MESSAGE_TYPE_SENT -> "📤"
                else -> "📱"
            }
            
            // Second line: First line of SMS body
            val firstLine = sms.body.split("\n").firstOrNull() ?: sms.body
            val truncatedBody = if (firstLine.length > 50) firstLine.substring(0, 47) + "..." else firstLine
            bodyText.text = truncatedBody
            
            // Unread indicator
            if (!sms.read) {
                senderText.setTextColor(0xFF0000FF.toInt()) // Blue for unread
            } else {
                senderText.setTextColor(0xFF000000.toInt()) // Black for read
            }
            
            // Trash icon click
            trashIcon.setOnClickListener {
                Log.d("SMS_CLICK", "Trash icon clicked at position: $position")
                deleteSMS(position)
            }
            
            // Make the entire item clickable
            view.isClickable = true
            view.isFocusable = true
            
            return view
        }
        
        override fun getCount(): Int = items.size
        
        override fun getItem(position: Int): SmsItem = items[position]
        
        override fun getItemId(position: Int): Long = position.toLong()
    }
}
