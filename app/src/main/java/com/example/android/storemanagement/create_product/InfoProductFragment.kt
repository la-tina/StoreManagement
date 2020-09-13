package com.example.android.storemanagement.create_product

import android.Manifest
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.android.material.textfield.TextInputLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.android.storemanagement.BARCODE_ACTIVITY_REQUEST_CODE
import com.example.android.storemanagement.BARCODE_KEY
import com.example.android.storemanagement.R
import com.example.android.storemanagement.create_product.CreateProductFieldValidator.areAllFieldsValid
import com.example.android.storemanagement.create_product.CreateProductFieldValidator.isFieldValid
import com.example.android.storemanagement.products_database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_create_product.*
import me.dm7.barcodescanner.zbar.ZBarScannerView

abstract class InfoProductFragment : Fragment() {

    abstract val fragmentTitle: String
    abstract val buttonText: String
    var fieldType: CreateProductFieldValidator.ProductFieldElements = CreateProductFieldValidator.ProductFieldElements.NAME

    companion object {
        const val CAMERA_PERMISSION_CODE = 0
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

    abstract fun isBarcodeDuplicated(barcode: String): Boolean

    abstract fun onButtonClicked(name: Editable, price: Editable, overcharge: Editable, barcode: Editable)

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

        toolbarTopProduct.title = fragmentTitle
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
        removeEditFieldsErrors()
        setFieldsTextWatcher()

        button_add_product.setOnClickListener { onButtonClicked(name, price, overcharge, barcode) }
        button_scan_barcode.setOnClickListener { onBarcodeButtonPressed() }
    }

    private fun setFieldsTextWatcher() {
        val textWatcherName = getTextWatcher(product_name, name_text)
        product_name.addTextChangedListener(textWatcherName)
        val textWatcherPrice = getTextWatcher(product_price, price_text)
        product_price.addTextChangedListener(textWatcherPrice)
        val textWatcherOvercharge = getTextWatcher(product_overcharge, overcharge_text)
        product_overcharge.addTextChangedListener(textWatcherOvercharge)
        val textWatcherBarcode = getTextWatcher(product_barcode, barcode_text)
        product_barcode.addTextChangedListener(textWatcherBarcode)
    }

    private fun removeEditFieldsErrors() {
        name_text.error = null
        price_text.error = null
        overcharge_text.error = null
        barcode_text.error = null
        name_text.isErrorEnabled = false
        price_text.isErrorEnabled = false
        overcharge_text.isErrorEnabled = false
        barcode_text.isErrorEnabled = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(KEY_PRODUCT_NAME_VALUE, product_name.text)
        outState.putCharSequence(KEY_PRODUCT_PRICE_VALUE, product_price.text)
        outState.putCharSequence(KEY_PRODUCT_OVERCHARGE_VALUE, product_overcharge.text)
        outState.putCharSequence(KEY_PRODUCT_BARCODE_VALUE, product_barcode.text)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.getStringExtra(BARCODE_KEY)?.let { scannedBarcode ->
            savedProductBarcode = scannedBarcode
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

    protected fun getTextWatcher(editTextView: EditText, inputLayoutView: TextInputLayout) =
        object : TextWatcher by TextChangedWatcher {
            override fun afterTextChanged(editable: Editable) {
                when (editTextView) {
                    product_name -> fieldType = CreateProductFieldValidator.ProductFieldElements.NAME
                    product_price -> fieldType = CreateProductFieldValidator.ProductFieldElements.PRICE
                    product_overcharge -> fieldType = CreateProductFieldValidator.ProductFieldElements.OVERCHARGE
                    product_barcode -> fieldType = CreateProductFieldValidator.ProductFieldElements.BARCODE
                }
                isFieldValid(editTextView, inputLayoutView, fieldType, ::isBarcodeDuplicated)
                button_add_product.isEnabled = areAllFieldsValid(name_text, price_text, overcharge_text, barcode_text)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                inputLayoutView.error = null
                inputLayoutView.isErrorEnabled = false
            }
        }

    object TextChangedWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun clean() {
        product_barcode.setText("")
        product_name.setText("")
        product_overcharge.setText("")
        product_price.setText("")
    }
}