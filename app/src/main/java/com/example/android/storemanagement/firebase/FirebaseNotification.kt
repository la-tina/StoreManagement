package com.example.android.storemanagement.firebase

import java.io.Serializable

class FirebaseNotification(
    val orderId: String,
    val fromUserId: String,
    val hasTheOrder: String,
    val seen: String
) :
    Serializable {
    constructor() : this(
        "",
        "", "", "",
    )
}