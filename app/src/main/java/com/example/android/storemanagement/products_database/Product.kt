package com.example.android.storemanagement.products_database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import java.io.Serializable

@Entity(tableName = "Products")
data class Product(
    val name: String,
    val price: Float,
    val overcharge: Float,
    @PrimaryKey
    val barcode: String,
    val quantity: Int
) : Serializable

@Dao
interface ProductDao {

    @Insert
    fun insert(product: Product)

    @Query("DELETE FROM Products")
    fun deleteAll()

    @Delete
    fun deleteProduct(product: Product)

    @Query("SELECT * FROM Products ORDER BY name ASC")
    fun getAllProducts(): LiveData<List<Product>>

    @Query("UPDATE Products SET Quantity = :quantity WHERE name = :product")
    fun updateQuantity(product: String, quantity: Int)

    @Query("UPDATE Products SET name = :name WHERE Barcode = :barcode")
    fun updateName(barcode: String, name: String)

    @Query("UPDATE Products SET Price = :price WHERE name = :product")
    fun updatePrice(product: String, price: Float)

    @Query("UPDATE Products SET Overcharge = :overcharge WHERE name = :product")
    fun updateOvercharge(product: String, overcharge: Float)

    @Query("SELECT * FROM Products WHERE Quantity > 0 ORDER BY name ASC")
    fun getInStockProducts(): LiveData<List<Product>>

    @Query("SELECT * FROM Products WHERE Quantity < 5 ORDER BY name ASC")
    fun getLowStockProducts(): LiveData<List<Product>>

    @Query("UPDATE Products  SET quantity = :quantity WHERE barcode = :barcode")
    fun updateProductQuantity(barcode: String, quantity: Int)
}

