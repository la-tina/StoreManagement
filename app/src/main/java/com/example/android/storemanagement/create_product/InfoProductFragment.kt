package com.example.android.storemanagement.create_product

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.storemanagement.BARCODE_ACTIVITY_REQUEST_CODE
import com.example.android.storemanagement.BARCODE_KEY
import com.example.android.storemanagement.R
import com.example.android.storemanagement.Utils.PRODUCT_BARCODE
import com.example.android.storemanagement.Utils.PRODUCT_NAME
import com.example.android.storemanagement.Utils.PRODUCT_OVERCHARGE
import com.example.android.storemanagement.Utils.PRODUCT_PERCENTAGE
import com.example.android.storemanagement.Utils.PRODUCT_PRICE
import com.example.android.storemanagement.create_product.CreateProductFieldValidator.areAllFieldsValid
import com.example.android.storemanagement.create_product.CreateProductFieldValidator.isFieldValid
import com.example.android.storemanagement.products_database.ProductViewModel
import com.facebook.FacebookSdk.getApplicationContext
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_create_product.*
import me.dm7.barcodescanner.zbar.ZBarScannerView
import kotlin.math.round


abstract class InfoProductFragment : Fragment() {

    abstract val fragmentTitle: String
    abstract val buttonText: String
    var fieldType: CreateProductFieldValidator.ProductFieldElements =
        CreateProductFieldValidator.ProductFieldElements.NAME

    var isPercentageViewSelected = false
    var isPriceViewSelected = false
    var isOverchargeViewSelected = false

    var lastPercentage = 0
    protected var user: FirebaseUser? = null

    companion object {
        const val CAMERA_PERMISSION_CODE = 0
        const val MAX_PRICE = 1000
        const val KEY_PRODUCT_NAME_VALUE = "productNameValue"
        const val KEY_PRODUCT_PRICE_VALUE = "productPriceValue"
        const val KEY_PRODUCT_OVERCHARGE_VALUE = "productOverchargeValue"
        const val KEY_PRODUCT_OVERCHARGE_PERCENTAGE_VALUE = "productOverchargePercentageValue"
        const val KEY_PRODUCT_BARCODE_VALUE = "productBarcodeValue"
    }

    private lateinit var scannerView: ZBarScannerView

    protected val productViewModel: ProductViewModel by lazy {
        ViewModelProvider(this).get(ProductViewModel(requireActivity().application)::class.java)
    }

    private var savedProductName: String = ""
    private var savedProductPrice: String = ""
    private var savedProductOvercharge: String = ""
    private var savedProductBarcode: String = ""
    private var savedProductOverchargePercentage: String = ""

    abstract fun isBarcodeDuplicated(barcode: String): Boolean

    abstract fun isNameDuplicated(name: String): Boolean

