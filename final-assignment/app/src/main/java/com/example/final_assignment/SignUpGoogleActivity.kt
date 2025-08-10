package com.example.final_assignment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.final_assignment.databinding.ActivitySignUpGoogleActivityBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpGoogleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpGoogleActivityBinding
    private lateinit var credentialManager: CredentialManager
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "SignUpGoogleActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpGoogleActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        credentialManager = CredentialManager.create(this)
        auth = FirebaseAuth.getInstance()

        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

        binding.signInButton.setOnClickListener {
            lifecycleScope.launch {
                signIn()
            }
        }

        binding.signOutButton.setOnClickListener {
            signOut()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.nextButton.setOnClickListener {
            if (auth.currentUser != null) {
                val intent = Intent(this, PersonalInformationActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpGoogle)) { v, insets ->
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

    private suspend fun signIn() {
        if (auth.currentUser != null) {
            updateUI(auth.currentUser)
            return
        }

        try {
            val response = buildCredentialRequest()
            handleSignIn(response)
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Sign up failed", e)
            when (e.message) {
                "Request canceled by user." -> {
                    Toast.makeText(
                        this@SignUpGoogleActivity,
                        "Sign up canceled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    Toast.makeText(
                        this@SignUpGoogleActivity,
                        "Sign up failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign up", e)
            if (e is CancellationException) throw e
            Toast.makeText(
                this@SignUpGoogleActivity,
                "An unexpected error occurred: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private suspend fun buildCredentialRequest(): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()

        return credentialManager.getCredential(
            request = request, context = this@SignUpGoogleActivity
        )
    }

    private suspend fun handleSignIn(response: GetCredentialResponse) {
        val credential = response.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                updateUI(authResult.user)

                val username = authResult.user?.email ?: ""
                sharedPreferences.edit { putString(MainActivity.KEY_USERNAME, username) }
            } catch (e: GoogleIdTokenParsingException) {
                Log.w(TAG, "Error parsing Google ID token", e)
                updateUI(null)
            } catch (e: Exception) {
                Log.e(TAG, "Firebase auth failed", e)
                updateUI(null)
            }
        } else {
            Log.w(TAG, "Invalid credential type")
            Toast.makeText(this, "Invalid credential type", Toast.LENGTH_SHORT).show()
            updateUI(null)
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            binding.signInButton.visibility = View.GONE
            binding.signOutButton.visibility = View.VISIBLE
            binding.statusTextView.text = "Signed in as: ${user.email}"

            Log.d(TAG, "User signed in: ${user.email}")
            Toast.makeText(this, "Welcome ${user.displayName ?: user.email}!", Toast.LENGTH_SHORT).show()
        } else {
            binding.signInButton.visibility = View.VISIBLE
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