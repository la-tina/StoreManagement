package com.example.android.storemanagement.firebase

import android.util.Log
import com.example.android.storemanagement.orders_tab.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase


object FirebaseDatabaseOperations {

    enum class ChildAction {
        ChildAdded,
        ChildChanged,
        ChildRemoved
    }

    fun addFirebaseUser(completionHandler: (fbUserId: String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val uniqueId: String = user!!.uid
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Users")
        val usersQuery = myRef.orderByChild("id").equalTo(uniqueId)
//        val userKey = myRef.push().key!!
//        val firebaseUser = FirebaseUserInternal(uniqueId, user.displayName.toString(), user.email.toString())
//        myRef.child(userKey).setValue(firebaseUser)
        val users = mutableListOf<FirebaseUserInternal>()
        usersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                var isAccountDuplicated = false
//                for (fbUser in dataSnapshot.children) {
//                    isAccountDuplicated = false
//                    val firebaseUser = fbUser.getValue(FirebaseUserInternal::class.java)
//                    Log.d("TinaLogin", "fbUser " + firebaseUser?.id + " curr " + uniqueId)
//                    if (firebaseUser?.id == uniqueId) {
//                        isAccountDuplicated = true
//                    }
//                }
                if (!dataSnapshot.exists()) {
                    Log.d("addFirebaseUser", "id $uniqueId")
                    val userKey = myRef.push().key!!
                    val firebaseUser = FirebaseUserInternal(uniqueId, user.displayName.toString(), user.email.toString(), "")
                    users.add(firebaseUser)
                    myRef.child(userKey).setValue(firebaseUser)
                    completionHandler(userKey)
                } else {
                    completionHandler(null)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun updateFirebaseUserType(fbUserId: String, userType: String, completionHandler: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val uniqueId: String = user!!.uid
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Users")
        val userQuery: Query = myRef.child(fbUserId).orderByChild("id").equalTo(uniqueId)
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.ref.child("accountType").setValue(userType)
                completionHandler()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun getCurrentFirebaseUserInternal(completionHandler: (firebaseUser: FirebaseUserInternal) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val uniqueId: String = user!!.uid
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val userQuery: Query = database.getReference("Users")
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users = dataSnapshot.children
                for (user in users) {
                    val firebaseUser = user.getValue(FirebaseUserInternal::class.java)
                    if (firebaseUser?.id == uniqueId) {
                        completionHandler(firebaseUser!!)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun isNotificationAddedForUser(
        userId: String,
        notification: FirebaseNotification,
        completionHandler: (isNotificationExisting: Boolean) -> Unit
    ) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Notifications").child(userId)
        val usersQuery = myRef.orderByChild("orderId").equalTo(notification.orderId)
        usersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    completionHandler(false)
                } else {
                    completionHandler(true)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun addFirebaseNotification(notification: FirebaseNotification, userId: String) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Notifications").child(userId)
        val notificationKey = myRef.push().key!!
        myRef.child(notificationKey).setValue(notification)
    }

    fun updateFirebaseSeenIndicator() {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("Notifications")
        val notificationsQuery: Query = ref.child(uniqueId)
        notificationsQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (notification in dataSnapshot.children) {
                    notification.ref.child("seen").setValue(true.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun areThereNewNotifications(completionHandler: (areAllNotificationsSeen: Boolean) -> Unit) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("Notifications")
        val notificationsQuery: Query = ref.child(uniqueId)
        Log.d("TinaNotifications", "")
        notificationsQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val firebaseNotification = dataSnapshot.getValue(FirebaseNotification::class.java)
                if (firebaseNotification != null && firebaseNotification.seen == false.toString()) {
                    completionHandler(false)
                    Log.d("TinaNotifications", "areAllNotSeen false")
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val firebaseNotification = dataSnapshot.getValue(FirebaseNotification::class.java)
                if (firebaseNotification != null && firebaseNotification.seen == false.toString()) {
                    completionHandler(false)
                    Log.d("TinaNotifications", "areAllNotSeen false")
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun setFirebaseOrderData(order: FirebaseOrder): String {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Orders").child(uniqueId)
        val orderKey = myRef.push().key!!
        myRef.child(orderKey).setValue(order)
        return orderKey
    }

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

    fun checkForFirebaseOrderContentsInternal(
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
//                val user = dataSnapshot.children
//                for (user in users) {
                val firebaseUser = dataSnapshot.getValue(FirebaseUserInternal::class.java)
                if (firebaseUser != null) {
                    if (checkIfContentsExist) {
                        checkForFirebaseOrderContentsInternal(
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
//                }
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

    fun getFirebaseUser(userId: String, completionHandler: (user: FirebaseUserInternal) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users = dataSnapshot.children
                for (user in users) {
                    val firebaseUser = user.getValue(FirebaseUserInternal::class.java)
                    if (firebaseUser != null && firebaseUser.id == userId) {
                        completionHandler(firebaseUser)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
            }
        })
    }


    fun deleteFirebaseOrderData(firebaseOrderId: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val ref = FirebaseDatabase.getInstance().getReference("Orders")
        val ordersQuery: Query = ref.child(uniqueId).child(firebaseOrderId)

        ordersQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.ref.removeValue()
                deleteFirebaseOrderContents(dataSnapshot.key!!)
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

//    fun getFirebaseProduct(barcode: String, completionHandler: (organizationName: String?) -> Unit) {
//        val user: FirebaseUser? = Firebase.auth.currentUser
//        val uniqueId: String = user?.uid!!
//        val ref = FirebaseDatabase.getInstance().getReference("Products")
//        val productsQuery: Query =
//            ref.child(uniqueId).orderByChild("barcode").equalTo(barcode)
//
//        productsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val firebaseProduct: FirebaseProduct? =
//                    dataSnapshot.getValue(FirebaseProduct::class.java)
//
//                if (firebaseProduct != null) {
//                    val product = FirebaseProduct(
//                        firebaseProduct.name,
//                        firebaseProduct.price,
//                        firebaseProduct.overcharge,
//                        firebaseProduct.barcode,
//                        firebaseProduct.quantity,
//                        dataSnapshot.key!!
//                    )
//                    completionHandler
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.e("Firebase_Products", "onCancelled", databaseError.toException())
//            }
//        })
//    }

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
