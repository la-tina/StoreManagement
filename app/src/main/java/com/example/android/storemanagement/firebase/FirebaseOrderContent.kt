package com.example.android.storemanagement.firebase

data class FirebaseOrderContent(val productBarcode: String,
                                val productName: String,
                                val productPrice: String,
                                val productOvercharge: String,
                                val quantity: String,
                                val orderId: String,
                                val id: String) {
    constructor() : this("", "",
        "", "","", "", "")
}