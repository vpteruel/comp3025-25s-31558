package com.example.week6

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Intent
import android.widget.Toast

import android.content.SharedPreferences

// SharedPreferences is a class that will give me access to my storage

import com.example.week6.databinding.ActivitySignUpBinding


class SignUp : AppCompatActivity() {

    private lateinit var binding:ActivitySignUpBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateAccount.setOnClickListener {

            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            // text we get from the user are : "Editable" datatype
            // we convert them to strings for comparison later ( compare this
            /// value to a value in a database ( string)

            // check if the passwords match:
            if (password != confirmPassword){
                Toast.makeText(this,"Password do not match",Toast.LENGTH_SHORT).show()
            }
            else{
                val sharedPreferences: SharedPreferences = getSharedPreferences("UserPref",MODE_PRIVATE)
                val editor : SharedPreferences.Editor = sharedPreferences.edit()

                editor.putString("USERNAME" , username)
                editor.putString("Password" , password)
                editor.apply()

                Toast.makeText(this,"Account Created !" , Toast.LENGTH_SHORT).show()
            }

            // go to the main page using back button:

        }

        binding.btnBackToMain.setOnClickListener {

            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}