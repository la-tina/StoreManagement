package com.example.android.storemanagement.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

object FirebaseDatabaseOrdersOperations {

    fun setFirebaseOrderData(order: FirebaseOrder): String {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Orders").child(uniqueId)
        val orderKey = myRef.push().key!!
        myRef.child(orderKey).setValue(order)
        return orderKey
    }

    fun getFirebaseOrder(userId: String, orderId: String, completionHandler: (order: FirebaseOrder) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("Orders")
        val ordersQuery: Query =
            ref.child(userId).child(orderId)

        ordersQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val firebaseOrder = dataSnapshot.getValue(FirebaseOrder::class.java)
                if (firebaseOrder != null) {
                    val order = FirebaseOrder(
                        firebaseOrder.finalPrice,
                        firebaseOrder.date,
                        firebaseOrder.orderStatus,
                        dataSnapshot.key!!,
                        firebaseOrder.userId
                    )
                    completionHandler(order)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
            }
        })
    }

    private fun updateFirebaseOrderId(orderId: String, orderKey: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
//        if (user != null) {
        val uniqueId: String = user?.uid!!
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference =
            database.getReference("Orders").child(uniqueId).child(orderKey)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                myRef.child("id").setValue(orderId)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun updateFirebaseOrderFinalPrice(fbOrderId: String, finalPrice: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
//        if (user != null) {
        val uniqueId: String = user?.uid!!
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference =
            database.getReference("Orders")
        val ordersQuery: Query = myRef.child(uniqueId).child(fbOrderId)
        ordersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.ref.child("finalPrice").setValue(finalPrice)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun updateFirebaseOrderDate(firebaseOrderId: String, date: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
//        if (user != null) {
        val uniqueId: String = user?.uid!!
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Orders")
        val ordersQuery: Query = myRef.child(uniqueId).child(firebaseOrderId)
        ordersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.ref.child("date").setValue(date)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun updateFirebaseOrderStatus(firebaseOrderId: String, status: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
//        if (user != null) {
        val uniqueId: String = user?.uid!!
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Orders")
        val ordersQuery: Query = myRef.child(uniqueId).child(firebaseOrderId)
        ordersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val reference: DatabaseReference =
                    dataSnapshot.ref.child("orderStatus")
                reference.setValue(status)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getFirebaseOrderId(orderId: String): String {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("Orders")
        val ordersQuery: Query =
            ref.child(uniqueId).orderByChild("id").equalTo(orderId)
        var orderKey = ""

        ordersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (appleSnapshot in dataSnapshot.children) {
                    orderKey = appleSnapshot.ref.key!!
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
            }
        })
        return orderKey
    }

    fun deleteFirebaseOrderData(firebaseOrderId: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("Orders")
        val ordersQuery: Query = ref.child(uniqueId).child(firebaseOrderId)

        ordersQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.ref.removeValue()
                FirebaseDatabaseOrderContentsOperations.deleteFirebaseOrderContents(dataSnapshot.key!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
            }
        })
    }

}