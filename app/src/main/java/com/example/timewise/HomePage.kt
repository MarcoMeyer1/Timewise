package com.example.timewise

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HomePage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        fetchUserDataAndSetupWelcomeMessage()
        setupCardButtons()
    }

    private fun fetchUserDataAndSetupWelcomeMessage() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            user?.let {
                setupWelcomeMessage(it.name)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupWelcomeMessage(userName: String) {
        val lblWelcomeMessage: TextView = findViewById(R.id.lblWelcomeMessage)
        val firstName = userName.split(" ").first()
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