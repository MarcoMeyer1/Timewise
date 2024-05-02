package com.example.timewise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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

        loadSettings()  // Call to load settings

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
        editor.putString("MinHours", minHours ?: "0") // Default to "0" if null
        editor.putString("MaxHours", maxHours ?: "0") // Default to "0" if null
        editor.apply()
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        // Load the settings with defaults if they are not set yet
        val minHours = prefs.getString("MinHours", "0")
        val maxHours = prefs.getString("MaxHours", "0")

        // Update UI with loaded settings
        minHoursEditText.setText(minHours)
        maxHoursEditText.setText(maxHours)
    }
}
