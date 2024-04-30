package com.example.timewise

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbarBack: ImageView? = findViewById(R.id.btnToolbarBack)
        val toolbarProfile: ImageView? = findViewById(R.id.imgToolbarProfile)
        val toolbarHome: ImageView? = findViewById(R.id.imgToolbarLogo)

        toolbarHome?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

        toolbarBack?.setOnClickListener {
            onBackPressed()
        }


    }
}
