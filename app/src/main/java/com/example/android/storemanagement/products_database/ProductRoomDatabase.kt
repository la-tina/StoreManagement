package com.example.android.storemanagement.products_database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentDao
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_database.OrderDao

//Room is a database layer on top of an SQLite database.
@Database(entities = [Product::class, Order::class, OrderContent::class], version = 1)
abstract class ProductRoomDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    abstract fun orderDao(): OrderDao

    abstract fun orderContentDao(): OrderContentDao

    //ProductRoomDatabase is singleton to prevent having multiple instances of the database opened at the same time.
    companion object {
        @Volatile
        private var INSTANCE: ProductRoomDatabase? = null

        fun getDatabase(
            context: Context
        ): ProductRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                // Create database here
                //create a RoomDatabase object in the application context from the ProductRoomDatabase class
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProductRoomDatabase::class.java,
                    "Product_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}





