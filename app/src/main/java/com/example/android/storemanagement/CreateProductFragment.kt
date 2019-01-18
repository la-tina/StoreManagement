package com.example.android.storemanagement


import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.arch.lifecycle.Observer
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.Toast
import com.example.android.storemanagement.database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_create_product.*


class CreateProductFragment : Fragment() {

    lateinit var viewModel: ProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_product, container, false)
    }


    override fun onStart() {
        super.onStart()

        button_add_product.setOnClickListener {
            val productName = product_name!!.text.toString()
            val productPrice = product_price!!.text.toString()
            val productOvercharge = product_overcharge!!.text.toString()
            val productBarcode = product_barcode!!.text.toString()
            val product = Product(productName, productPrice.toFloat(), productOvercharge.toFloat(), productBarcode.toLong())
            viewModel.insert(product)

            when {
                TextUtils.isEmpty(productName) -> button_add_product.isEnabled = false
                TextUtils.isEmpty(productPrice) -> button_add_product.isEnabled = false
                TextUtils.isEmpty(productOvercharge) -> button_add_product.isEnabled = false
                TextUtils.isEmpty(productBarcode) -> button_add_product.isEnabled = false
                else -> button_add_product.isEnabled = true
            }
            fragmentManager?.popBackStackImmediate()
        }



        //Use ViewModelProviders to associate your ViewModel with your Activity
        //When the Activity first starts, the ViewModelProviders will create the ViewModel
        //We get a ViewModel from the ViewModelProvider


    }

    companion object {
        const val newProductActivityRequestCode = 1
        const val EXTRA_REPLY = "com.example.android.products.REPLY"
    }

    override fun onResume() {
        super.onResume()

        name_text.isHintAnimationEnabled = true
    }

}


