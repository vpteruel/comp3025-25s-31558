package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.assignment2.databinding.ActivitySignup2Binding
import android.util.Patterns
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

class Signup2Activity : AppCompatActivity() {

    private val pageName = "Signup2Activity"
    private lateinit var binding: ActivitySignup2Binding
    private lateinit var auth: FirebaseAuth

    private val passwordPattern = Pattern.compile(
        "^(?=.*[A-Z])(?=.*[!@#\$%^&+=_()-])(?=\\S+$).{8,15}$"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignup2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.buttonPerformSignup2.setOnClickListener {
            performSignup2()
        }

        binding.buttonBackToMainFromSignup2.setOnClickListener {
            finish()
        }
    }

    private fun performSignup2() {
        val email = binding.editTextEmailSignup2.text.toString().trim()
        val password = binding.editTextPasswordSignup2.text.toString().trim()
        val confirmPassword = binding.editTextConfirmPasswordSignup2.text.toString().trim()

        if (!isValidEmail(email)) {
            Log.d(pageName, "Invalid email format: $email")
            binding.textFieldEmailLayoutSignup2.error = "Invalid email format"
            binding.editTextEmailSignup2.requestFocus()
            Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show()
            return
        } else {
            binding.textFieldEmailLayoutSignup2.error = null
        }

        val passwordValidationResult = isValidPassword(password)
        if (!passwordValidationResult.isValid) {
            Log.d(pageName, "Invalid password format: $password")
            binding.textFieldPasswordLayoutSignup2.error = passwordValidationResult.errorMessage
            binding.editTextPasswordSignup2.requestFocus()
            Toast.makeText(this, passwordValidationResult.errorMessage, Toast.LENGTH_LONG).show()
            return
        } else {
            binding.textFieldPasswordLayoutSignup2.error = null
        }

        if (password != confirmPassword) {
            Log.d(pageName, "Passwords do not match: $password, $confirmPassword")
            binding.textFieldConfirmPasswordLayoutSignup2.error = "Passwords do not match"
            binding.editTextConfirmPasswordSignup2.requestFocus()
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        } else {
            binding.textFieldConfirmPasswordLayoutSignup2.error = null
        }

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(pageName, "createUserWithEmail:success")
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let {
                        saveUserToFirestore(it.uid, email)
                    }
                } else {
                    setLoading(false)
                    Log.w(pageName, "createUserWithEmail:failure", task.exception)
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(baseContext, "Email address already in use.", Toast.LENGTH_LONG).show()
                        binding.textFieldEmailLayoutSignup2.error = "Email address already in use"
                    } else {
                        Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun saveUserToFirestore(userId: String, email: String) {
        val db = Firebase.firestore
        val user = hashMapOf(
            "email" to email,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("usersV2").document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d(pageName, "User (email) successfully written to Firestore!")
                setLoading(false)
                Toast.makeText(baseContext, "Sign up successful! Please login.", Toast.LENGTH_LONG).show()

                val intent = Intent(this, Login2Activity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finishAffinity()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Log.w(pageName, "Error writing user (email) to Firestore", e)
                Toast.makeText(baseContext, "Failed to save user details: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    data class PasswordValidationResult(val isValid: Boolean, val errorMessage: String? = null)

    private fun isValidPassword(password: String): PasswordValidationResult {
        if (password.length < 8 || password.length > 15) {
            return PasswordValidationResult(false, "Password must be 8-15 characters long.")
        }
        if (!password.matches(Regex(".*[A-Z].*"))) {
            return PasswordValidationResult(false, "Password must include at least one capital letter.")
        }
        if (!password.matches(Regex(".*[!@#\$%^&+=_()-].*"))) {
            return PasswordValidationResult(false, "Password must include at least one special character (e.g., !@#\$%^&+=_()-).")
        }
        if (!passwordPattern.matcher(password).matches()) {
            return PasswordValidationResult(false, "Password does not meet all criteria (8-15 chars, 1 uppercase, 1 special).")
        }
        return PasswordValidationResult(true)
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarSignup2.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonPerformSignup2.isEnabled = !isLoading
        binding.buttonBackToMainFromSignup2.isEnabled = !isLoading
        binding.editTextEmailSignup2.isEnabled = !isLoading
        binding.editTextPasswordSignup2.isEnabled = !isLoading
        binding.editTextConfirmPasswordSignup2.isEnabled = !isLoading
    }
}