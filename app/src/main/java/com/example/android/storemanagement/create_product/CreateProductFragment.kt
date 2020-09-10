package com.example.android.storemanagement.create_product

import android.arch.lifecycle.Observer
import android.text.Editable
import com.example.android.storemanagement.products_database.Product
import kotlinx.android.synthetic.main.fragment_create_product.*


open class CreateProductFragment : InfoProductFragment() {

    override val fragmentTitle: String = "Create Product"
    override val buttonText: String = "Add Product"

    private val barcodes: MutableList<String> = mutableListOf()

    override fun onResume() {
        super.onResume()
        productViewModel.allProducts.observe(this, Observer { products ->
            // Update the cached copy of the products in the adapter.
            products?.let {
                barcodes.clear()
                val productsBarcodes = it.map { product -> product.barcode }
                barcodes.addAll(productsBarcodes)
            }
        })
    }

    override fun onButtonClicked(name: Editable, price: Editable, overcharge: Editable, barcode: Editable) {
        val quantity = 0
        val product = Product(
            name.toString(),
            price.toString().toFloat(),
            overcharge.toString().toFloat(),
            barcode.toString(),
            quantity
        )
        productViewModel.insert(product)
        fragmentManager?.popBackStackImmediate()
    }

    override fun isBarcodeDuplicated(barcode: String) =
        barcodes.any { currentBarcode -> currentBarcode == barcode }
}