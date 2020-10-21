package com.example.android.storemanagement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class AboutActivity: AppCompatActivity() {

    lateinit var onNavigationChangedListener: OnNavigationChangedListener

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val inflatedView = inflater.inflate(
//            R.layout.about_activity,
//            container,
//            false
//        )
//
//        return inflatedView
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity)
        val toolbar: Toolbar = findViewById(R.id.toolbarTopAbout)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back)
        toolbar.setNavigationOnClickListener{
            this.finish()
        }
    }


}