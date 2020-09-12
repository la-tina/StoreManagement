package com.example.android.storemanagement.create_order

import android.support.design.widget.TextInputLayout
import android.widget.EditText
import android.widget.TextView


object CreateOrderFieldValidator {

    private const val MESSAGE_QUANTITY_ABOVE_MAX_SIZE = "Тhe maximum allowed quantity is 500."

    private const val MESSAGE_ZERO_QUANTITY = "You can't make an order if the quantity is empty."

    fun isQuantityCorrect(quantityView: EditText, quantityLayout: TextInputLayout): Boolean {
        quantityLayout.error = null
        quantityLayout.isErrorEnabled = false
        return !isQuantityEmpty(quantityView) && !isQuantityAboveLimit(quantityView, quantityLayout)
    }

    private fun isQuantityEmpty(quantity: EditText): Boolean {
        return quantity.text.toString().isBlank() || quantity.text.toString() == "0"
    }

    private fun isQuantityAboveLimit(quantity: EditText, quantityLayout: TextInputLayout): Boolean {
        val isQuantityAboveLimit = quantity.text.toString().toInt() > 500
        if (isQuantityAboveLimit)
            quantity.error = MESSAGE_QUANTITY_ABOVE_MAX_SIZE
//            quantityLayout.error = MESSAGE_QUANTITY_ABOVE_MAX_SIZE
        return isQuantityAboveLimit
    }
}
