package com.example.timewise

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder

class NewTimesheet : BaseActivity() {
    private lateinit var selectedCategory: String
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

        val spinner: Spinner = findViewById(R.id.dropdownCategory)
        val button: Button = findViewById(R.id.btnSelectColor)
        val confirmButton: Button = findViewById(R.id.btnCreateNewTimesheet)

        // Setup spinner with adapter
        ArrayAdapter.createFromResource(
            this,
            R.array.dropdown_items,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        // Setup color picker button
        button.setOnClickListener {
            ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("ok") { dialog, selectedColor, allColors ->
                    // Save selected color in hexadecimal format
                    selectedColorHex = String.format("#%06X", 0xFFFFFF and selectedColor)
                    // Change the button background color to the selected color
                    button.setBackgroundColor(selectedColor)
                }
                .setNegativeButton("cancel") { dialog, which -> }
                .build()
                .show()
        }

        // Confirm button action
        confirmButton.setOnClickListener {
            // Get selected item from spinner
            selectedCategory = spinner.selectedItem.toString()
            // Here you can use `selectedCategory` and `selectedColorHex` for further actions
            // For example, save to a database or send through an API
        }
    }
}
