package com.example.sarvov1
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val login=findViewById<Button>(R.id.login)
        login.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val signup=findViewById<Button>(R.id.signup)
        signup.setOnClickListener{
            val intent1 = Intent(this, SignupActivity::class.java)
            startActivity(intent1)
        }
    }
}