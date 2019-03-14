package com.example.android.storemanagement.products_database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import java.io.Serializable

@Entity(tableName = "Products")
data class Product (
    @ColumnInfo(name = "Product") val name: String,
    @ColumnInfo(name = "Price") val price: Float,
    @ColumnInfo(name = "Overcharge") val overcharge: Float,
    @PrimaryKey
    @ColumnInfo(name = "Barcode") val barcode: String,
    @ColumnInfo(name = "Quantity") val quantity: Int
) : Serializable

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

    @Query("UPDATE Products SET Product = :name WHERE Barcode = :barcode")
    fun updateName(barcode: String, name: String)

    @Query("UPDATE Products SET Price = :price WHERE Product = :product")
    fun updatePrice(product: String, price: Float)

    @Query("UPDATE Products SET Overcharge = :overcharge WHERE Product = :product")
    fun updateOvercharge(product: String, overcharge: Float)

    @Query("SELECT * FROM Products WHERE Quantity > 0 ORDER BY Product ASC")
    fun getInStockProducts(): LiveData<List<Product>>

    @Query("SELECT * FROM Products WHERE Quantity < 5 ORDER BY Product ASC")
    fun getLowStockProducts(): LiveData<List<Product>>
}

