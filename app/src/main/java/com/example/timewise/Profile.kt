package com.example.timewise

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Profile : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        setupButtonListeners()
        setupTextViewFullName()
    }


    private fun setupTextViewFullName() {
        val fullName : TextView = findViewById(R.id.lblProfileFullName)
        fullName.text = UserData.loggedUserName
    }
    private fun setupButtonListeners() {


        val btnEditProfile: Button = findViewById(R.id.btnEditProfile)
        btnEditProfile.setOnClickListener {
            // Handle Edit Profile button click
        }

        val btnManageTimesheets: Button = findViewById(R.id.btnManageTimesheets)
        btnManageTimesheets.setOnClickListener {
            // Handle Manage Timesheets button click
        }

        val btnPrivacyPolicy: Button = findViewById(R.id.btnPrivacyPolicy)
        btnPrivacyPolicy.setOnClickListener {
            // Handle Privacy Policy button click
        }

        val btnSettings: Button = findViewById(R.id.btnSettings)
        btnSettings.setOnClickListener {
            // Handle Settings button click
        }

        val btnLogOut: Button = findViewById(R.id.btnLogOut)
        btnLogOut.setOnClickListener {
            // Handle Log Out button click
        }
    }
}