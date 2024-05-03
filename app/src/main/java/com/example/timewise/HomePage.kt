package com.example.timewise

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomePage : BaseActivity() {

    private val userName: String = UserData.loggedUserName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupWelcomeMessage(userName)
        setupCardButtons()

    }
    private fun setupWelcomeMessage(userName: String) {
        val lblWelcomeMessage: TextView = findViewById(R.id.lblWelcomeMessage)
        // Split the userName string into parts based on spaces
        val firstName = userName.split(" ").first()  // Takes the first part of the split string

        lblWelcomeMessage.text = "Welcome, $firstName"
    }


    private fun setupCardButtons() {
        val cvNewTimesheet: CardView = findViewById(R.id.CVNewTimesheet)
        cvNewTimesheet.setOnClickListener {
            val intent = Intent(this, NewTimesheet::class.java)
            startActivity(intent)
        }

        val cvCalendar: CardView = findViewById(R.id.CVCalendar)
        cvCalendar.setOnClickListener {
            val intent = Intent(this, EventsCalenderView::class.java)
            startActivity(intent)
        }

        val cvActiveTimesheets: CardView = findViewById(R.id.CVActiveTimesheets)
        cvActiveTimesheets.setOnClickListener {
            val intent = Intent(this, ActiveTimesheetsPage::class.java)
            startActivity(intent)
        }

        val cvAnalytics: CardView = findViewById(R.id.CVAnalytics)
        cvAnalytics.setOnClickListener {
            val intent = Intent(this, Analytics::class.java)
            startActivity(intent)
        }
    }

}

