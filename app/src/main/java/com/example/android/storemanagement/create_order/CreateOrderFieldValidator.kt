package com.example.android.storemanagement.create_order

import com.google.android.material.textfield.TextInputLayout
import android.widget.EditText
import android.widget.TextView


object CreateOrderFieldValidator {

    private const val MESSAGE_QUANTITY_ABOVE_MAX_SIZE = "Тhe maximum allowed quantity is 500."

    private const val MESSAGE_ZERO_QUANTITY = "You can't make an order if the quantity is empty."

    fun isQuantityCorrect(quantityView: EditText, quantityLayout: TextInputLayout): Boolean {
        quantityLayout.error = null
        quantityLayout.isErrorEnabled = false
        return !isQuantityEmpty(quantityView) && !isQuantityAboveLimit(quantityView)
    }

    fun isEditOrderQuantityCorrect(quantityView: EditText, quantityLayout: TextInputLayout): Boolean {
        quantityLayout.error = null
        quantityLayout.isErrorEnabled = false
        return !isQuantityAboveLimit(quantityView)
    }

    private fun isQuantityEmpty(quantity: EditText): Boolean {
        return quantity.text.toString().isBlank() || quantity.text.toString() == "0"
    }

    private fun isQuantityAboveLimit(quantity: EditText): Boolean {
        if (quantity.text.toString().isBlank()) return false
        if (isQuantityAboveLimit(quantity.text.toString().toInt()))
            quantity.error = MESSAGE_QUANTITY_ABOVE_MAX_SIZE
//            quantityLayout.error = MESSAGE_QUANTITY_ABOVE_MAX_SIZE
        return isQuantityAboveLimit(quantity.text.toString().toInt())
    }

    fun isQuantityAboveLimit(quantity: Int) =
        quantity > 500
}
