package com.example.android.storemanagement.create_product

import android.arch.lifecycle.Observer
import android.text.Editable
import android.text.TextWatcher
import com.example.android.storemanagement.products_database.Product
import kotlinx.android.synthetic.main.fragment_create_product.*


open class CreateProductFragment : InfoProductFragment() {

    override val fragmentTitle: String = "Create Product"
    override val buttonText: String = "Add Product"

    private val barcodes: MutableList<String> = mutableListOf()

    override fun onStart() {
        super.onStart()

        val name = product_name.text
        val price = product_price.text
        val overcharge = product_overcharge.text

        onBarcodeTextChanged(name, price, overcharge)
    }

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
            name.toString(), price.toString().toFloat(),
            overcharge.toString().toFloat(), barcode.toString(), quantity
        )
        productViewModel.insert(product)
        fragmentManager?.popBackStackImmediate()
    }

    override fun isBarcodeDuplicated(barcode: String) =
        barcodes.any { currentBarcode -> currentBarcode == barcode }

    private fun onBarcodeTextChanged(productName: Editable, productPrice: Editable, productOvercharge: Editable) {
        product_barcode.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(barcode: Editable) {
                setupButton(productName, productPrice, productOvercharge, barcode)
            }
        })
    }
}