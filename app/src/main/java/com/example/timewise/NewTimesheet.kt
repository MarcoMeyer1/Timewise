package com.example.timewise

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class NewTimesheet : BaseActivity() {
    private lateinit var timesheetNameEditText: EditText
    private var selectedColorHex: String = "#FFFFFF"
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseOperationsManager: DatabaseOperationsManager
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_timesheet)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseDatabase.getInstance()
        databaseOperationsManager = DatabaseOperationsManager(this)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        updateToolbarColor("#B378FE")
        timesheetNameEditText = findViewById(R.id.txtNewTimesheetName)
        val buttonSelectColor: Button = findViewById(R.id.btnSelectColor)
        val buttonCreate: Button = findViewById(R.id.btnCreateNewTimesheet)

        // Setup color picker button
        buttonSelectColor.setOnClickListener {
            ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("OK") { dialog, selectedColor, allColors ->
                    selectedColorHex = String.format("#%06X", 0xFFFFFF and selectedColor)
                    buttonSelectColor.setBackgroundColor(selectedColor)
                }
                .setNegativeButton("Cancel", null)
                .build()
                .show()
        }

        buttonCreate.setOnClickListener {
            val timesheetName = timesheetNameEditText.text.toString()

            if (timesheetName.isEmpty()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedColorHex.isEmpty() || selectedColorHex == "#FFFFFF") {
                Toast.makeText(this, "Please select a color", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentUser?.let { user ->
                val userId = user.uid
                val timesheetId = "timesheet_${System.currentTimeMillis()}"
                databaseOperationsManager.createTimesheet(
                    db,
                    userId,
                    timesheetId,
                    timesheetName,
                    selectedColorHex
                )
                val intent = Intent(this, ActiveTimesheetsPage::class.java)
                startActivity(intent)
                finish()
            } ?: run {
                Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
