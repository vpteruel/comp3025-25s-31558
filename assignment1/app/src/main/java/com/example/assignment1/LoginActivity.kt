package com.example.assignment1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.assignment1.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val pageName = "LoginActivity"
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val PREFS_NAME = "UserPrefs"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // check if user is already logged in
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            Log.d(pageName, "User is already logged in, redirecting to MainActivity")
            navigateToMain()
            return
        }

        binding.buttonLogin.setOnClickListener {
            val enteredUsername = binding.editTextUsername.text.toString().trim()
            val enteredPassword = binding.editTextPassword.text.toString().trim()

            if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                Log.d(pageName, "Username or password is empty")
                return@setOnClickListener
            }

            val savedUsername = sharedPreferences.getString(KEY_USERNAME, null)
            val savedPassword = sharedPreferences.getString(KEY_PASSWORD, null)

            if (enteredUsername == savedUsername && enteredPassword == savedPassword) {
                with(sharedPreferences.edit()) {
                    putBoolean(KEY_IS_LOGGED_IN, true)
                    apply()
                }
                Toast.makeText(this, "Login successful.", Toast.LENGTH_SHORT).show()
                Log.d(pageName, "Login successful")
                navigateToMain()
            } else {
                Toast.makeText(this, "Login failed. Username or password is incorrect.", Toast.LENGTH_LONG).show()
                Log.d(pageName, "Login failed")
            }
        }

        binding.textViewGoToSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear back stack
        startActivity(intent)
        finish()
    }
}