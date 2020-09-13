package com.example.android.storemanagement.create_product

import com.google.android.material.textfield.TextInputLayout
import android.widget.EditText

object CreateProductFieldValidator {

    private var MESSAGE_BARCODE = "A product with the same barcode already exists."
    private const val MESSAGE_EMPTY_BARCODE = "The barcode cannot be empty."
    private const val MESSAGE_PRICE_MAX_VALUE = "Тhe maximum allowed price is 100lv."
    private const val MESSAGE_INVALID_PRICE = "Invalid price."
    private const val MESSAGE_OVERCHARGE_MAX_VALUE = "Тhe maximum allowed overcharge is 100lv."
    private const val MESSAGE_INVALID_OVERCHARGE = "Invalid overcharge."
    private const val MESSAGE_EMPTY_PRICE = "Тhe price cannot be empty."
    private const val MESSAGE_EMPTY_OVERCHARGE = "Тhe overcharge cannot be empty."
    private const val MESSAGE_EMPTY_NAME = "Product name cannot be empty."
    private const val REGEX = "^((?=.)(?=[0-9]+))|([0-9]+)|((?=[0-9]+)(?=.)(?=[0-9]+))\$"

    enum class ProductFieldElements {
        NAME,
        BARCODE,
        PRICE,
        OVERCHARGE
    }

    //, isBarcodeDuplicatedAction: (String) -> Boolean
    fun isFieldValid(
        editTextView: EditText,
        inputLayoutView: TextInputLayout,
        fieldType: ProductFieldElements,
        isBarcodeDuplicatedAction: (String) -> Boolean
    ): Boolean {
        return when (fieldType) {
            ProductFieldElements.NAME -> validateName(editTextView, inputLayoutView)
            ProductFieldElements.BARCODE -> validateBarcode(
                isBarcodeDuplicatedAction,
                editTextView,
                inputLayoutView
            )
            ProductFieldElements.PRICE -> validatePrice(editTextView, inputLayoutView)
            ProductFieldElements.OVERCHARGE -> validateOvercharge(editTextView, inputLayoutView)
        }
    }

    fun areAllFieldsValid(
        nameLayout: TextInputLayout,
        priceLayout: TextInputLayout,
        overchargeLayout: TextInputLayout,
        barcodeLayout: TextInputLayout
    ): Boolean {
        return nameLayout.error == null && priceLayout.error == null && overchargeLayout.error == null && barcodeLayout.error == null
    }

    private fun validateName(nameView: EditText, nameLayout: TextInputLayout): Boolean {
        val name = nameView.text.toString()
        nameLayout.error = null
        nameLayout.isErrorEnabled = false
        if (name.isBlank())
            nameLayout.error = MESSAGE_EMPTY_NAME

        return nameLayout.error == null
    }

    private fun validateOvercharge(
        overchargeView: EditText,
        overchargeLayout: TextInputLayout
    ): Boolean {
        val overcharge = overchargeView.text.toString()
        overchargeLayout.error = null
        overchargeLayout.isErrorEnabled = false
        if (isValidPrice(overcharge)) {
            if (overcharge.toFloat() > InfoProductFragment.MAX_PRICE) overchargeLayout.error =
                MESSAGE_OVERCHARGE_MAX_VALUE
        } else if (overcharge.isBlank()) {
            overchargeLayout.error = MESSAGE_EMPTY_OVERCHARGE
        } else {
            overchargeLayout.error = MESSAGE_INVALID_OVERCHARGE
        }

        return overchargeLayout.error == null
    }

    private fun validatePrice(priceView: EditText, priceLayout: TextInputLayout): Boolean {
        val price = priceView.text.toString()
        priceLayout.error = null
        priceLayout.isErrorEnabled = false
        if (isValidPrice(price)) {
            if (price.toFloat() > InfoProductFragment.MAX_PRICE) priceLayout.error =
                MESSAGE_PRICE_MAX_VALUE
        } else if (price.isBlank()) {
            priceLayout.error = MESSAGE_EMPTY_PRICE
        } else {
            priceLayout.error = MESSAGE_INVALID_PRICE
        }

        return priceLayout.error == null
    }


    private fun validateBarcode(
        isBarcodeDuplicatedAction: (String) -> Boolean,
        barcodeView: EditText,
        barcodeLayout: TextInputLayout
    ): Boolean {
        barcodeLayout.error = null
        barcodeLayout.isErrorEnabled = false
        val barcode = barcodeView.text.toString()

        if (barcode.isBlank()) {
            barcodeLayout.error = MESSAGE_EMPTY_BARCODE
        }
        if (isBarcodeDuplicatedAction(barcode)) {
            barcodeLayout.error = MESSAGE_BARCODE
        }

        return barcodeLayout.error == null
    }

    private fun isValidPrice(price: String): Boolean =
        Regex(pattern = REGEX).containsMatchIn(price)

}