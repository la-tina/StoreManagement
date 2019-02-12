package com.example.android.storemanagement

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*


@Entity(tableName = "Orders")
data class Order(
    @ColumnInfo(name = "Final Price") val finalPrice: Float,
    @ColumnInfo(name = "Date") val date: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var id: Int = 0
}

@Dao
interface OrderDao {

    @Insert
    fun insert(order: Order)

    @Query("DELETE FROM Orders")
    fun deleteAll()

    @Delete
    fun deleteOrder(order: Order)

    @Query("SELECT * FROM Orders ORDER BY Date ASC")
    fun getAllOrders(): LiveData<List<Order>>


//    @Query("SELECT price FROM Products JOIN Orders WHERE barcode")
//    fun getPriceFromProducts()

}


