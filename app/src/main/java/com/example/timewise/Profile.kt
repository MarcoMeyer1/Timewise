package com.example.timewise

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Profile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupButtonListeners()
        fetchUserData()
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            user?.let {
                val fullName: TextView = findViewById(R.id.lblProfileFullName)
                fullName.text = it.name
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtonListeners() {
        val btnEditProfile: Button = findViewById(R.id.btnEditProfile)
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        val btnManageTimesheets: Button = findViewById(R.id.btnManageTimesheets)
        btnManageTimesheets.setOnClickListener {
            val intent = Intent(this, ActiveTimesheetsPage::class.java)
            startActivity(intent)
        }

        val btnPrivacyPolicy: Button = findViewById(R.id.btnPrivacyPolicy)
        btnPrivacyPolicy.setOnClickListener {
            showPrivacyPolicyDialog()
        }

        val btnSettings: Button = findViewById(R.id.btnSettings)
        btnSettings.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        val btnLogOut: Button = findViewById(R.id.btnLogOut)
        btnLogOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showPrivacyPolicyDialog() {
        val privacyPolicyText = "At TimeWise, we respect your privacy and are committed to protecting the personal information you share with us. This policy outlines our practices regarding the collection, use, and disclosure of your information when you use our app.\n" +
                "\n" +
                "Collection of Information: We collect information you provide directly to us, such as when you create an account, interact with the app, or make a purchase. This may include your name, email address, phone number, and payment information.\n" +
                "\n" +
                "Use of Information: We use your information to provide and improve our services, communicate with you, and ensure the security of our app.\n" +
                "\n" +
                "Sharing of Information: We may share your information with service providers to perform functions and process your data, and with authorities to comply with legal obligations or protect against fraud and abuse.\n" +
                "\n" +
                "Your Rights: You have the right to access, update, or delete the information we hold about you. You can manage your information through your account settings."

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Privacy Policy")
            .setMessage(privacyPolicyText)
            .setPositiveButton("Accept") { dialog, which ->
                // Handle acceptance
            }
            .setNegativeButton("Decline") { dialog, which ->
                finishAffinity()
            }
            .create()

        alertDialog.show()
    }
}