    abstract fun onButtonClicked(
        name: Editable,
        price: Editable,
        overcharge: Editable,
        barcode: Editable
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_product, container, false)
        val toolbar: Toolbar = view.findViewById(R.id.toolbarTopProduct)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStackImmediate()
        }
        if (savedInstanceState != null) {
            savedProductName =
                savedInstanceState.getCharSequence(KEY_PRODUCT_NAME_VALUE)?.toString() ?: ""
            savedProductPrice =
                savedInstanceState.getCharSequence(KEY_PRODUCT_PRICE_VALUE)?.toString() ?: ""
            savedProductOvercharge =
                savedInstanceState.getCharSequence(KEY_PRODUCT_OVERCHARGE_VALUE)?.toString() ?: ""
            savedProductOverchargePercentage =
                savedInstanceState.getCharSequence(KEY_PRODUCT_OVERCHARGE_PERCENTAGE_VALUE)?.toString() ?: ""
            savedProductBarcode =
                savedInstanceState.getCharSequence(KEY_PRODUCT_BARCODE_VALUE)?.toString() ?: ""
        }
        return view
    }

    override fun onStart() {
        super.onStart()

        clean()

        toolbarTopProduct.title = fragmentTitle
        button_add_product.text = buttonText

        scannerView = ZBarScannerView(context)

        product_name.setText(savedProductName)
        product_price.setText(savedProductPrice)
        product_overcharge.setText(savedProductOvercharge)
        product_overcharge_percentage.setText(savedProductOverchargePercentage)
        product_barcode.setText(savedProductBarcode)

        overcharge_percentage_text.isErrorEnabled = false

//        overcharge_percentage_text.setOnClickListener {
//            isPercentageViewSelected = true
//        }
//
//        price_text.setOnClickListener {
//            isPriceViewSelected = true
//        }
//
//        overcharge_text.setOnClickListener {
//            isOverchargeViewSelected = true
//        }

        product_overcharge_percentage.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            isPercentageViewSelected = hasFocus
        }
        product_price.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            isPriceViewSelected = hasFocus
        }
        product_overcharge.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            isOverchargeViewSelected = hasFocus
        }

        // clear space when there is no error message
        removeEmptyErrorMessageSpace(overcharge_percentage_text)
    }

    override fun onResume() {
        super.onResume()
        removeEditFieldsErrors()
        setFieldsTextWatcher()
        hideKeyboard(activity as Activity)

        user = Firebase.auth.currentUser

        button_add_product.setOnClickListener {
            onButtonClicked(
                product_name.text,
                product_price.text,
                product_overcharge.text,
                product_barcode.text
            )
        }
        button_scan_barcode.setOnClickListener { onBarcodeButtonPressed() }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard(activity as Activity)
    }

    /**
     * Removes the error text view from the given TextInputLayout
     * @param layout The given TextInputLayout
     */
    private fun removeEmptyErrorMessageSpace(layout: TextInputLayout) {
        if (layout.childCount == 2) {
            layout.getChildAt(1).visibility = View.GONE
        }
    }

    open fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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
        val textWatcherPercentage = getTextWatcher(
            product_overcharge_percentage,
            overcharge_percentage_text
        )

        product_overcharge_percentage.addTextChangedListener(textWatcherPercentage)
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
        outState.putCharSequence(KEY_PRODUCT_NAME_VALUE, product_name.text.toString())
        outState.putCharSequence(KEY_PRODUCT_PRICE_VALUE, product_price.text.toString())
        outState.putCharSequence(KEY_PRODUCT_OVERCHARGE_VALUE, product_overcharge.text.toString())
        outState.putCharSequence(KEY_PRODUCT_OVERCHARGE_PERCENTAGE_VALUE, product_overcharge_percentage.text.toString())
        outState.putCharSequence(KEY_PRODUCT_BARCODE_VALUE, product_barcode.text.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.getStringExtra(BARCODE_KEY)?.let { scannedBarcode ->
            savedProductBarcode = scannedBarcode
        }
        val preferences = activity?.getSharedPreferences("PREFERENCE", AppCompatActivity.MODE_PRIVATE)
        if (data?.getStringExtra(BARCODE_KEY) == null) {
            savedProductBarcode = preferences?.getString(PRODUCT_BARCODE, "") ?: ""
        }
        savedProductName = preferences?.getString(PRODUCT_NAME, "") ?: ""
        savedProductPrice = preferences?.getString(PRODUCT_PRICE, "") ?: ""
        savedProductOvercharge = preferences?.getString(PRODUCT_OVERCHARGE, "") ?: ""
        savedProductOverchargePercentage = preferences?.getString(PRODUCT_PERCENTAGE, "") ?: ""
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
            activity?.getSharedPreferences("PREFERENCE", AppCompatActivity.MODE_PRIVATE)
                ?.edit()
                ?.putString(PRODUCT_NAME, product_name.text.toString())
                ?.putString(PRODUCT_PRICE, product_price.text.toString())
                ?.putString(PRODUCT_BARCODE, product_barcode.text.toString())
                ?.putString(PRODUCT_OVERCHARGE, product_overcharge.text.toString())
                ?.putString(PRODUCT_PERCENTAGE, product_overcharge_percentage.text.toString())
                ?.apply()
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
                    product_name -> {
                        fieldType =
                            CreateProductFieldValidator.ProductFieldElements.NAME
                    }
                    product_price -> {
                        fieldType = CreateProductFieldValidator.ProductFieldElements.PRICE
//                        if (editTextView.text.toString().isEmpty() || editTextView.text.toString() == ".0") {
//                            product_overcharge_percentage.setText("0")
//                        }
                        if (isFieldValid(
                                editTextView,
                                inputLayoutView,
                                fieldType,
                                ::isBarcodeDuplicated,
                                ::isNameDuplicated
                            )
                            && !inputLayoutView.isErrorEnabled
                            && editTextView.text.isNotEmpty()
                            && editTextView.text.toString().toFloat() != 0F
                            && product_overcharge.text.isNotEmpty()
                            && product_overcharge.text.toString().toFloat() != 0F
                        ) {
                            //calculate percentage for percentage field
                            if (shouldCalculatePrice(editTextView)) {
                                val percentage: Float = (product_overcharge.text.toString()
                                    .toFloat() / editTextView.text.toString().toFloat()) * 100
                                product_overcharge_percentage.setText(
                                    percentage.toInt().toString()
                                )
                            }
                        }
                    }
                    product_overcharge -> {
                        fieldType = CreateProductFieldValidator.ProductFieldElements.OVERCHARGE

                        if (editTextView.text.startsWith(".")) {
                            val text = "0" + editTextView.text.toString()
                            editTextView.setText(text)
                        }
                        if (editTextView.text.toString().isEmpty()) {
                            product_overcharge_percentage.setText("0")
                        }
                        if (isFieldValid(
                                editTextView,
                                inputLayoutView,
                                fieldType,
                                ::isBarcodeDuplicated,
                                ::isNameDuplicated
                            )
                            && !inputLayoutView.isErrorEnabled
                            && product_price.text.isNotEmpty()
                            && product_price.text.toString().toFloat() != 0F
                            && editTextView.text.isNotEmpty()
                            && editTextView.text.toString().toFloat() != 0F
                        ) {
                            //calculate percentage for percentage field
                            if (shouldCalculateOvercharge(editTextView)) {
                                val percentage: Float = (editTextView.text.toString()
                                    .toFloat() / product_price.text.toString().toFloat()) * 100
                                product_overcharge_percentage.setText(
                                    percentage.toInt().toString()
                                )
                            }
                        }
                    }
                    product_overcharge_percentage -> {
                        fieldType = CreateProductFieldValidator.ProductFieldElements.PERCENTAGE
                        if (editTextView.text.toString().startsWith(".")) {
                            val text = "0" + editTextView.text.toString()
                            editTextView.setText(text)
                        }
                        if (editTextView.text.toString().isEmpty() || editTextView.text.toString().toInt() == 0) {
                            if (editTextView.text.toString()
                                    .isEmpty() && product_overcharge.text.toString() != "0"
                            ) product_overcharge.setText("0")
                        }
                        if (shouldCalculatePercentage(inputLayoutView, editTextView)
                        ) {
                            val overcharge: Float = (product_overcharge_percentage.text.toString()
                                .toFloat() * product_price.text.toString().toFloat()) / 100
                            if (product_overcharge.text.toString() != overcharge.toString()) {
                                product_overcharge.setText(overcharge.toString())
                            }
                            lastPercentage = editTextView.text.toString().toInt()
                            return
                        }
                    }
                    product_barcode -> {
                        fieldType =
                            CreateProductFieldValidator.ProductFieldElements.BARCODE
                    }
                }
                isFieldValid(
                    editTextView,
                    inputLayoutView,
                    fieldType,
                    ::isBarcodeDuplicated,
                    ::isNameDuplicated
                )
                button_add_product.isEnabled = areAllFieldsValid(
                    name_text,
                    price_text,
                    overcharge_text,
                    barcode_text,
                    product_name,
                    product_price,
                    product_barcode
                )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                inputLayoutView.error = null
                inputLayoutView.isErrorEnabled = false
            }
        }

    private fun shouldCalculatePercentage(
        inputLayoutView: TextInputLayout,
        editTextView: EditText
    ) =
        !inputLayoutView.isErrorEnabled && !product_price.text.isNullOrEmpty() && editTextView.text.isNotEmpty() && editTextView.text.toString()
            .toFloat() != 0F && lastPercentage != editTextView.text.toString()
            .toInt() && isPercentageViewSelected

    private fun shouldCalculatePrice(
        editTextView: EditText
    ) =
        editTextView.text.toString().toFloat() != 0F && isPriceViewSelected

    private fun shouldCalculateOvercharge(
        editTextView: EditText
    ) =
        editTextView.text.toString().toFloat() != 0F && isOverchargeViewSelected

    object TextChangedWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    fun Float.round(decimals: Int): Float {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (round(this * multiplier) / multiplier).toFloat()
    }

    private fun clean() {
        product_barcode.setText("")
        product_name.setText("")
        product_overcharge.setText("")
        product_price.setText("")
    }
}