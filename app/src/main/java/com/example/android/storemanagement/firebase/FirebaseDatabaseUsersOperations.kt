package com.example.android.storemanagement.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

object FirebaseDatabaseUsersOperations {

    fun addFirebaseUser(completionHandler: (fbUserId: String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val uniqueId: String = user!!.uid
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Users")
        val usersQuery = myRef.orderByChild("id").equalTo(uniqueId)
        val users = mutableListOf<FirebaseUserInternal>()
        usersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
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
                for (fbUser in users) {
                    val firebaseUser = fbUser.getValue(FirebaseUserInternal::class.java)
                    if (firebaseUser?.id == uniqueId) {
                        completionHandler(firebaseUser)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
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
}