package com.example.android.storemanagement.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.example.android.storemanagement.Product
import com.example.android.storemanagement.ProductDao
import kotlinx.coroutines.CoroutineScope

//Room is a database layer on top of an SQLite database.
@Database(entities = [Product::class], version = 1)
abstract class ProductRoomDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    //ProductRoomDatabase is singleton to prevent having multiple instances of the database opened at the same time.
    companion object {
        @Volatile
        private var INSTANCE: ProductRoomDatabase? = null


        fun getDatabase(
            context: Context,
            scope: CoroutineScope
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





