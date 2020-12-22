package com.example.android.storemanagement.firebase

import android.util.Log
import com.example.android.storemanagement.products_database.Product
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase


object FirebaseDatabaseOperations {

    fun setFirebaseOrderData(order: FirebaseOrder): String {
        val user: FirebaseUser? = Firebase.auth.currentUser
//        if (user != null) {
        val uniqueId: String = user?.uid!!
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Orders").child(uniqueId)
        val orderKey = myRef.push().key!!
//        updateFirebaseOrderId(orderId, orderKey)
        myRef.child(orderKey).setValue(order)
        return orderKey
//        }
    }

    fun getFirebaseOrderContents(fbOrderId: String): List<FirebaseOrderContent> {
        val orderContents: MutableList<FirebaseOrderContent> = mutableListOf()
        val user: FirebaseUser? = Firebase.auth.currentUser
        if (user != null) {
            val uniqueId: String = user.uid
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val myRef: DatabaseReference = database.getReference("Orders").child(uniqueId)
            myRef.child(fbOrderId).child("OrderContent")
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val firebaseOrderContents = dataSnapshot.children

                    for (item in firebaseOrderContents) {
                        val firebaseOrderContent: FirebaseOrderContent? =
                            item.getValue(FirebaseOrderContent::class.java)

                        if (firebaseOrderContent != null) {
                            val orderContent = FirebaseOrderContent(
                                firebaseOrderContent.productBarcode,
                                firebaseOrderContent.productName,
                                firebaseOrderContent.productPrice,
                                firebaseOrderContent.productOvercharge,
                                firebaseOrderContent.quantity,
                                firebaseOrderContent.orderId,
                                item.key!!
                            )
                            orderContents.add(orderContent)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
        return orderContents
    }

    fun setFirebaseOrderContentData(orderContent: FirebaseOrderContent, fbOrderId: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
//        if (user != null) {
//            val uniqueId: String = user.uid
//            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
//            val myRef: DatabaseReference = database.getReference("Orders").child(uniqueId)
//            myRef.child(fbOrderId).child("OrderContent").push().setValue(orderContent)
//        }
        if (user != null) {
            val uniqueId: String = user.uid
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val myRef: DatabaseReference = database.getReference("OrderContent").child(uniqueId)
            myRef.push().setValue(orderContent)
        }
    }

    fun setFirebaseProductData(product: Product) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        if (user != null) {
            val uniqueId: String = user.uid
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val myRef: DatabaseReference = database.getReference("Products").child(uniqueId)
            myRef.push().setValue(product)
        }
    }

    fun deleteFirebaseOrderData(firebaseOrderId: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("Orders")
        val ordersQuery: Query =
            ref.child(uniqueId).child(firebaseOrderId)

        ordersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (appleSnapshot in dataSnapshot.children) {
                    appleSnapshot.ref.removeValue()
                    deleteFirebaseOrderContents(appleSnapshot.key!!)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
            }
        })
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
//                for (appleSnapshot in dataSnapshot.children) {
                dataSnapshot.ref.removeValue()
//                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
            }
        })
    }

    fun deleteFirebaseProductData(product: Product) {
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

    fun updateFirebaseOrderContent(fbOrderContentId: String, quantity: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val ref =
            database.getReference("OrderContent")
        val orderContentsQuery: Query = ref.child(uniqueId).child(fbOrderContentId)

        orderContentsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (dataSnapshot in dataSnapshot.children) {
                    dataSnapshot.ref.child("quantity").setValue(quantity)
//                }
            }

            override fun onCancelled(error: DatabaseError) {}
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


    fun updateFirebaseProductQuantity(product: Product, quantity: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("Products")
        val productsQuery: Query =
            ref.child(uniqueId).orderByChild("barcode").equalTo(product.barcode)

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