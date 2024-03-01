package com.example.sarvov1

import android.content.pm.PackageManager
import android.Manifest
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity(), ApiCallback {

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var voiceButton: Button
    private lateinit var speechRecognizer: SpeechRecognizer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter(messages)
        recyclerView.adapter = adapter

        val drawerLayout:DrawerLayout = findViewById(R.id.drawerLayout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.nav_item1 -> {
                    Toast.makeText(this, "Nav 1 clicked", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(navigationView)
                    true
                }
                R.id.nav_item2 -> {
                    Toast.makeText(this, "nav 2 clicked", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(navigationView)
                    true
                }
                else -> false
            }
        }
        // Buttons on clicks
        voiceButton = findViewById(R.id.voiceButton)
        //check for record audio permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        voiceButton.setOnClickListener {
            startListening()
            Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show()
        }


        val sendButton = findViewById<Button>(R.id.sendButton)
        val messageInputField = findViewById<EditText>(R.id.messageInputField)
        addMessage("Hello User! How can I help?", false) // change text if needed
        // API link https://deep-friendly-kodiak.ngrok-free.app/user-input

        sendButton.setOnClickListener {
            val message = messageInputField.text.toString()
            if (message.isNotBlank()) {
                Toast.makeText(this, "Message: $message", Toast.LENGTH_SHORT).show()
                addMessage(message, true)
                messageInputField.text.clear()
                if(isNetworkAvailable()){
                    makeApiCall(message)
                }else{
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
                }
            }
        }


    }

    private fun startListening() {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizer.setRecognitionListener(object: RecognitionListener{
            override fun onReadyForSpeech(p0: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(p0: Float) {}

            override fun onBufferReceived(p0: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(p0: Int) {
                Toast.makeText(this@HomeActivity, "Error occurred: $p0", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if(!matches.isNullOrEmpty()){
                    val recognizedText = matches[0]
                    addMessage(recognizedText, true)
                    if(isNetworkAvailable()){
                        makeApiCall(recognizedText)
                    }else{
                        Toast.makeText(this@HomeActivity, "No internet connection", Toast.LENGTH_LONG).show()
                    }
                    // Toast.makeText(this@HomeActivity, "Recognized: $recognizedText", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onPartialResults(p0: Bundle?) {}

            override fun onEvent(p0: Int, p1: Bundle?) {}

        })
        speechRecognizer.startListening(speechIntent)
    }
    private fun addMessage(message: String, isUser: Boolean) {
        messages.add(ChatMessage(message, isUser))
        adapter.notifyItemInserted(messages.size - 1)
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network  = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun makeApiCall(userMessage: String){
        val apiHelper = ApiHelper(this)
        apiHelper.getResponse(userMessage)
    }

    override fun onApiSuccess(response: String) {
        runOnUiThread{
            addMessage(response, false)
        }
    }

    override fun onApiError(error: String) {
        runOnUiThread{
            addMessage("Error connecting to backend!", false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}