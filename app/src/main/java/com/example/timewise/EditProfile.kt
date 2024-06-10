package com.example.timewise

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditProfile : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                updateProfilePicture(uri)
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_LONG).show()
            }
        }

        val btnChangePicture: Button = findViewById(R.id.btnEditChangePicture)
        btnChangePicture.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        prefillUserData()

        val nameEditText: EditText = findViewById(R.id.txtEditName)
        val emailEditText: EditText = findViewById(R.id.txtEditEmail)
        val passwordEditText: EditText = findViewById(R.id.txtEditPassword)
        val btnConfirmChanges: Button = findViewById(R.id.btnConfirmChanges)

        btnConfirmChanges.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Validate inputs (if necessary) then update user details
            if (name.isNotBlank() && password.isNotBlank()) {
                val userId = auth.currentUser?.uid ?: return@setOnClickListener
                val userRef = database.getReference("users").child(userId)

                val updates = mapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password
                )

                userRef.updateChildren(updates).addOnSuccessListener {
                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomePage::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill out all the fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfilePicture(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("profilePictures/$userId")

        storageRef.putFile(imageUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val userRef = database.getReference("users").child(userId)
                userRef.child("profilePictureUrl").setValue(uri.toString()).addOnSuccessListener {
                    val imageView: ImageView = findViewById(R.id.imageView)
                    imageView.setImageURI(imageUri)
                    Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating profile picture URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error uploading profile picture: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun prefillUserData() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            user?.let {
                findViewById<EditText>(R.id.txtEditName).setText(it.name)
                findViewById<EditText>(R.id.txtEditEmail).setText(it.email)
                findViewById<EditText>(R.id.txtEditPassword).setText(it.password)
                val imageView: ImageView = findViewById(R.id.imageView)
                it.profilePictureUrl?.let { url ->
                    Glide.with(this).load(url).into(imageView)
                }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

data class User(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val profilePictureUrl: String? = null
)