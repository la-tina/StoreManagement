package com.example.android.storemanagement

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

class ActivityIntro : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
    }

    override fun onResume() {
        super.onResume()
        findViewById<ViewGroup>(android.R.id.content)?.setOnClickListener {
            openLoginActivity()
        }
    }

    private fun openLoginActivity() {
        val intent = Intent(this, ActivityLogin::class.java)
        startActivity(intent)
        finish()
    }
}