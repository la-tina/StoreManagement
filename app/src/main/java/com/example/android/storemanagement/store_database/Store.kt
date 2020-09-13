package com.example.android.storemanagement.store_database

import androidx.room.*
import java.io.Serializable


@Entity(tableName = "Store")
data class Store(
    val name: String,
    val price: Float,
    val quantity: Int,
    @PrimaryKey
    val barcode: String
) : Serializable


@Dao
interface StoreDao {
    @Insert
    fun insert(store: Store): Long

    @Query("DELETE FROM Store")
    fun deleteAll()

    @Query("UPDATE Products SET quantity = :quantity WHERE barcode = :barcode")
    fun updateQuantityStore(quantity: Float, barcode: String)
}

