package com.example.android.storemanagement.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

object FirebaseDatabaseProductsOperations {
    fun addFirebaseProduct(product: FirebaseProduct) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        if (user != null) {
            val uniqueId: String = user.uid
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val myRef: DatabaseReference = database.getReference("Products").child(uniqueId)
            myRef.push().setValue(product)
        }
    }

    fun addFirebaseProductForUser(userId: String, product: FirebaseProduct) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Products").child(userId)
        myRef.push().setValue(product)
    }

    fun deleteFirebaseProductData(product: FirebaseProduct) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("Products")
        val productsQuery: Query =
            ref.child(uniqueId).orderByChild("barcode").equalTo(product.barcode)

        productsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (appleSnapshot in dataSnapshot.children) {
                    appleSnapshot.ref.removeValue()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
            }
        })
    }

    fun updateFirebaseProduct(
        productBarcode: String,
        productName: String,
        productOvercharge: String,
        productPrice: String
    ) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("Products")
        val productsQuery: Query =
            ref.child(uniqueId).orderByChild("barcode").equalTo(productBarcode)

        productsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (appleSnapshot in dataSnapshot.children) {
                    appleSnapshot.ref.child("name").setValue(productName)
                    appleSnapshot.ref.child("overcharge").setValue(productOvercharge)
                    appleSnapshot.ref.child("price").setValue(productPrice)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
            }
        })
    }

    fun updateFirebaseProductQuantity(barcode: String, quantity: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("Products")
        val productsQuery: Query =
            ref.child(uniqueId).orderByChild("barcode").equalTo(barcode)

        productsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (appleSnapshot in dataSnapshot.children) {
                    appleSnapshot.ref.child("quantity").setValue(quantity)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
            }
        })
    }

    fun updateFirebaseProductQuantityForUser(userId: String, barcode: String, quantity: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Products")
        val productsQuery: Query =
            ref.child(userId).orderByChild("barcode").equalTo(barcode)

        productsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (appleSnapshot in dataSnapshot.children) {
                    appleSnapshot.ref.child("quantity").setValue(quantity)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
            }
        })
    }
}