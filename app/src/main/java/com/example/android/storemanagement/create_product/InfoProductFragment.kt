package com.example.android.storemanagement.create_product

import android.Manifest
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
import com.example.android.storemanagement.BARCODE_ACTIVITY_REQUEST_CODE
import com.example.android.storemanagement.BARCODE_KEY
import com.example.android.storemanagement.MainActivity
import com.example.android.storemanagement.R
import com.example.android.storemanagement.products_database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_create_product.*
import me.dm7.barcodescanner.zbar.ZBarScannerView

abstract class InfoProductFragment : Fragment() {

    abstract val fragmentTitle: String
    abstract val buttonText: String

    companion object {
        const val CAMERA_PERMISSION_CODE = 0
        const val MESSAGE_BARCODE = "A product with the same barcode already exists or the barcode is empty."
        const val MESSAGE_PRICE = "Тhe maximum allowed price is 100лв."
        const val MESSAGE_INVALID_PRICE = "Invalid price."
        const val MESSAGE_OVERCHARGE = "Тhe maximum allowed overcharge is 100лв."
        const val MESSAGE_INVALID_OVERCHARGE = "Invalid overcharge."
        const val MESSAGE_PRICE_ZERO = "Тhe price can't be 0лв."
        const val MESSAGE_EMPTY_NAME = "Product name can't be empty."
        const val MAX_PRICE = 100
        const val KEY_PRODUCT_NAME_VALUE = "productNameValue"
        const val KEY_PRODUCT_PRICE_VALUE = "productPriceValue"
        const val KEY_PRODUCT_OVERCHARGE_VALUE = "productOverchargeValue"
        const val KEY_PRODUCT_BARCODE_VALUE = "productBarcodeValue"
    }

    private lateinit var mScannerView: ZBarScannerView

    protected val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel(requireActivity().application)::class.java)
    }

    private var savedProductName: String = ""
    private var savedProductPrice: String = ""
    private var savedProductOvercharge: String = ""
    private var savedProductBarcode: String = ""

    private val priceRegex = Regex(pattern = "^((?=.)(?=[0-9]+))|([0-9]+)|((?=[0-9]+)(?=.)(?=[0-9]+))\$")

    abstract fun isBarcodeDuplicated(barcode: String): Boolean

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_product, container, false)

        if (savedInstanceState != null) {
            savedProductName = savedInstanceState.getCharSequence(KEY_PRODUCT_NAME_VALUE)?.toString() ?: ""
            savedProductPrice = savedInstanceState.getCharSequence(KEY_PRODUCT_PRICE_VALUE)?.toString() ?: ""
            savedProductOvercharge = savedInstanceState.getCharSequence(KEY_PRODUCT_OVERCHARGE_VALUE)?.toString() ?: ""
            savedProductBarcode = savedInstanceState.getCharSequence(KEY_PRODUCT_BARCODE_VALUE)?.toString() ?: ""
        }
        return view
    }

    override fun onStart() {
        super.onStart()

        clean()

        toolbarTop.title = fragmentTitle
        button_add_product.text = buttonText

        mScannerView = ZBarScannerView(context)

        product_name.setText(savedProductName)
        product_price.setText(savedProductPrice)
        product_overcharge.setText(savedProductOvercharge)
        product_barcode.setText(savedProductBarcode)
    }

    override fun onResume() {
        super.onResume()
        val name = product_name.text
        val price = product_price.text
        val overcharge = product_overcharge.text
        val barcode = product_barcode.text

        onProductTextChanged(price, overcharge, barcode)
        onPriceTextChanged(name, overcharge, barcode)
        onOverchargeTextChanged(name, price, barcode)

        button_add_product.setOnClickListener { onButtonClicked(name, price, overcharge, barcode) }

        button_scan_barcode.setOnClickListener { onBarcodeButtonPressed() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(KEY_PRODUCT_NAME_VALUE, product_name.text)
        outState.putCharSequence(KEY_PRODUCT_PRICE_VALUE, product_price.text)
        outState.putCharSequence(KEY_PRODUCT_OVERCHARGE_VALUE, product_overcharge.text)
        outState.putCharSequence(KEY_PRODUCT_BARCODE_VALUE, product_barcode.text)
    }

    abstract fun onButtonClicked(
        name: Editable,
        price: Editable,
        overcharge: Editable,
        barcode: Editable
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.getStringExtra(BARCODE_KEY)?.let {
            savedProductBarcode = it
        }
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
                BARCODE_ACTIVITY_REQUEST_CODE
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

    protected fun setupButton(
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
        if (!isPriceValid(productPrice)) product_price.error = MESSAGE_INVALID_PRICE
        if (!isPriceValid(productOvercharge)) product_overcharge.error = MESSAGE_INVALID_OVERCHARGE
    }

    private fun isPriceValid(price: String): Boolean {
        return priceRegex.containsMatchIn(price)
    }

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