package com.example.android.storemanagement.orders_database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.orders_tab.OrderStatus
import com.example.android.storemanagement.products_database.Product
import java.io.Serializable


@Entity(tableName = "Orders")
data class Order(
    val finalPrice: Float,
    val date: String,
    val orderStatus: String
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    constructor() : this(0F, "",
        "")
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

//    @Query("SELECT deletedOrderContent FROM Orders ORDER BY Date DESC")
//    fun getDeletedOrderContents(): LiveData<List<OrderContent>>

    @Query("UPDATE Orders SET FinalPrice = :finalPrice WHERE Id = :id")
    fun updateFinalPrice(finalPrice: Float, id: Long)

    @Query("UPDATE Orders SET date = :date WHERE Id = :id")
    fun updateDate(date: String, id: Long)

    @Query("SELECT * FROM OrderContent WHERE orderId = :orderId ")
    fun getCurrentOrderContents(orderId: Long): LiveData<List<OrderContent>>

    @Query("UPDATE Orders SET orderStatus = :orderStatus WHERE id = :id")
    fun onOrderStatusChanged(id: Long, orderStatus: String)

//    @Query("UPDATE Orders SET deletedOrderContent = :deletedOrderContent WHERE id = :id")
//    fun addDeletedProducts(id: Long, deletedOrderContent: List<OrderContent>)

//    @Query("SELECT orderId, barcode, productBarcode FROM Products, OrderContent WHERE orderId = :orderId & productBarcode = barcode")
//    fun getCurrentProducts(orderId: Long): LiveData<List<Product>>


//    @Query("SELECT price FROM Products JOIN Orders WHERE barcode")
//    fun getPriceFromProducts()
}


