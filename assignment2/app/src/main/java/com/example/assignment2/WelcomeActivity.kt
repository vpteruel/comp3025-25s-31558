package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.assignment2.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class WelcomeActivity : AppCompatActivity() {

    private val pageName = "WelcomeActivity"
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = intent.getStringExtra("USER_NAME")
        if (username != null) {
            Log.d(pageName, "Successful login with username: $username")
            binding.textViewWelcomeMessage.text = "Welcome, $username!"
        } else {
            Log.d(pageName, "Successful login without username")
            binding.textViewWelcomeMessage.text = "Welcome!"
        }

        binding.buttonLogout.setOnClickListener {
            Log.d(pageName, "Logout button clicked")
            Firebase.auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}