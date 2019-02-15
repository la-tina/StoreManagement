package com.example.android.storemanagement

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.Delete


@Entity(tableName = "Products")
data class Product(
    @ColumnInfo(name = "Product") val name: String,
    @ColumnInfo(name = "Price") val price: Float,
    @ColumnInfo(name = "Overcharge") val overcharge: Float,
    @PrimaryKey
    @ColumnInfo(name = "Barcode") val barcode: Long,
    @ColumnInfo(name = "Quantity") val quantity: Int
)

@Dao
interface ProductDao {

    @Insert
    fun insert(product: Product)

    @Query("DELETE FROM Products")
    fun deleteAll()

    @Delete
    fun deleteProduct(product: Product)

    @Query("SELECT * FROM Products ORDER BY Product ASC")
    fun getAllProducts(): LiveData<List<Product>>

    @Query("UPDATE Products SET Quantity = :quantity WHERE Product = :product")
    fun updateQuantity(product: String, quantity: Int)

    @Query("SELECT * FROM Products WHERE Quantity > 0 ")
    fun getInStockProducts(): LiveData<List<Product>>

    @Query("SELECT * FROM Products WHERE Quantity < 5 ")
    fun getLowStockProducts(): LiveData<List<Product>>
}

