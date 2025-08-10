package com.example.final_assignment

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.final_assignment.databinding.ActivityMediaMapOptionsBinding

class MediaMapOptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaMapOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMediaMapOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapsButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        binding.mediaPhotoButton.setOnClickListener {
            val intent = Intent(this, MediaPhotoActivity::class.java)
            startActivity(intent)
        }

        binding.mediaVideoButton.setOnClickListener {
            val intent = Intent(this, MediaVideoActivity::class.java)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.exitButton.setOnClickListener {
            finishAffinity()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mediaMapOptions)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}