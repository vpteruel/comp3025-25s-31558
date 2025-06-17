package com.example.week7

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import com.example.assignment1.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    private val pageName = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = Firebase.firestore

        val user = hashMapOf(
            "first_name" to "Vinicius",
            "last_name" to "Picossi Teruel",
            "born" to 1987,
            "email" to "fake@email"
        )

        // add
        db.collection("users")
            .document("first_user")
            .set(user)
            .addOnSuccessListener { documentReference ->
                Log.d(pageName, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(pageName, "Error adding document", e)
            }

        // get
        db.collection("users")
            .document("first_user")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("first_name")
                    Log.d(pageName, "First name: $name")
                } else {
                    Log.d(pageName, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(pageName, "Error getting documents.", exception)
            }

        // update
        db.collection("users")
            .document("first_user")
            .update("first_name", "Vinicius")
            .addOnSuccessListener {
                Log.d(pageName, "DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { exception ->
                Log.w(pageName, "Error updating document", exception)
            }

        // delete
        db.collection("users")
            .document("first_user")
            .delete()
            .addOnSuccessListener {
                Log.d(pageName, "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { exception ->
                Log.w(pageName, "Error deleting document", exception)
            }

        // get all
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(pageName, "${document.id} => ${document.data}")
                    val name = document.getString("first_name")
                    Log.d(pageName, "First name: $name")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(pageName, "Error getting documents.", exception)
            }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
