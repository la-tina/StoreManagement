package com.example.android.storemanagement

import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseUserType
import kotlinx.android.synthetic.main.activity_account_type.*
import java.util.*


class ActivityAccountTypeSelection : AppCompatActivity() {

    var markedType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_type)
    }

    override fun onResume() {
        super.onResume()
        val fbUserId = intent.getStringExtra("fbUserId")!!
        vendorButton.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            onTypeMarked(isChecked, buttonView)
        })
        retailerButton.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            onTypeMarked(isChecked, buttonView)
        })
        customerButton.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            onTypeMarked(isChecked, buttonView)
        })
        buttonSave.isEnabled = vendorButton.isChecked || retailerButton.isChecked || customerButton.isChecked
        buttonSave.setOnClickListener {
            updateFirebaseUserType(fbUserId, markedType) { runOnUiThread { openMainActivity() } }
        }
    }

    private fun onTypeMarked(isChecked: Boolean, buttonView: CompoundButton) {
        if (isChecked) {
            buttonSave.isEnabled = true
            markedType = buttonView.text.toString().toUpperCase(Locale.ROOT)
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}