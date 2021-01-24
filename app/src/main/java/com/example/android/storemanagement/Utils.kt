package com.example.android.storemanagement

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object Utils {

    const val PRODUCT_NAME = "productName"
    const val PRODUCT_PRICE = "productPrice"
    const val PRODUCT_OVERCHARGE = "productOvercharge"
    const val PRODUCT_PERCENTAGE = "productPercentage"
    const val PRODUCT_BARCODE = "productBarcode"

    fun getFormattedDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd/M/yyyy HH:mm:ss", Locale.ROOT)
        val currentDate = simpleDateFormat.format(Date())
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentDate
    }
}