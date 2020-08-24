package com.example.android.storemanagement.create_order

import android.widget.EditText


object CreateOrderFieldValidator {

    private const val MESSAGE_QUANTITY_ABOVE_MAX_SIZE = "Тhe maximum allowed quantity is 500лв."

    private const val MESSAGE_ZERO_QUANTITY = "You can't make an order if the quantity is empty."

    fun isQuantityCorrect(quantityView: EditText): Boolean {
        return !isQuantityEmpty(quantityView) && !isQuantityAboveLimit(quantityView)
    }

    private fun isQuantityEmpty(quantity: EditText): Boolean {
        return quantity.text.toString().isBlank() || quantity.text.toString() == "0"
    }

    private fun isQuantityAboveLimit(quantity: EditText): Boolean {
        val isQuantityAboveLimit = quantity.text.toString().toInt() > 500
        if (isQuantityAboveLimit)
            quantity.error = MESSAGE_QUANTITY_ABOVE_MAX_SIZE
        return isQuantityAboveLimit
    }
}
