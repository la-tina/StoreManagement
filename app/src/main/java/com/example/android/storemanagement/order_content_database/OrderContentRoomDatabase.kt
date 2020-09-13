package com.example.android.storemanagement.order_content_database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_database.OrderDao
//import com.example.android.storemanagement.orders_database.OrderRoomDatabase

//@Database(entities = [OrderContent::class], version = 1)
//abstract class OrderContentRoomDatabase: RoomDatabase() {
//    abstract fun orderContentDao(): OrderContentDao
//
//    //ProductRoomDatabase is singleton to prevent having multiple instances of the database opened at the same time.
//    companion object {
//        @Volatile
//        private var INSTANCE: OrderContentRoomDatabase? = null
//
//        fun getDatabase(
//            context: Context
//        ): OrderContentRoomDatabase {
//            return INSTANCE ?: synchronized(this) {
//                // Create database here
//                //create a RoomDatabase object in the application context from the ProductRoomDatabase class
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    OrderContentRoomDatabase::class.java,
//                    "Order_content_database"
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}