package com.example.android.storemanagement.firebase

import java.io.Serializable

data class FirebaseUserInternal(val id: String, val name: String, val email: String, val accountType: String) : Serializable {
    constructor() : this(
        "", "",
        "", ""
    )
}