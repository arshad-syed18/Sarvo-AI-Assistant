package com.example.sarvov1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.sarvov1.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase:FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        binding.signup2.setOnClickListener {
            val email = binding.signupemail.text.toString()
            val pass = binding.signuppass1.text.toString()
            val confirmPass = binding.signuppass2.text.toString()
            val name = binding.signupname.text.toString()
            val phno = binding.signupphno.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { createUserTask ->
                        if (createUserTask.isSuccessful) {
                            val userId = firebaseAuth.currentUser?.uid
                            val userData = hashMapOf(
                                "email" to email,
                                "name" to name,
                                "phone" to phno
                            )
                            userId?.let {
                                firebaseDatabase.reference.child("users").child(userId).setValue(userData)
                                    .addOnSuccessListener {
                                        val intent = Intent(this, SetupActivity::class.java).apply {
                                            putExtra("email", email)
                                            putExtra("name", name)
                                            putExtra("phone", phno)
                                        }
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this, "Failed to create user: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, createUserTask.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
