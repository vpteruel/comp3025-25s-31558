package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.assignment2.databinding.ActivityLoginBinding
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private val pageName = "LoginActivity"
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.buttonLogin.setOnClickListener {
            performLogin()
        }

        binding.buttonBackToMainFromLogin.setOnClickListener {
            finish()
        }

        binding.textViewGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun performLogin() {
        val usernameOrEmail = binding.editTextUsername.text.toString().trim() // User might enter username or email
        val password = binding.editTextPassword.text.toString().trim()

        if (usernameOrEmail.isEmpty()) {
            Log.d(pageName, "Username or Email is required")
            binding.textFieldUsernameLayout.error = "Username or Email is required"
            binding.editTextUsername.requestFocus()
            return
        } else {
            binding.textFieldUsernameLayout.error = null
        }

        if (password.isEmpty()) {
            Log.d(pageName, "Password is required")
            binding.textFieldPasswordLayout.error = "Password is required"
            binding.editTextPassword.requestFocus()
            return
        } else {
            binding.textFieldPasswordLayout.error = null
        }

        setLoading(true)

        if (usernameOrEmail.contains("@")) {
            Log.d(pageName, "Attempting login with email")
            signInWithFirebase(usernameOrEmail, password)
        } else {
            Log.d(pageName, "Attempting login with username")
            queryFirestoreForEmail(usernameOrEmail, password)
        }
    }

    private fun queryFirestoreForEmail(username: String, passwordToAuth: String) {
        val db = Firebase.firestore
        db.collection("users")
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(pageName, "No user found with username: $username")
                    showLoginError()
                    setLoading(false)
                } else {
                    for (document in documents) {
                        val firebaseAuthEmail = "$username@example.com"
                        Log.d(pageName, "Username found. Attempting auth with email: $firebaseAuthEmail")
                        signInWithFirebase(firebaseAuthEmail, passwordToAuth, username)
                        return@addOnSuccessListener
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(pageName, "Error getting documents: ", exception)
                showLoginError()
                setLoading(false)
            }
    }

    private fun signInWithFirebase(emailForAuth: String, passwordToAuth: String, displayName: String? = null) {
        auth.signInWithEmailAndPassword(emailForAuth, passwordToAuth)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(pageName, "signInWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(baseContext, "Login Successful.", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, WelcomeActivity::class.java)

                    val nameToShow = displayName ?: emailForAuth.substringBefore('@')
                    intent.putExtra("USER_NAME", nameToShow)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.w(pageName, "signInWithEmail:failure", task.exception)
                    showLoginError()
                }
                setLoading(false)
            }
    }

    private fun showLoginError() {
        Toast.makeText(baseContext, "Username or password is incorrect. Try again!", Toast.LENGTH_LONG).show()
        binding.textFieldPasswordLayout.error = " "
        binding.textFieldUsernameLayout.error = " "
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
        binding.buttonBackToMainFromLogin.isEnabled = !isLoading
        binding.editTextUsername.isEnabled = !isLoading
        binding.editTextPassword.isEnabled = !isLoading
    }
}