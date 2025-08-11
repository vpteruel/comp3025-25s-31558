package com.example.final_assignment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.final_assignment.databinding.ActivityPersonalInformationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class PersonalInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInformationBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPersonalInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.submitButton.setOnClickListener {
            savePersonalInformation()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.nextButton.setOnClickListener {
            val intent = Intent(this, MediaMapOptionsActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.personalInformation)) { v, insets ->
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
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")
                        val age = document.getString("age")
                        val city = document.getString("city")
                        val country = document.getString("country")
                        binding.firstNameTextInputEditText.setText(firstName)
                        binding.lastNameTextInputEditText.setText(lastName)
                        binding.ageSpinner.setSelection(getIndex(binding.ageSpinner, age))
                        binding.citySpinner.setSelection(getIndex(binding.citySpinner, city))
                        binding.countrySpinner.setSelection(getIndex(binding.countrySpinner, country))
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
        val firstName = binding.firstNameTextInputEditText.text.toString()
        val lastName = binding.lastNameTextInputEditText.text.toString()
        val age = binding.ageSpinner.selectedItem.toString()
        val city = binding.citySpinner.selectedItem.toString()
        val country = binding.countrySpinner.selectedItem.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || age.isEmpty() || city.isEmpty() || country.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null) {
            setLoading(true)

            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(user.uid)
            val personalInfo = hashMapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "age" to age,
                "city" to city,
                "country" to country
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
        binding.firstNameTextInputLayout.isEnabled = !isLoading
        binding.lastNameTextInputLayout.isEnabled = !isLoading
        binding.ageSpinner.isEnabled = !isLoading
        binding.citySpinner.isEnabled = !isLoading
        binding.countrySpinner.isEnabled = !isLoading
        binding.submitButton.isEnabled = !isLoading
        binding.backButton.isEnabled = !isLoading
        binding.nextButton.isEnabled = !isLoading
    }
}