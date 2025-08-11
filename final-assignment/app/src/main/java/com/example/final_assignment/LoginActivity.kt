package com.example.final_assignment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.final_assignment.databinding.ActivityLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager
    private lateinit var callbackManager: CallbackManager

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "Facebook login canceled")
            }

            override fun onError(error: FacebookException) {
                Log.w(TAG, "Facebook login error", error)
            }
        })

        binding.loginButton.setOnClickListener {
            login()
        }

        binding.googleLoginButton.setOnClickListener {
            lifecycleScope.launch {
                googleLogin()
            }
        }

        binding.facebookLoginButton.setOnClickListener {
            facebookLogin()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun login() {
        val email = binding.emailTextInputEditText.text.toString()
        val password = binding.passwordTextInputEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Authentication successful.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, PersonalInformationActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private suspend fun googleLogin() {
        if (auth.currentUser != null) {
            startActivity(Intent(this, PersonalInformationActivity::class.java))
            finish()
            return
        }

        try {
            val response = buildGoogleSignInRequest()
            handleSignInWithGoogle(response)
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Sign in failed", e)
            when (e.message) {
                "Request canceled by user." -> {
                    Toast.makeText(this@LoginActivity, "Sign in canceled", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this@LoginActivity, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign in", e)
            if (e is CancellationException) throw e
            Toast.makeText(this@LoginActivity, "An unexpected error occurred: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun buildGoogleSignInRequest(): GetCredentialResponse {
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
            request = request, context = this@LoginActivity
        )
    }

    private suspend fun handleSignInWithGoogle(response: GetCredentialResponse) {
        val credential = response.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
                auth.signInWithCredential(authCredential).await()

                Toast.makeText(baseContext, "Google Authentication successful.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, PersonalInformationActivity::class.java))
                finish()

            } catch (e: GoogleIdTokenParsingException) {
                Log.w(TAG, "Error parsing Google ID token", e)
                Toast.makeText(baseContext, "Google Authentication failed.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Firebase auth failed", e)
                Toast.makeText(baseContext, "Google Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Invalid credential type")
            Toast.makeText(this, "Invalid credential type", Toast.LENGTH_SHORT).show()
        }
    }

    private fun facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
    }

    private fun handleFacebookAccessToken(token: com.facebook.AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Facebook Authentication successful.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, PersonalInformationActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(baseContext, "Facebook Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
