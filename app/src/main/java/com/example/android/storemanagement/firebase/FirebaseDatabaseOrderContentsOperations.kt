package com.example.android.storemanagement.firebase

import android.util.Log
import com.example.android.storemanagement.firebase.FirebaseDatabaseOrdersOperations.getFirebaseOrder
import com.example.android.storemanagement.orders_tab.OrderStatus
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase


object FirebaseDatabaseOrderContentsOperations {

    fun setFirebaseOrderContentData(orderContent: FirebaseOrderContent, fbOrderId: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        if (user != null) {
            val uniqueId: String = user.uid
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val myRef: DatabaseReference = database.getReference("OrderContent").child(uniqueId)
            myRef.push().setValue(orderContent)
        }
    }

    fun getFirebaseOrderContent(fbOrderId: String): FirebaseOrderContent {
        lateinit var fbOrderContent: FirebaseOrderContent
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("OrderContent")
        val productsQuery: Query = ref.child(uniqueId).orderByChild("orderId").equalTo(fbOrderId)

        productsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val firebaseOrderContent = dataSnapshot.getValue(FirebaseOrderContent::class.java)
                if (firebaseOrderContent != null) {
                    val orderContent = FirebaseOrderContent(
                        firebaseOrderContent.productBarcode,
                        firebaseOrderContent.productName,
                        firebaseOrderContent.productPrice,
                        firebaseOrderContent.productOvercharge,
                        firebaseOrderContent.quantity,
                        firebaseOrderContent.orderId,
                        firebaseOrderContent.userId,
                        dataSnapshot.key!!
                    )
                    fbOrderContent = orderContent
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
            }
        })
        return fbOrderContent
    }

    fun checkForAvailableOrderContentsInternal(
        userId: String,
        productsUserId: String,
        barcode: String,
        completionHandler: (orderContent: FirebaseOrderContent, childAction: ChildAction) -> Unit
    ) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        if (user != null) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val myRef: DatabaseReference = database.getReference("OrderContent").child(userId)

            myRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                    val firebaseOrderContent: FirebaseOrderContent? =
                        dataSnapshot.getValue(FirebaseOrderContent::class.java)

                    if (firebaseOrderContent != null
                        && firebaseOrderContent.userId == productsUserId
                        && firebaseOrderContent.productBarcode == barcode
                    ) {
                        val orderContent = FirebaseOrderContent(
                            firebaseOrderContent.productBarcode,
                            firebaseOrderContent.productName,
                            firebaseOrderContent.productPrice,
                            firebaseOrderContent.productOvercharge,
                            firebaseOrderContent.quantity,
                            firebaseOrderContent.orderId,
                            firebaseOrderContent.userId,
                            dataSnapshot.key!!
                        )
                        getFirebaseOrder(userId, firebaseOrderContent.orderId) { order ->
                            if (order.orderStatus == OrderStatus.PENDING.toString() || order.orderStatus == OrderStatus.ORDERED.toString()
                                || order.orderStatus == OrderStatus.CONFIRMED.toString() || order.orderStatus == OrderStatus.DELIVERED.toString()
                            ) {
                                completionHandler(orderContent, ChildAction.ChildAdded)
                            }
                        }
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                    val firebaseOrderContent: FirebaseOrderContent? =
                        dataSnapshot.getValue(FirebaseOrderContent::class.java)

                    if (firebaseOrderContent != null && firebaseOrderContent.userId == productsUserId && firebaseOrderContent.productBarcode == barcode) {
                        val orderContent = FirebaseOrderContent(
                            firebaseOrderContent.productBarcode,
                            firebaseOrderContent.productName,
                            firebaseOrderContent.productPrice,
                            firebaseOrderContent.productOvercharge,
                            firebaseOrderContent.quantity,
                            firebaseOrderContent.orderId,
                            firebaseOrderContent.userId,
                            dataSnapshot.key!!
                        )
                        getFirebaseOrder(userId, firebaseOrderContent.orderId) { order ->
                            if (order.orderStatus == OrderStatus.PENDING.toString() || order.orderStatus == OrderStatus.ORDERED.toString()
                                || order.orderStatus == OrderStatus.CONFIRMED.toString() || order.orderStatus == OrderStatus.DELIVERED.toString()
                            ) {
                                completionHandler(orderContent, ChildAction.ChildChanged)
                            }
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    fun getFirebaseUserOrderContents(
        productsUserId: String,
        barcode: String,
        checkIfContentsExist: Boolean = false,
        completionHandler: (orderContent: FirebaseOrderContent, childAction: ChildAction) -> Unit
    ) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val firebaseUser = dataSnapshot.getValue(FirebaseUserInternal::class.java)
                if (firebaseUser != null) {
                    if (checkIfContentsExist) {
                        checkForAvailableOrderContentsInternal(
                            firebaseUser.id,
                            productsUserId,
                            barcode
                        ) { orderContent, childAction ->
                            completionHandler(orderContent, childAction)
                        }
                    } else {
                        getFirebaseUserOrderContentsInternal(
                            firebaseUser.id,
                            productsUserId,
                            barcode
                        ) { orderContent, childAction ->
                            completionHandler(orderContent, childAction)
                        }
                    }
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun getFirebaseUserOrderContentsInternal(
        userId: String,
        productsUserId: String,
        barcode: String,
        completionHandler: (orderContent: FirebaseOrderContent, childAction: ChildAction) -> Unit
    ) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        if (user != null) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val myRef: DatabaseReference = database.getReference("OrderContent").child(userId)

            myRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                    val firebaseOrderContent: FirebaseOrderContent? =
                        dataSnapshot.getValue(FirebaseOrderContent::class.java)

                    if (firebaseOrderContent != null && firebaseOrderContent.userId == productsUserId && firebaseOrderContent.productBarcode == barcode) {
                        val orderContent = FirebaseOrderContent(
                            firebaseOrderContent.productBarcode,
                            firebaseOrderContent.productName,
                            firebaseOrderContent.productPrice,
                            firebaseOrderContent.productOvercharge,
                            firebaseOrderContent.quantity,
                            firebaseOrderContent.orderId,
                            firebaseOrderContent.userId,
                            dataSnapshot.key!!
                        )
                        getFirebaseOrder(userId, firebaseOrderContent.orderId) { order ->
                            if (order.orderStatus == OrderStatus.PENDING.toString() || order.orderStatus == OrderStatus.ORDERED.toString()) {
                                completionHandler(orderContent, ChildAction.ChildAdded)
                            }
                        }
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                    val firebaseOrderContent: FirebaseOrderContent? =
                        dataSnapshot.getValue(FirebaseOrderContent::class.java)

                    if (firebaseOrderContent != null && firebaseOrderContent.userId == productsUserId && firebaseOrderContent.productBarcode == barcode) {
                        val orderContent = FirebaseOrderContent(
                            firebaseOrderContent.productBarcode,
                            firebaseOrderContent.productName,
                            firebaseOrderContent.productPrice,
                            firebaseOrderContent.productOvercharge,
                            firebaseOrderContent.quantity,
                            firebaseOrderContent.orderId,
                            firebaseOrderContent.userId,
                            dataSnapshot.key!!
                        )
                        getFirebaseOrder(userId, firebaseOrderContent.orderId) { order ->
                            if (order.orderStatus == OrderStatus.PENDING.toString() || order.orderStatus == OrderStatus.ORDERED.toString()) {
                                completionHandler(orderContent, ChildAction.ChildChanged)
                            }
                        }
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    val firebaseOrderContent: FirebaseOrderContent? =
                        dataSnapshot.getValue(FirebaseOrderContent::class.java)

                    if (firebaseOrderContent != null && firebaseOrderContent.userId == productsUserId && firebaseOrderContent.productBarcode == barcode) {
                        val orderContent = FirebaseOrderContent(
                            firebaseOrderContent.productBarcode,
                            firebaseOrderContent.productName,
                            firebaseOrderContent.productPrice,
                            firebaseOrderContent.productOvercharge,
                            firebaseOrderContent.quantity,
                            firebaseOrderContent.orderId,
                            firebaseOrderContent.userId,
                            dataSnapshot.key!!
                        )
                        getFirebaseOrder(userId, firebaseOrderContent.orderId) { order ->
                            if (order.orderStatus == OrderStatus.PENDING.toString() || order.orderStatus == OrderStatus.ORDERED.toString()) {
                                completionHandler(orderContent, ChildAction.ChildRemoved)
                            }
                        }
                    }
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    fun deleteFirebaseOrderContents(firebaseOrderId: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("OrderContent")
        val orderContentQuery: Query =
            ref.child(uniqueId).orderByChild("orderId").equalTo(firebaseOrderId)

        orderContentQuery.addListenerForSingleValueEvent(object : ValueEventListener {
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

    fun deleteFirebaseOrderContentData(firebaseOrderContentId: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("OrderContent")
        val orderContentQuery: Query =
            ref.child(uniqueId).child(firebaseOrderContentId)

        orderContentQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.ref.removeValue()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
            }
        })
    }

    fun updateFirebaseOrderContent(fbOrderContentId: String, quantity: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val ref =
            database.getReference("OrderContent")
        val orderContentsQuery: Query = ref.child(uniqueId).child(fbOrderContentId)

        orderContentsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.ref.child("quantity").setValue(quantity)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
