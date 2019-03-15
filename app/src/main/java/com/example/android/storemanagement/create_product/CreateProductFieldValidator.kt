package com.example.android.storemanagement.create_product

import android.widget.EditText

object CreateProductFieldValidator {

    private const val MESSAGE_BARCODE = "A product with the same barcode already exists or the barcode is empty."
    private const val MESSAGE_PRICE = "Тhe maximum allowed price is 100лв."
    private const val MESSAGE_INVALID_PRICE = "Invalid price."
    private const val MESSAGE_OVERCHARGE = "Тhe maximum allowed overcharge is 100лв."
    private const val MESSAGE_INVALID_OVERCHARGE = "Invalid overcharge."
    private const val MESSAGE_PRICE_ZERO = "Тhe price can't be 0лв."
    private const val MESSAGE_EMPTY_NAME = "Product name can't be empty."
    private const val REGEX = "^((?=.)(?=[0-9]+))|([0-9]+)|((?=[0-9]+)(?=.)(?=[0-9]+))\$"

    fun validate(
        nameView: EditText, priceView: EditText, overchargeView: EditText, barcodeView: EditText,
        isBarcodeDuplicatedAction: (String) -> Boolean
    ): Boolean {
        val anyFieldEmpty = isAnyFieldEmpty(nameView, priceView, overchargeView, barcodeView)

        val isOverchargeCorrect = validateOvercharge(overchargeView)
        val isNameCorrect = validateName(nameView)
        val isBarcodeCorrect = validateBarcode(isBarcodeDuplicatedAction, barcodeView)
        val isPriceCorrect = validatePrice(priceView)

        return !anyFieldEmpty && isBarcodeCorrect && isPriceCorrect && isOverchargeCorrect && isNameCorrect
    }

    private fun validateName(nameView: EditText): Boolean {
        val name = nameView.text.toString()

        if (name.isBlank())
            nameView.error = MESSAGE_EMPTY_NAME

        return nameView.error == null
    }

    private fun validateOvercharge(overchargeView: EditText): Boolean {
        val overcharge = overchargeView.text.toString()

        if (isPriceValid(overcharge)) {
            if (overcharge.toFloat() > InfoProductFragment.MAX_PRICE) overchargeView.error =
                MESSAGE_OVERCHARGE
        }
        else
            overchargeView.error = MESSAGE_INVALID_OVERCHARGE

        return overchargeView.error == null
    }

    private fun validatePrice(priceView: EditText): Boolean {
        val price = priceView.text.toString()

        if (isPriceValid(price)) {
            if (price.toFloat() > InfoProductFragment.MAX_PRICE) priceView.error = MESSAGE_PRICE
            if (price.toFloat() <= 0) priceView.error = MESSAGE_PRICE_ZERO
        } else {
            priceView.error = MESSAGE_INVALID_PRICE
        }

        return priceView.error == null
    }

    private fun validateBarcode(isBarcodeDuplicatedAction: (String) -> Boolean, barcodeView: EditText): Boolean {
        val barcode = barcodeView.text.toString()

        if (isBarcodeDuplicatedAction(barcode) || barcode.isBlank())
            barcodeView.error = MESSAGE_BARCODE

        return barcodeView.error == null
    }

    private fun isPriceValid(price: String): Boolean =
        Regex(pattern = REGEX).containsMatchIn(price)

    private fun isAnyFieldEmpty(name: EditText, price: EditText, overcharge: EditText, barcode: EditText): Boolean =
        name.text.isBlank() || price.text.isBlank() || overcharge.text.isBlank() || barcode.text.isBlank()
}