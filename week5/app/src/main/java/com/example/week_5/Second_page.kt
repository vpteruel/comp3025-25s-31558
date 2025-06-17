package com.example.week_5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.week_5.databinding.ActivitySecondPageBinding

class Second_page : AppCompatActivity() {
    private lateinit var binding_2 :ActivitySecondPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding_2 = ActivitySecondPageBinding.inflate(layoutInflater)
        setContentView(binding_2.root)

        val message = intent.getStringExtra("Amazon")

        binding_2.textFromPreviousPage.text = message


        // go back when the user clicks on go back button:
        binding_2.buttonToPreviousPage.setOnClickListener {
            // create a new order to go to the previous page
            finish()  // will close this page and go to the previous one
        }
    }

}