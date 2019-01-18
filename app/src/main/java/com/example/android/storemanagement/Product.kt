package com.example.android.storemanagement

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*


@Entity(tableName = "Products")
data class Product(@ColumnInfo(name = "Product") val name: String,
                   @ColumnInfo(name = "Price") val price: Float,
                   @ColumnInfo(name = "Overcharge") val overcharge: Float,
                   @PrimaryKey
                   @ColumnInfo(name = "Barcode") val barcode: Long)

@Dao
interface ProductDao {

    @Insert
    fun insert(product: Product)

    @Query("DELETE FROM Products")
    fun deleteAll()

    @Query("SELECT * FROM Products ORDER BY Product ASC" )
    fun getAllProducts(): LiveData<List<Product>>
}

