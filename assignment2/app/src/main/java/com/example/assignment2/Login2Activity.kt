package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.assignment2.databinding.ActivityLogin2Binding
import android.util.Patterns
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login2Activity : AppCompatActivity() {

    private val pageName = "Login2Activity"
    private lateinit var binding: ActivityLogin2Binding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.buttonPerformLogin2.setOnClickListener {
            performLogin()
        }

        binding.buttonBackToMainFromLogin2.setOnClickListener {
            finish()
        }

        binding.textViewGoToSignUp2.setOnClickListener {
            startActivity(Intent(this, Signup2Activity::class.java))
        }
    }

    private fun performLogin() {
        val email = binding.editTextEmailLogin2.text.toString().trim()
        val password = binding.editTextPasswordLogin2.text.toString().trim()

        if (!isValidEmail(email)) {
            Log.e(pageName, "Invalid email format: $email")
            binding.textFieldEmailLayoutLogin2.error = "Invalid email format"
            binding.editTextEmailLogin2.requestFocus()
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
            return
        } else {
            binding.textFieldEmailLayoutLogin2.error = null
        }

        if (password.isEmpty()) {
            Log.e(pageName, "Password is empty")
            binding.textFieldPasswordLayoutLogin2.error = "Password is required"
            binding.editTextPasswordLogin2.requestFocus()
            Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show()
            return
        } else {
            binding.textFieldPasswordLayoutLogin2.error = null
        }

        setLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    Log.d(pageName, "signInWithEmail:success - ${auth.currentUser?.email}")
                    Toast.makeText(baseContext, "Login Successful.", Toast.LENGTH_SHORT).show()

                    navigateToWelcomePage(email.substringBefore('@'))
                } else {
                    Log.w(pageName, "signInWithEmail:failure", task.exception)
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidUserException -> "No account found with this email. Please Sign Up."
                        is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Try again!"
                        else -> "Login failed. ${exception?.message ?: "Please try again."}"
                    }
                    Toast.makeText(baseContext, errorMessage, Toast.LENGTH_LONG).show()

                    if (exception is FirebaseAuthInvalidCredentialsException) {
                        binding.editTextPasswordLogin2.requestFocus()
                        binding.textFieldPasswordLayoutLogin2.error = " "
                    } else if (exception is FirebaseAuthInvalidUserException) {
                        binding.editTextEmailLogin2.requestFocus()
                        binding.textFieldEmailLayoutLogin2.error = " "
                    }
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun navigateToWelcomePage(userName: String) {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.putExtra("USER_NAME", userName)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarLogin2.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonPerformLogin2.isEnabled = !isLoading
        binding.buttonBackToMainFromLogin2.isEnabled = !isLoading
        binding.editTextEmailLogin2.isEnabled = !isLoading
        binding.editTextPasswordLogin2.isEnabled = !isLoading
    }
}