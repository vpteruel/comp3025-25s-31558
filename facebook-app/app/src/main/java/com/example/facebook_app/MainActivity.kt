package com.example.facebook_app

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.facebook_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val pageName = "MainActivity"

    private val callbackManager = CallbackManager.Factory.create()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignIn()
        setupFacebookSignIn()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupGoogleSignIn() {

    }

    private fun setupFacebookSignIn() {
        binding.btnFacebookLogin.setPermissions("email", "public_profile")
        binding.btnFacebookLogin.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                // Handle Facebook login success
                Log.d(pageName, "Facebook login success")
            }

            override fun onCancel() {
                // Handle Facebook login cancel
                Log.d(pageName, "Facebook login cancel")
            }

            override fun onError(error: FacebookException) {
                // Handle Facebook error
                Log.e(pageName, "Facebook login error", error)
            }
        })
    }
}