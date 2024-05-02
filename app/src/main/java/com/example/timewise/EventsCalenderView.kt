package com.example.timewise

import EventCreationDialogFragment
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.net.Uri
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventsCalenderView : AppCompatActivity() {

    private lateinit var selectedImageUri: Uri
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_events_calender_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnAddEvent: Button = findViewById(R.id.btnAddEvent)
        btnAddEvent.setOnClickListener {

            EventCreationDialogFragment().show(supportFragmentManager, "createEvent")
        }
    }

    private fun showEventCreationPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_layout, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val txtEventName = popupView.findViewById<EditText>(R.id.txtEventName)
        val btnStartDate = popupView.findViewById<Button>(R.id.btnStartDate)
        val btnEndDate = popupView.findViewById<Button>(R.id.btnEndDate)
        val allDaySwitch = popupView.findViewById<Switch>(R.id.allDaySwitch)
        val categorySpinner = popupView.findViewById<Spinner>(R.id.categorySpinner)
        val btnAddPhoto = popupView.findViewById<Button>(R.id.btnAddPhoto)
        val btnCreateEvent = popupView.findViewById<Button>(R.id.btnCreateEvent)

        btnStartDate.setOnClickListener {
            showDateTimePickerDialog(this, true, btnStartDate)
        }

        btnEndDate.setOnClickListener {
            showDateTimePickerDialog(this, false, btnEndDate)
        }

        btnAddPhoto.setOnClickListener {
            openPhotoPicker()
        }

        btnCreateEvent.setOnClickListener {
            val eventName = txtEventName?.text.toString()
            val allDay = allDaySwitch?.isChecked ?: false
            val category = categorySpinner?.selectedItem.toString()
            val imageUri = selectedImageUri



            if (startDate != null && endDate != null) {
                val timesheetEntry = TimesheetEntry(
                    eventName,
                    startDate!!,
                    endDate!!,
                    allDay,
                    category,
                    imageUri
                )

                val dummyTimesheet = TimesheetRepository.getDummyTimesheet()
                TimesheetManager.addTimesheetEntry(dummyTimesheet.id, timesheetEntry)

                Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show()


            } else {
                Toast.makeText(this, "Please set both start and end dates", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }



    private fun openPhotoPicker() {

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

            if (uri != null) {
                selectedImageUri = uri
                Log.d("PhotoPicker", "Selected URI: $uri")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    private fun showDateTimePickerDialog(context: Context, isStartDate: Boolean, dateButton: Button) {
        val calendar = if (isStartDate) startDate ?: Calendar.getInstance() else endDate ?: Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        val formattedDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                        dateButton.text = formattedDateTime

                        if (isStartDate) startDate = calendar else endDate = calendar
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}


