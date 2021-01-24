package com.example.android.storemanagement.create_order

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout


object CreateOrderFieldValidator {

    private const val MESSAGE_QUANTITY_ABOVE_MAX_SIZE = "Тhe maximum allowed quantity is 500."

    private const val MESSAGE_ZERO_QUANTITY = "You can't make an order if the quantity is empty."

    fun isQuantityCorrectForOrder(quantityView: EditText, quantityLayout: TextInputLayout, finalProductAvailableQuantity: Int): Boolean {
        quantityLayout.error = null
        quantityLayout.isErrorEnabled = false
        return !isQuantityEmpty(quantityView) && !isQuantityAboveLimit(quantityView) && isQuantityInTheAvailableRange(quantityLayout, quantityView, finalProductAvailableQuantity)
    }

    fun isQuantityCorrectForStore(quantityView: EditText, quantityLayout: TextInputLayout): Boolean {
        quantityLayout.error = null
        quantityLayout.isErrorEnabled = false
        return !isQuantityEmpty(quantityView) && !isQuantityAboveLimit(quantityView)
    }

    private fun isQuantityInTheAvailableRange(quantityLayout: TextInputLayout, quantityView: EditText, finalProductAvailableQuantity: Int): Boolean {
        return if (!isQuantityInTheAvailableRangeInternal(quantityView.text.toString(), finalProductAvailableQuantity)) {
            quantityView.error = "Insufficient quantity."
            quantityLayout.isErrorEnabled = true
            false
        } else true
    }

    private fun isQuantityInTheAvailableRangeInternal(quantity: String, finalProductAvailableQuantity: Int) = if (quantity.isNotEmpty())
        quantity.toInt() <= finalProductAvailableQuantity else true

    fun isEditOrderQuantityCorrect(quantityView: EditText, quantityLayout: TextInputLayout, finalProductAvailableQuantity: Int): Boolean {
        quantityLayout.error = null
        quantityLayout.isErrorEnabled = false
        return !isQuantityAboveLimit(quantityView) && isQuantityInTheAvailableRangeInternal(quantityView.text.toString(), finalProductAvailableQuantity)
    }

    private fun isQuantityEmpty(quantity: EditText): Boolean {
        return quantity.text.toString().isBlank() || quantity.text.toString() == "0"
    }

    fun isQuantityAboveLimit(quantity: EditText): Boolean {
        if (quantity.text.toString().isBlank()) return false
        if (isQuantityAboveLimit(quantity.text.toString().toInt()))
            quantity.error = MESSAGE_QUANTITY_ABOVE_MAX_SIZE
//            quantityLayout.error = MESSAGE_QUANTITY_ABOVE_MAX_SIZE
        return isQuantityAboveLimit(quantity.text.toString().toInt())
    }

    fun isQuantityAboveLimit(quantity: Int) =
        quantity > 500
}
