package com.example.android.storemanagement.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

object FirebaseDatabaseNotificationsOperations {
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
        if (user != null) {
            val uniqueId: String = user.uid
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
    }

    fun areThereNewNotifications(completionHandler: (areAllNotificationsSeen: Boolean) -> Unit) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        if (user != null) {
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
    }
}