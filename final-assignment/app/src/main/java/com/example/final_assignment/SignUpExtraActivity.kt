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
import com.example.final_assignment.databinding.ActivitySignUpExtraBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class SignUpExtraActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpExtraBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpExtraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

        binding.sendCodeButton.setOnClickListener {
            sendVerificationCode()
        }

        binding.verifyButton.setOnClickListener {
            verifyCode()
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpExtra)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun sendVerificationCode() {
        val phoneNumber = binding.phoneTextInputEditText.text.toString().trim()
        if (phoneNumber.isNotEmpty()) {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
            Toast.makeText(this, "Sending verification code", Toast.LENGTH_SHORT).show()
            binding.verificationCodeTextInputLayout.visibility = View.VISIBLE
            binding.verifyButton.visibility = View.VISIBLE
            binding.sendCodeButton.visibility = View.GONE
        } else {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("SignUpExtraActivity", "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w("SignUpExtraActivity", "onVerificationFailed", e)
            Toast.makeText(applicationContext, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()

            binding.verificationCodeTextInputLayout.visibility = View.GONE
            binding.verifyButton.visibility = View.GONE
            binding.sendCodeButton.visibility = View.VISIBLE
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d("SignUpExtraActivity", "onCodeSent:$verificationId")

            this@SignUpExtraActivity.verificationId = verificationId
        }
    }

    private fun verifyCode() {
        val code = binding.verificationCodeTextInputEditText.text.toString().trim()
        if (code.isNotEmpty() && verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        } else {
            Toast.makeText(this, "Please enter verification code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show()

                    sharedPreferences.edit { putString(MainActivity.KEY_USERNAME, user?.phoneNumber) }
                } else {
                    Toast.makeText(this, "Sign in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}