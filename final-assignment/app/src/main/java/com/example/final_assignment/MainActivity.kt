package com.example.final_assignment

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.final_assignment.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}