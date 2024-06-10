package com.example.timewise

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val passwordEditText: EditText = findViewById(R.id.txtRegisterPassword)
        val confirmPasswordEditText: EditText = findViewById(R.id.txtRegisterConfirmPassword)
        val nameEditText: EditText = findViewById(R.id.txtRegisterName)
        val emailEditText: EditText = findViewById(R.id.txtRegisterEmail)
        val registerButton: Button = findViewById(R.id.btnRegisterAccount)


        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        val userId = firebaseUser?.uid ?: ""

                        // Save user details to the database
                        val userRef = database.getReference("users").child(userId)
                        val user = User(name, email, password)
                        userRef.setValue(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()

                                // Navigate to the Login Activity
                                val intent = Intent(this, Login::class.java)
                                startActivity(intent)
                                finish() // Finish this activity so the user can't return to it
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // If registration fails, display a message to the user.
                        Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
    data class User(
        var name: String,
        var email: String,
        var password: String
    )
}