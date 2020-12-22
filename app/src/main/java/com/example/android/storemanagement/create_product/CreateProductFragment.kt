package com.example.android.storemanagement.create_product

import androidx.lifecycle.Observer
import android.text.Editable
import com.example.android.storemanagement.products_database.Product


open class CreateProductFragment : InfoProductFragment() {

    override val fragmentTitle: String = "Create Product"
    override val buttonText: String = "Add Product"

    private val barcodes: MutableList<String> = mutableListOf()
    private val names: MutableList<String> = mutableListOf()

    override fun onResume() {
        super.onResume()
        productViewModel.allProducts.observe(this, Observer { products ->
            // Update the cached copy of the products in the adapter.
            products?.let {
                barcodes.clear()
                names.clear()
                val productsBarcodes = it.map { product -> product.barcode }
                barcodes.addAll(productsBarcodes)
                val productsNames = it.map { product -> product.name }
                names.addAll(productsNames)
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
        parentFragmentManager.popBackStackImmediate()
    }

    override fun isBarcodeDuplicated(barcode: String) =
        barcodes.any { currentBarcode -> currentBarcode == barcode }

    override fun isNameDuplicated(name: String) =
        names.any { currentName -> currentName == name }
}