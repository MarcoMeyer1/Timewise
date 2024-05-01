package com.example.timewise

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder

class NewTimesheet : BaseActivity() {
    private lateinit var timesheetNameEditText: EditText
    private var selectedColorHex: String = "#FFFFFF" // Default white color

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_timesheet)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        timesheetNameEditText = findViewById(R.id.txtNewTimesheetName)
        val buttonSelectColor: Button = findViewById(R.id.btnSelectColor)
        val buttonCreate: Button = findViewById(R.id.btnCreateNewTimesheet)


        // Setup color picker button
        // Setup color picker dialog
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


        // Setup the create button
        buttonCreate.setOnClickListener {
            val timesheetName = timesheetNameEditText.text.toString()
            if (timesheetName.isNotEmpty()) {
                val newTimesheet = Timesheet(
                    id = TimesheetManager.timesheets.size + 1,  // Generate a new ID
                    name = timesheetName,
                    colorHex = selectedColorHex
                )
                TimesheetManager.addTimesheet(newTimesheet)
                val intent = Intent(this, ActiveTimesheetsPage::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
