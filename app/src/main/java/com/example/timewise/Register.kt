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
import com.bumptech.glide.Glide

class Register : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Load image from Google Drive link using Glide
        Glide.with(this)
            .load("https://drive.google.com/uc?export=view&id=1UIMVG78OboB96ra0KPrLa4D1risr4TW4")
            .into(findViewById(R.id.imgTimeWiseLogo))

        val passwordEditText: EditText = findViewById(R.id.txtRegisterPassword)
        val confirmPasswordEditText: EditText = findViewById(R.id.txtRegisterConfirmPassword)
        val nameEditText: EditText = findViewById(R.id.txtRegisterName)
        val emailEditText: EditText = findViewById(R.id.txtRegisterEmail)
        val registerButton: Button = findViewById(R.id.btnRegisterAccount)

        val skip: Button = findViewById(R.id.btnSkipToHomepage)

        skip.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
            finish()
        }

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



            val user = User(name, email, password)
            UserManager.addUser(user)
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()

            // Navigate to the Login Activity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish() // Finish this activity so the user can't return to it
        }
    }
}

data class User(val name: String, val email: String, val password: String)
