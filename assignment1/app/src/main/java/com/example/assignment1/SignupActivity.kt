package com.example.assignment1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.assignment1.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private val pageName = "SignupActivity"
    private lateinit var binding: ActivitySignupBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)

        binding.buttonSignUp.setOnClickListener {
            val newUsername = binding.editTextNewUsername.text.toString().trim()
            val newPassword = binding.editTextNewPassword.text.toString().trim()

            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                Log.d(pageName, "Username or password is empty")
                return@setOnClickListener
            }

            with(sharedPreferences.edit()) {
                putString(LoginActivity.KEY_USERNAME, newUsername)
                putString(LoginActivity.KEY_PASSWORD, newPassword)
                putBoolean(LoginActivity.KEY_IS_LOGGED_IN, false)
                apply()
            }

            Toast.makeText(this, "Sign up successful! Please login.", Toast.LENGTH_LONG).show()
            Log.d(pageName, "Sign up successful")

            // navigate back to LoginActivity or directly to login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        binding.textViewGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}