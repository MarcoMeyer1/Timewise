package com.example.timewise

import android.content.Intent
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

open class BaseActivity : AppCompatActivity() {
    private var toolbarContainer: RelativeLayout? = null



    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbarBack: ImageView? = findViewById(R.id.btnToolbarBack)
        val toolbarProfile: ImageView? = findViewById(R.id.imgToolbarProfile)
        val toolbarHome: ImageView? = findViewById(R.id.imgToolbarLogo)
        val imgToolbarExtra: ImageView = findViewById(R.id.imgToolbarExtra)

        toolbarHome?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

        toolbarBack?.setOnClickListener {
            onBackPressed()
        }

        toolbarProfile?.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        toolbarHome?.setOnClickListener {
            val homeIntent = Intent(this, HomePage::class.java)
            homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(homeIntent)
        }


        imgToolbarExtra.setOnClickListener {
            val intent = Intent(this, DailyProgress::class.java)
            startActivity(intent)
        }
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        imgToolbarExtra.colorFilter = filter

    }
    fun updateToolbarColor(hexCode: String) {
       toolbarContainer = findViewById(R.id.toolbarColorBar)
        try {
            val color = Color.parseColor(hexCode)
         toolbarContainer?.setBackgroundColor(color)

        } catch (e: IllegalArgumentException) {

            e.printStackTrace()
        }
    }
}
