package com.example.final_assignment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.final_assignment.databinding.ActivitySignUpFacebookBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpFacebookActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpFacebookBinding
    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "SignUpFacebookActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpFacebookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                lifecycleScope.launch {
                    handleFacebookAccessToken(result.accessToken)
                }
            }

            override fun onCancel() {
                Log.d(TAG, "Sign up canceled")
            }

            override fun onError(error: FacebookException) {
                Log.w(TAG, "Sign up failed", error)
            }
        })

        binding.signInButton.setOnClickListener {
            signIn()
        }

        binding.signOutButton.setOnClickListener {
            signOut()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpFacebook)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun signIn() {
        auth.signOut()
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
    }

    private suspend fun handleFacebookAccessToken(token: com.facebook.AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential).await()
        updateUI()

        val intent = Intent(this, PersonalInformationActivity::class.java)
        startActivity(intent)
    }

    private fun signOut() {
        auth.signOut()
        LoginManager.getInstance().logOut()
        updateUI()
    }

    private fun updateUI() {
        if (auth.currentUser != null) {
            binding.statusTextView.text = "Signed in as ${auth.currentUser?.displayName}"
            binding.signInButton.visibility = android.view.View.GONE
            binding.signOutButton.visibility = android.view.View.VISIBLE
        } else {
            binding.statusTextView.text = "You are not signed in."
            binding.signInButton.visibility = android.view.View.VISIBLE
            binding.signOutButton.visibility = android.view.View.GONE
        }
    }
}