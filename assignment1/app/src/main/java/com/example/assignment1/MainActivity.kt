package com.example.assignment1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import com.example.assignment1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val pageName = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)

        // if the user is not logged in, redirect to LoginActivity
        if (!sharedPreferences.getBoolean(LoginActivity.KEY_IS_LOGGED_IN, false)) {
            Log.d(pageName, "User is not logged in, redirecting to LoginActivity")
            navigateToLogin()
            return
        }

        val loggedInUsername = sharedPreferences.getString(LoginActivity.KEY_USERNAME, "User")
        binding.textViewLoggedInUser.text = "Username: $loggedInUsername"
        binding.textViewWelcome.text = "Welcome, $loggedInUsername!"

        binding.buttonLogout.setOnClickListener {
            with(sharedPreferences.edit()) {
                putBoolean(LoginActivity.KEY_IS_LOGGED_IN, false)
                // remove(LoginActivity.KEY_USERNAME)
                remove(LoginActivity.KEY_PASSWORD)
                apply()
            }
            navigateToLogin()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}