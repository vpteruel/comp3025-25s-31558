package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.assignment2.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {

    private val pageName = "SignupActivity"
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.buttonPerformSignup.setOnClickListener {
            performSignup()
        }

        binding.buttonBackToMain.setOnClickListener {
            finish()
        }
    }

    private fun performSignup() {
        val username = binding.editTextUsername.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

        if (username.isEmpty()) {
            Log.d(pageName, "Username is required")
            binding.textFieldUsernameLayout.error = "Username is required"
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

        if (password.length < 6) {
            Log.d(pageName, "Password must be at least 6 characters")
            binding.textFieldPasswordLayout.error = "Password must be at least 6 characters"
            binding.editTextPassword.requestFocus()
            return
        } else {
            binding.textFieldPasswordLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            Log.d(pageName, "Confirm password is required")
            binding.textFieldConfirmPasswordLayout.error = "Confirm password is required"
            binding.editTextConfirmPassword.requestFocus()
            return
        } else {
            binding.textFieldConfirmPasswordLayout.error = null
        }

        if (password != confirmPassword) {
            Log.d(pageName, "Passwords do not match")
            binding.textFieldConfirmPasswordLayout.error = "Passwords do not match"
            binding.editTextConfirmPassword.requestFocus()
            return
        } else {
            binding.textFieldConfirmPasswordLayout.error = null
        }

        setLoading(true)

        val firebaseAuthEmail = "$username@hpha.ca"

        auth.createUserWithEmailAndPassword(firebaseAuthEmail, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(pageName, "createUserWithEmail:success")
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let {
                        saveUsernameToFirestore(it.uid, username)
                    }
                } else {
                    setLoading(false)
                    Log.w(pageName, "createUserWithEmail:failure", task.exception)
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(baseContext, "Username might already be taken. Try a different one.", Toast.LENGTH_LONG).show()
                        binding.textFieldUsernameLayout.error = "Username might be taken"
                    } else {
                        Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun saveUsernameToFirestore(userId: String, username: String) {
        val db = Firebase.firestore
        val user = hashMapOf(
            "username" to username,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d(pageName, "Username successfully written to Firestore!")
                setLoading(false)
                Toast.makeText(baseContext, "Signup successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Log.w(pageName, "Error writing username to Firestore", e)
                Toast.makeText(baseContext, "Failed to save user details: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarSignup.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonPerformSignup.isEnabled = !isLoading
        binding.buttonBackToMain.isEnabled = !isLoading
        binding.editTextUsername.isEnabled = !isLoading
        binding.editTextPassword.isEnabled = !isLoading
        binding.editTextConfirmPassword.isEnabled = !isLoading
    }
}