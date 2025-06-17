package com.example.week6

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.week6.databinding.ActivityMainBinding

import android.content.Intent
import android.net.Uri

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // initial values for the created binding object:
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // navigate to the sign up page:
        binding.btnSignup.setOnClickListener {
            // send an order to the sign up page to open
            // order = intent
            // to send intent : we create the intent then send
            val intent = Intent(this,SignUp::class.java)
            startActivity(intent)
        }

        // navigate to the login page:
        binding.btnLogin.setOnClickListener {
            val intent = Intent(this,Login :: class.java)
            startActivity(intent)

        }

        // navigate to the updates link

        binding.btnUpdates.setOnClickListener {

            // intent to go to url
            // If we want our code to access any information
            // It will access the information in the from : Uri
            // We want to access url ( link) ,
            // we will give the link in the form of uri

            val updatesUrl = "https://github.blog/changelog/"
            // create the order (intent)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updatesUrl))
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}