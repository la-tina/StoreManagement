package com.example.android.storemanagement.firebase

import java.io.Serializable

data class FirebaseOrder(val finalPrice: String, val date: String, val orderStatus: String, val id: String, val userId: String): Serializable{
    constructor() : this("", "",
        "", "", "")
}