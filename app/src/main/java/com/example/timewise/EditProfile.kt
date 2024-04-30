package com.example.timewise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditProfile : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        prefillUserData()

        val nameEditText: EditText = findViewById(R.id.txtEditName)
        val emailEditText: EditText = findViewById(R.id.txtEditEmail)
        val passwordEditText: EditText = findViewById(R.id.txtEditPassword)
        val btnConfirmChanges: Button = findViewById(R.id.btnConfirmChanges)

        btnConfirmChanges.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Validate inputs (if necessary) then update user details
            if (name.isNotBlank() && password.isNotBlank()) {
                UserManager.updateUser(email, name, password)
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please fill out all the fields", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun prefillUserData() {

        val userEmail = UserData.loggedUserEmail

        UserManager.getUserByEmail(userEmail)?.let { user ->
            findViewById<EditText>(R.id.txtEditName).setText(user.name)
            findViewById<EditText>(R.id.txtEditEmail).setText(user.email)
            findViewById<EditText>(R.id.txtEditPassword).setText(user.password)
        }
    }
}