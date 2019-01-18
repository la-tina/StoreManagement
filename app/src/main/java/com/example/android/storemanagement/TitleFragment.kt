package com.example.android.storemanagement


import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_title.*



class TitleFragment : Fragment() {

    lateinit var onNavigationChangedListener: OnNavigationChangedListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_title, container, false)
    }

    override fun onResume() {
        super.onResume()
        orders_add_button?.setOnClickListener {
            if (::onNavigationChangedListener.isInitialized)
                onNavigationChangedListener.onNavigationChanged(0)
        }
    }
}


