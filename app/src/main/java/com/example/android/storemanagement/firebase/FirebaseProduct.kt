package com.example.android.storemanagement.firebase

import java.io.Serializable

class FirebaseProduct(
    val name: String,
    val price: String,
    val overcharge: String,
    val barcode: String,
    val quantity: String,
    val id: String): Serializable {
    constructor() : this("", "",
        "", "","", "")
}
