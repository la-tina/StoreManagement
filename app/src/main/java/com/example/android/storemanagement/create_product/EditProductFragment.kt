package com.example.android.storemanagement.create_product

import android.text.Editable
import android.view.View
import androidx.lifecycle.Observer
import com.example.android.storemanagement.PRODUCT_KEY
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.toSingleEvent
import kotlinx.android.synthetic.main.fragment_create_product.*


class EditProductFragment : InfoProductFragment() {

    override val fragmentTitle: String = "Edit product"
    override val buttonText: String = "Save product"
    private val names: MutableList<String> = mutableListOf()

    override fun onStart() {
        super.onStart()

        button_scan_barcode.visibility = View.INVISIBLE

        val product = arguments?.getSerializable(PRODUCT_KEY) as Product
        product_name.setText(product.name)
        product_price.setText(product.price.toString())
        product_overcharge.setText(product.overcharge.toString())
        product_barcode.setText(product.barcode)
    }

    override fun onResume() {
        super.onResume()
        button_add_product.isEnabled = true
        product_barcode.isEnabled = false
        productViewModel.allProducts.toSingleEvent().observe(this, Observer { products ->
            products?.let {
                names.clear()
                val productsNames = it.map { product -> product.name }
                names.addAll(productsNames)
            }
        })
    }

    override fun isBarcodeDuplicated(barcode: String): Boolean = false
    override fun isNameDuplicated(name: String): Boolean = names.any { currentName -> currentName == name }


    override fun onButtonClicked(name: Editable, price: Editable, overcharge: Editable, barcode: Editable) {
        productViewModel.updateName(barcode.toString(), name.toString())
        productViewModel.updatePrice(name.toString(), price.toString().toFloat())
        productViewModel.updateOvercharge(name.toString(), overcharge.toString().toFloat())
        parentFragmentManager.popBackStackImmediate()
    }
}