package com.example.myapplication11111111111

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

private lateinit var googleSignInClient: GoogleSignInClient
private lateinit var auth: FirebaseAuth
private val google_sign_in_code = 123


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)

        findViewById<Button>(R.id.btnGoogleSignIn).setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            // startActivity(SignInIntent)
            startActivityForResult(signInIntent, google_sign_in_code)
            // I will give my request an ID, this ID is like a tracking number
            // I can use the ID to check on my request; if my request was successful or not
            // check for me the results of the request that has the ID (123, which is the ID of my google_sign_in_id))
            // Checking the results will be done in the OnActivity method

        }







        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == google_sign_in_code) { // Check if this result belongs to Google Sign-In
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            if (task.isSuccessful) { // Check if sign-in was successful
                val account = task.result // Extract user account details
                val idToken = account.idToken // Get ID token

                // Create credentials for Firebase Authentication
                val credential = GoogleAuthProvider.getCredential(idToken,null)

                // .getCredential needs two parameters to get the data ,
                // usually the second parameter is the accessToken
                // for Firebase auth , we only need the idToken ,
                // since It is mandatory to pass two parameters, we will pass the second as null


                println("Google Sign-In Successful")
            } else {
                println("Google Sign-In Failed")
            }
        }
    }
}


/* STEPS:
* 1- Create Android project and Firebase project
* 2- Connect the android project to the Firebase project
* 3- Make sure that the connection is successful
* 4- connect Android project to Firebase authentication
* 5- make sure that the connection was successful
* 6- take the fingerprint of our android project and give it to  firebase

7- specify the login method ( start with google)
  - from auth we specify to login using google
  - firebase auth will create a new .json file
  - sub the old .josn file with the new one
  - after the connection is built , we need dependencies to use this connection.

  8- Good to start coding about the google sign up process:
  * 8.1: a class for google sign up and a class to help the google class ( firebase auth)
  * the firebase auth will always be there to help the first class ( regardless if it google class , facebook...)
  * Create copies from these classes
  *
  * 8.2: the firebase auth object is the main object, this object will handle the data submitted to firebase auth
  * regardless if the data is username / password or a report about the user ( from google account -> firebase authentication)
  * I need the user to sign up for one time only , but our code will be executed each time the user use our app. In this case,
  * each time , a new object called auth will be created that will handle the sign up process !! so each time the user will be considered
  * as a new user, to avoid this issue , we will capture the auth object when the user logs in for the first time , so the object has the data
  * of the user , and then we will use this capture always.
  *
  * 8.3: create a variable that will specify the parameters found in the report that the google account will send to firebase authentication
  * 8.4 : give the variable to the googleSignInClient , so we will initialize the googleSignInClient object, the role of this object is to use gso variable in the
  * main page to get the data needed in my report ( in our case, it will get the default parameters which are the username, email, and profile pic )
  *
  * 8.5: create a sign up button in the xml file
  * 8.6: add a click listener to the button, when the user clicks on the button, we will create an order,
  * the order is to tell my googleSignInClient object to to its task ( apply the variable gso and have the report)
  *
  *
  *
  *
  * if the request code is the google_sign_in_code then use the
  * method to get information about my request .and save it in a variable . The information will include the report created and other information as well
  *
  * if the request was successfully created and data about my request were successfully added in the the variable then extract only the report we need
  * and put it in another variable
  *
  * each report created will have an ID , we use the ID to get data from the report . Example: Get me the credentials from the report that has the ID = ....
  *
* */



// in the app folder:
// gradle file --> dependencies - external libraries that allows to use a tool,feature...
// .json --> it will allow us connect to external platforms*/
