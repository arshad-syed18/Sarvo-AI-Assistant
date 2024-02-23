package com.example.sarvov1

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity(), ApiCallback {

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
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
        val sendButton = findViewById<Button>(R.id.sendButton)
        val messageInputField = findViewById<EditText>(R.id.messageInputField)
        addMessage("Hello User! How can I help?", false) // change text if needed
        // API link https://deep-friendly-kodiak.ngrok-free.app/user-input

        sendButton.setOnClickListener {
            val message = messageInputField.text.toString()
            if (message.isNotBlank()) {
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
}