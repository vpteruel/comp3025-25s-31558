package com.example.final_assignment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.final_assignment.databinding.ActivityMediaMapOptionsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MediaMapOptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaMapOptionsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMediaMapOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.submitButton.setOnClickListener {
            savePersonalInformation()
        }

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

    override fun onStart() {
        super.onStart()
        loadPersonalInformation()
    }

    private fun loadPersonalInformation() {
        val user = auth.currentUser
        if (user != null) {

            setLoading(true)

            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(user.uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    setLoading(false)
                    if (document != null && document.exists()) {
                        val question = document.getString("question")
                        val answer = document.getString("answer")
                        binding.questionSpinner.setSelection(getIndex(binding.questionSpinner, question))
                        binding.answerTextInputEditText.setText(answer)
                    } else {
                        Toast.makeText(this, "User document does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(this, "Error loading personal information: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun savePersonalInformation() {
        val question = binding.questionSpinner.selectedItem.toString()
        val answer = binding.answerTextInputEditText.text.toString()

        if (question.isEmpty() || answer.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null) {
            setLoading(true)

            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(user.uid)
            val personalInfo = hashMapOf(
                "question" to question,
                "answer" to answer
            )
            userRef.set(personalInfo, SetOptions.merge())
                .addOnSuccessListener {
                    setLoading(false)
                    Toast.makeText(this, "Personal information saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(this, "Error saving personal information: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getIndex(spinner: Spinner, value: String?): Int {
        if (value == null) {
            return 0
        }
        val adapter = spinner.adapter
        for (position in 0 until adapter.count) {
            if (adapter.getItem(position).toString() == value) {
                return position
            }
        }
        return 0
    }

    private fun setLoading(isLoading: Boolean) {
        binding.signUpProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.mapsButton.isEnabled = !isLoading
        binding.mediaPhotoButton.isEnabled = !isLoading
        binding.mediaVideoButton.isEnabled = !isLoading
        binding.questionSpinner.isEnabled = !isLoading
        binding.answerTextInputLayout.isEnabled = !isLoading
        binding.submitButton.isEnabled = !isLoading
        binding.backButton.isEnabled = !isLoading
        binding.exitButton.isEnabled = !isLoading
    }
}