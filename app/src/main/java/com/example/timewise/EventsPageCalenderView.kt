package com.example.timewise

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*


class EventsPageCalenderView : BaseActivity() {

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var selectedPhotoPath: String? = null
    private val PICK_PHOTO_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_page_calender_view)

        val imgAddEvent = findViewById<ImageView>(R.id.imgAddEvent)
        imgAddEvent.setOnClickListener {
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
                showDateTimePickerDialog(btnStartDate)

            }

            btnEndDate.setOnClickListener {
                showDateTimePickerDialog(btnEndDate)
            }

            btnAddPhoto.setOnClickListener {
                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, PICK_PHOTO_REQUEST)
            }

            btnCreateEvent.setOnClickListener {
                val eventName = txtEventName.toString()
                val allDay = allDaySwitch.isChecked
                val category = categorySpinner.selectedItem.toString()
                val timesheetEntry = TimesheetEntry(eventName, startDate!!, endDate!!, allDay, category, photo)

            }





            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            selectedPhotoPath = selectedImageUri?.toString()
        }
    }



}

private fun showDateTimePickerDialog(dateButton: Button) {
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        this,
        DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            showTimePickerDialog(dateButton, calendar)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    datePickerDialog.show()
}

    private fun showTimePickerDialog(dateButton: Button, calendar: Calendar) {
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                val formattedDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                dateButton.text = formattedDateTime
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        timePickerDialog.show()
    }

