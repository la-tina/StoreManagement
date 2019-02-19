package com.example.android.storemanagement


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_create_product.*


class CreateProductFragment : Fragment() {

    var message = "A product with the same barcode already exists."

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel(requireActivity().application)::class.java)
    }

    private val barcodes: MutableList<String> = mutableListOf()

    fun isBarcodeDuplicated(barcode: String) =
        barcodes.any { currentBarcode -> currentBarcode == barcode }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_product, container, false)
    }

    override fun onStart() {
        super.onStart()

        val name = product_name.toString()
        val price = product_price.toString()
        val overcharge = product_overcharge.toString()
        val barcode = product_barcode.toString()

        onProductTextChanged(price, overcharge, barcode)
        onPriceTextChanged(name, overcharge, barcode)
        onOverchargeTextChanged(name, price, barcode)
        onBarcodeTextChanged(name, price, overcharge)

        button_add_product.setOnClickListener {
            val quantity = 0
            val product = Product(name, price.toFloat(), overcharge.toFloat(), barcode, quantity)
            productViewModel.insert(product)
            fragmentManager?.popBackStackImmediate()
        }

        productViewModel.allProducts.observe(this, Observer { products ->
            // Update the cached copy of the words in the adapter.
            products?.let {
                barcodes.clear()
                val productsBarcodes = it.map { product -> product.barcode }
                barcodes.addAll(productsBarcodes)
            }
        })
    }

    private fun onProductTextChanged(productPrice: String, productOvercharge: String, productBarcode: String) {
        product_name.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                button_add_product.isEnabled = false
            }

            override fun afterTextChanged(name: Editable) {
                setupButton(name.toString(), productPrice, productOvercharge, productBarcode)
            }
        })
    }

    private fun onPriceTextChanged(productName: String, productOvercharge: String, productBarcode: String) {
        product_price.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                button_add_product.isEnabled = false
            }

            override fun afterTextChanged(price: Editable) {
                setupButton(productName, price.toString(), productOvercharge, productBarcode)
            }
        })
    }

    private fun onOverchargeTextChanged(productName: String, productPrice: String, productBarcode: String) {
        product_overcharge.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                button_add_product.isEnabled = false
            }

            override fun afterTextChanged(overcharge: Editable) {
                setupButton(productName, productPrice, overcharge.toString(), productBarcode)
            }
        })
    }

    private fun onBarcodeTextChanged(productName: String, productPrice: String, productOvercharge: String) {
        product_barcode.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                button_add_product.isEnabled = false
            }

            override fun afterTextChanged(barcode: Editable) {
                setupButton(productName, productPrice, productOvercharge, barcode.toString())
            }
        })
    }

    private fun setupButton(productName: String, productPrice: String, productOvercharge: String, barcode: String) {
        val anyFieldEmpty = isAnyFieldEmpty(productName, productPrice, productOvercharge, barcode)

        val isBarcodeDuplicated = isBarcodeDuplicated(barcode)

        button_add_product.isEnabled = !(anyFieldEmpty || isBarcodeDuplicated)

        if (isBarcodeDuplicated) product_barcode.error = message
    }

    private fun isAnyFieldEmpty(productName: String, productPrice: String, productOvercharge: String,
        productBarcode: String): Boolean =
        !(productName.isEmpty() && productPrice.isEmpty() && productOvercharge.isEmpty() &&
                productBarcode.isEmpty())
}





