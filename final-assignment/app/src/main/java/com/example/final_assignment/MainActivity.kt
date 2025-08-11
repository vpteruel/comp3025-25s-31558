package com.example.final_assignment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.example.final_assignment.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var credentialManager: CredentialManager
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        credentialManager = CredentialManager.create(this)
        auth = FirebaseAuth.getInstance()

        binding.signUpGoogleButton.setOnClickListener {
            val intent = Intent(this, SignUpGoogleActivity::class.java)
            startActivity(intent)
        }

        binding.signUpFacebookButton.setOnClickListener {
            val intent = Intent(this, SignUpFacebookActivity::class.java)
            startActivity(intent)
        }

        binding.signUpEmailButton.setOnClickListener {
            val intent = Intent(this, SignUpEmailActivity::class.java)
            startActivity(intent)
        }

        binding.signUpExtraButton.setOnClickListener {
            val intent = Intent(this, SignUpExtraActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.signOutButton.setOnClickListener {
            signOut()
        }

        binding.nextButton.setOnClickListener {
            if (auth.currentUser != null) {
                val intent = Intent(this, PersonalInformationActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI(currentUser)
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val username = user.email ?: user.phoneNumber ?: user.displayName

            binding.loginButton.visibility = View.GONE
            binding.signOutButton.visibility = View.VISIBLE
            binding.statusTextView.text = "Signed in as: ${username}"

            Log.d(TAG, "User signed in: ${username}")
        } else {
            binding.loginButton.visibility = View.VISIBLE
            binding.signOutButton.visibility = View.GONE
            binding.statusTextView.text = "You are not signed in."

            Log.d(TAG, "Sign up failed or user is signed out")
        }
    }

    private fun signOut() {
        lifecycleScope.launch {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
        auth.signOut()
        updateUI(null)
    }
}