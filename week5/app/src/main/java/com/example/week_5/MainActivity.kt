package com.example.week_5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.week_5.databinding.ActivityMainBinding

import  android.content.Intent
import android.net.Uri

class MainActivity : AppCompatActivity() {

    private lateinit var binding_1 : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding_1 = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding_1.root)

        // Next page button:
        /* Explicit Intent :
         when we specify which activity is executing our intent ( order)
        * usually within one application
        *
        * */
        // when we click on Next button, an explicit intent will be created
        // intent = order + we will send an optional box
        // The box should have a name and a value ( inside the box)

        binding_1.buttonNextPage.setOnClickListener {
            // to create an order we will call the Intent class
            // Then create a copy of this class and use it
            val intent = Intent(this,Second_page::class.java)
            // send an optional box
            intent.putExtra("Amazon","Welcome to page two")

            startActivity(intent)
        }

        /*  Implicit Intent: If you want to perform an order (intent) and you
        * don't know which application should execute your intent
        * We create the intent and Android System will detect which app on your
        * device will execute the order
        *
        * If Android detects more than one application to do the action, then it will
        * show you all the options and you choose  */

        // url and uri

        // Formats of Intents:
        // Explicit Intent : object_name = class_name ( page_1 , page_2)
        // ORDER --> Go from page 1 to page 2


        // Implicit Intent :
        // 1- to open or view a website
        // object_name = class_name (Intent.Action_VIEW , URI )
        // to get uri if you have url, we use parse



        binding_1.buttonOpenWebsite.setOnClickListener {

            // create a link to use in the intent
            val url = "http://www.google.com"
            // the intent we will create is Implicit Intent
            // we give the order and external activity in external application
            // will execute the order

            val WebIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            startActivity(WebIntent)

        }

        binding_1.buttonSendSMS.setOnClickListener {
            val phoneNumber = "911"
            val message = "I need medical help "

            // create the Implicit order to send a message
            val smsIntent = Intent(Intent.ACTION_SENDTO)
            // specify to whom we are sending --> we mention the receiver in data
            // section
            // The data taken should be in the format of uri
            smsIntent.data = Uri.parse("smsto:$phoneNumber")

            smsIntent.putExtra("note",message)


            startActivity(smsIntent)

            // 3 lines:
            // first --> specify the order ( I am sending something)
            // second --> specify the type ( ex: message , email..) and specify the receiver
            // Third --> specify the value of what you are sending ( give it a name)

        }





















    }
}