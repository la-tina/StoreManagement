package com.example.android.storemanagement.orders_database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.products_database.Product
import java.io.Serializable


@Entity(tableName = "Orders")
data class Order(
    val finalPrice: Float,
    val date: String,
    val isOrdered: Boolean
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

@Dao
interface OrderDao {

    @Insert
    fun insert(order: Order): Long

    @Query("DELETE FROM Orders")
    fun deleteAll()

    @Delete
    fun deleteOrder(order: Order)

    @Query("SELECT * FROM Orders ORDER BY Date DESC")
    fun getAllOrders(): LiveData<List<Order>>

    @Query("UPDATE Orders SET FinalPrice = :finalPrice WHERE Id = :id")
    fun updateFinalPrice(finalPrice: Float, id: Long)

    @Query("SELECT * FROM OrderContent WHERE orderId = :orderId ")
    fun getCurrentOrderContents(orderId: Long): LiveData<List<OrderContent>>

    @Query("UPDATE Orders SET isOrdered = :isOrdered WHERE id = :id")
    fun updateOrderStatus(id: Long, isOrdered: Boolean)

//    @Query("SELECT orderId, barcode, productBarcode FROM Products, OrderContent WHERE orderId = :orderId & productBarcode = barcode")
//    fun getCurrentProducts(orderId: Long): LiveData<List<Product>>


//    @Query("SELECT price FROM Products JOIN Orders WHERE barcode")
//    fun getPriceFromProducts()
}


