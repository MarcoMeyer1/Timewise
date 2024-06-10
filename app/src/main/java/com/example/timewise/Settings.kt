package com.example.timewise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Settings : BaseActivity() {
    private lateinit var minHoursEditText: EditText
    private lateinit var maxHoursEditText: EditText
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        minHoursEditText = findViewById(R.id.minHoursEditText)
        maxHoursEditText = findViewById(R.id.maxHoursEditText)
        confirmButton = findViewById(R.id.confirmButton)

        loadSettings()

        confirmButton.setOnClickListener {
            saveSettings(minHoursEditText.text.toString(), maxHoursEditText.text.toString())
            Toast.makeText(this, "Settings updated", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }
    }

    private fun saveSettings(minHours: String?, maxHours: String?) {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("MinHours", minHours ?: "0")
        editor.putString("MaxHours", maxHours ?: "0")
        editor.apply()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(userId).child("dailyGoal")

            val updates = mapOf(
                "minHours" to (minHours?.toIntOrNull() ?: 0),
                "maxHours" to (maxHours?.toIntOrNull() ?: 0)
            )

            userRef.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Min and Max hours updated successfully")
                } else {
                    showToast("Failed to update settings")
                }
            }
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val minHours = prefs.getString("MinHours", "0")
        val maxHours = prefs.getString("MaxHours", "0")

        minHoursEditText.setText(minHours)
        maxHoursEditText.setText(maxHours)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}