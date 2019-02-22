package com.example.android.storemanagement.orders_database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context


@Database(entities = [Order::class], version = 1)
abstract class OrderRoomDatabase: RoomDatabase(){

    abstract fun orderDao(): OrderDao

    //ProductRoomDatabase is singleton to prevent having multiple instances of the database opened at the same time.
    companion object {
        @Volatile
        private var INSTANCE: OrderRoomDatabase? = null


        fun getDatabase(
            context: Context
        ): OrderRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                // Create database here
                //create a RoomDatabase object in the application context from the ProductRoomDatabase class
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OrderRoomDatabase::class.java,
                    "Order_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}