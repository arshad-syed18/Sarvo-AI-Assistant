package com.example.sarvov1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.sarvov1.databinding.ActivitySetupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase:FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        binding.signup3.setOnClickListener {
            val keenname = binding.keenname.text.toString()
            val keenph = binding.keenph.text.toString()
            val hobbies = binding.hobbies.text.toString()
            val desc = binding.desc.text.toString()
            val likes = binding.likes.text.toString()
            val dislikes = binding.dislikes.text.toString()

            val userId = firebaseAuth.currentUser?.uid
            val userData = hashMapOf(
                "keenname" to keenname,
                "keenph" to keenph,
                "hobbies" to hobbies,
                "desc" to desc,
                "likes" to likes,
                "dislikes" to dislikes
            )

            userId?.let {
                firebaseDatabase.reference.child("users").child(userId).updateChildren(userData as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Setup successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()

                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to setup: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
