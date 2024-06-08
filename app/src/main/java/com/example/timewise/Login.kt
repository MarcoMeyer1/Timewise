package com.example.timewise

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets


        }


        var adminUser = User("Admin", "admin", "admin")
        UserManager.addUser(adminUser)

        val emailEditText: EditText = findViewById(R.id.txtLoginEmail)
        val passwordEditText: EditText = findViewById(R.id.txtLoginPassword)
        val loginButton: Button = findViewById(R.id.btnLogin)
        val backToRegisterButton: Button = findViewById(R.id.btnBackToRegister)
        backToRegisterButton.text = Html.fromHtml("<u>Don't have an account? Register Now</u>")

        backToRegisterButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }



        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = UserManager.findUser(email, password)
            if (user != null) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                UserData.loggedUserName = user.name
                UserData.loggedUserEmail = user.email

                val intent = Intent(this, HomePage::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

object UserData {
    var loggedUserName: String = ""
    var loggedUserEmail: String = ""

}
