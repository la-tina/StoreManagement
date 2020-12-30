package com.example.android.storemanagement.firebase

import java.util.*

data class FirebaseOrderContent(val productBarcode: String,
                                val productName: String,
                                val productPrice: String,
                                val productOvercharge: String,
                                val quantity: String,
                                val orderId: String,
                                val id: String) {
    constructor() : this("", "",
        "", "","", "", "")

    fun isEqual(orderContent: FirebaseOrderContent?): Boolean {
        return orderContent != null
                && this.orderId == orderContent.orderId
    }
}