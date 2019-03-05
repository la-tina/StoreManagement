package com.example.android.storemanagement.create_product


import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.android.storemanagement.MainActivity
import com.example.android.storemanagement.R
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_create_product.*
import me.dm7.barcodescanner.zbar.ZBarScannerView


class CreateProductFragment : Fragment() {

    companion object {
        const val CAMERA_PERMISSION_CODE = 0
        const val MESSAGE_BARCODE = "A product with the same barcode already exists or the barcode is empty."
        const val MESSAGE_PRICE = "Тhe maximum allowed price is 100лв."
        const val MESSAGE_OVERCHARGE = "Тhe maximum allowed overcharge is 100лв."
        const val MESSAGE_PRICE_ZERO = "Тhe price can't be 0лв."
        const val MESSAGE_EMPTY_NAME = "Product name can't be empty."
        const val MAX_PRICE = 100
        const val KEY_PRODUCT_NAME_VALUE = "productNameValue"
        const val KEY_PRODUCT_PRICE_VALUE = "productPriceValue"
        const val KEY_PRODUCT_OVERCHARGE_VALUE = "productOverchargeValue"
        const val KEY_PRODUCT_BARCODE_VALUE = "productBarcodeValue"
    }

    private lateinit var mScannerView: ZBarScannerView

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel(requireActivity().application)::class.java)
    }

    private var savedProductName: String = ""
    private var savedProductPrice: String = ""
    private var savedProductOvercharge: String = ""
    private var savedProductBarcode: String = ""

    private val barcodes: MutableList<String> = mutableListOf()

    private val priceRegex = Regex(pattern = "^((?=.)(?=[0-9]+))|([0-9]+)|((?=[0-9]+)(?=.)(?=[0-9]+))\$")

    private fun isBarcodeDuplicated(barcode: String) =
        barcodes.any { currentBarcode -> currentBarcode == barcode }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_product, container, false)

        if (savedInstanceState != null) {
            savedProductName = savedInstanceState.getCharSequence(KEY_PRODUCT_NAME_VALUE).toString()
            savedProductPrice = savedInstanceState.getCharSequence(KEY_PRODUCT_PRICE_VALUE).toString()
            savedProductOvercharge = savedInstanceState.getCharSequence(KEY_PRODUCT_OVERCHARGE_VALUE).toString()
            savedProductBarcode = savedInstanceState.getCharSequence(KEY_PRODUCT_BARCODE_VALUE).toString()
        }
        return view
    }

    override fun onStart() {
        super.onStart()

        clean()

        mScannerView = ZBarScannerView(context)

        product_name.setText(savedProductName)
        product_price.setText(savedProductPrice)
        product_overcharge.setText(savedProductOvercharge)
        product_barcode.setText(savedProductBarcode)

        val name = product_name.text
        val price = product_price.text
        val overcharge = product_overcharge.text
        val barcode = product_barcode.text

        onProductTextChanged(price, overcharge, barcode)
        onPriceTextChanged(name, overcharge, barcode)
        onOverchargeTextChanged(name, price, barcode)
        onBarcodeTextChanged(name, price, overcharge)

        button_add_product.setOnClickListener { onAddButtonClicked(name, price, overcharge, barcode) }

        productViewModel.allProducts.observe(this, Observer { products ->
            // Update the cached copy of the words in the adapter.
            products?.let {
                barcodes.clear()
                val productsBarcodes = it.map { product -> product.barcode }
                barcodes.addAll(productsBarcodes)
            }
        })

        button_scan_barcode.setOnClickListener { onBarcodeButtonPressed() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(KEY_PRODUCT_NAME_VALUE, product_name.text)
        outState.putCharSequence(KEY_PRODUCT_PRICE_VALUE, product_price.text)
        outState.putCharSequence(KEY_PRODUCT_OVERCHARGE_VALUE, product_overcharge.text)
        outState.putCharSequence(KEY_PRODUCT_BARCODE_VALUE, product_barcode.text)
    }

    private fun onAddButtonClicked(
        name: Editable,
        price: Editable,
        overcharge: Editable,
        barcode: Editable
    ) {
        val quantity = 0
        val product = Product(
            name.toString(), price.toString().toFloat(),
            overcharge.toString().toFloat(), barcode.toString(), quantity
        )
        productViewModel.insert(product)
        fragmentManager?.popBackStackImmediate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val barcode = data?.getStringExtra(MainActivity.BARCODE_KEY)
        product_barcode.setText(barcode)
    }

    private fun onBarcodeButtonPressed() {
        val cameraPermission =
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            val intent = Intent(requireContext(), BarcodeScanningActivity()::class.java)
            startActivityForResult(
                intent,
                MainActivity.BARCODE_ACTIVITY_REQUEST_CODE
            )
        }
    }

    private fun onProductTextChanged(productPrice: Editable, productOvercharge: Editable, productBarcode: Editable) {
        product_name.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(name: Editable) {
                setupButton(name, productPrice, productOvercharge, productBarcode)
            }
        })
    }

    private fun onPriceTextChanged(productName: Editable, productOvercharge: Editable, productBarcode: Editable) {
        product_price.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(price: Editable) {
                setupButton(productName, price, productOvercharge, productBarcode)
            }
        })
    }

    private fun onOverchargeTextChanged(productName: Editable, productPrice: Editable, productBarcode: Editable) {
        product_overcharge.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(overcharge: Editable) {
                setupButton(productName, productPrice, overcharge, productBarcode)
            }
        })
    }

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

    private fun setupButton(
        productNameView: Editable, productPriceView: Editable, productOverchargeView: Editable,
        barcodeView: Editable
    ) {
        val productName = productNameView.toString()
        val productPrice = productPriceView.toString()
        val productOvercharge = productOverchargeView.toString()
        val barcode = barcodeView.toString()

        var isProductPriceIncorrect = false
        var isProductPriceAboveLimit = false
        var isProductOverchargeAboveLimit = false
        var isProductPriceZero = false
        var isProductNameEmpty = false

        val anyFieldEmpty = isAnyFieldEmpty(productName, productPrice, productOvercharge, barcode)

        val isBarcodeDuplicated = isBarcodeDuplicated(barcode)

        if (isPriceValid(productPrice)) {
            isProductPriceAboveLimit = productPrice.toFloat() > MAX_PRICE
            isProductPriceZero = productPrice.toFloat() <= 0
            isProductPriceIncorrect = isProductPriceAboveLimit || isProductPriceZero
        }

        if (isPriceValid(productOvercharge))
            isProductOverchargeAboveLimit = productOvercharge.toFloat() > MAX_PRICE

        if (productName.isBlank())
            isProductNameEmpty = true

        button_add_product.isEnabled = !(anyFieldEmpty
                || isBarcodeDuplicated
                || isProductPriceIncorrect
                || isProductOverchargeAboveLimit
                || isProductNameEmpty
                || !isPriceValid(productPrice)
                || !isPriceValid(productOvercharge))

        if (isBarcodeDuplicated) product_barcode.error = MESSAGE_BARCODE
        if (isProductPriceAboveLimit) product_price.error = MESSAGE_PRICE
        if (isProductOverchargeAboveLimit) product_overcharge.error = MESSAGE_OVERCHARGE
        if (isProductPriceZero) product_price.error = MESSAGE_PRICE_ZERO
        if (isProductNameEmpty) product_name.error = MESSAGE_EMPTY_NAME
    }

    private fun isPriceValid(price: String):Boolean {
        return priceRegex.containsMatchIn(price)
    }

    private fun String.startsWithDigit(): Boolean =
        !this.isEmpty() && Character.isDigit(this.first())

    private fun isAnyFieldEmpty(
        productName: String, productPrice: String, productOvercharge: String,
        productBarcode: String
    ): Boolean =
        productName.isEmpty() || productPrice.isEmpty() || productOvercharge.isEmpty() || productBarcode.isEmpty()

    private fun clean() {
        product_barcode.setText("")
        product_name.setText("")
        product_overcharge.setText("")
        product_price.setText("")
    }
}