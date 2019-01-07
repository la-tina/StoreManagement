package com.example.android.storemanagement


import android.app.ActionBar
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import kotlinx.android.synthetic.main.fragment_title.*

/**
 * A simple [Fragment] subclass.
 *
 */
@Suppress("UNREACHABLE_CODE")
class TitleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_title, container, false)

//        val addButton: Button = findViewById(R.id.add_button)
//        addButton.setOnClickListener { view ->
//            Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .show()


        // Set toolbar title/app title

       }



    }


