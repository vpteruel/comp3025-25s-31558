package com.example.final_assignment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.final_assignment.databinding.ActivitySignUpEmailActivityBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import android.content.Intent
import android.content.SharedPreferences
import com.google.firebase.firestore.firestore
import androidx.core.content.edit

class SignUpEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpEmailActivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpEmailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

        binding.signUpButton.setOnClickListener {
            signup()
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpEmail)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun signup() {
        val username = binding.usernameTextInputEditText.text.toString().trim()
        val password = binding.passwordTextInputEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordTextInputEditText.text.toString().trim()

        if (username.isEmpty()) {
            binding.usernameTextInputLayout.error = "Username is required"
            binding.usernameTextInputEditText.requestFocus()
            return
        } else {
            binding.usernameTextInputLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordTextInputLayout.error = "Password is required"
            binding.passwordTextInputEditText.requestFocus()
            return
        } else {
            binding.passwordTextInputLayout.error = null
        }

        if (password.length < 6) {
            binding.passwordTextInputLayout.error = "Password must be at least 6 characters"
            binding.passwordTextInputEditText.requestFocus()
            return
        } else {
            binding.passwordTextInputLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordTextInputLayout.error = "Confirm password is required"
            binding.confirmPasswordTextInputEditText.requestFocus()
            return
        } else {
            binding.confirmPasswordTextInputLayout.error = null
        }

        if (password != confirmPassword) {
            binding.confirmPasswordTextInputLayout.error = "Passwords do not match"
            binding.confirmPasswordTextInputEditText.requestFocus()
            return
        } else {
            binding.confirmPasswordTextInputLayout.error = null
        }

        setLoading(true)

        //val firebaseAuthEmail = "$username@gmail.com"

        auth.createUserWithEmailAndPassword(username, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let {
                        saveUsernameToFirestore(it.uid, username)
                    }
                } else {
                    setLoading(false)
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(baseContext, "Username might already be taken. Try a different one.", Toast.LENGTH_LONG).show()
                        binding.usernameTextInputLayout.error = "Username might be taken"
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
                setLoading(false)
                Toast.makeText(baseContext, "Signup successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, PersonalInformationActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(baseContext, "Failed to save user details: ${e.message}", Toast.LENGTH_LONG).show()
            }

        sharedPreferences.edit { putString(MainActivity.KEY_USERNAME, username) }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.signUpProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.signUpButton.isEnabled = !isLoading
        binding.backButton.isEnabled = !isLoading
        binding.usernameTextInputEditText.isEnabled = !isLoading
        binding.passwordTextInputEditText.isEnabled = !isLoading
        binding.confirmPasswordTextInputEditText.isEnabled = !isLoading
    }
}