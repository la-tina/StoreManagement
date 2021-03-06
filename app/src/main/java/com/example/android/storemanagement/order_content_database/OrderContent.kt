package com.example.android.storemanagement.order_content_database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.products_database.Product
import java.io.Serializable


@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("orderId"),
            onDelete = ForeignKey.CASCADE
        )]
)
data class OrderContent(
    val productBarcode: String,
    val orderId: Long,
    val quantity: Int
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

@Dao
interface OrderContentDao {

    @Insert
    fun insert(productInOrder: OrderContent)

    @Query("SELECT * FROM OrderContent ORDER BY quantity ASC")
    fun getAllOrderContents(): LiveData<List<OrderContent>>

    @Query("UPDATE Products SET Quantity = :quantity WHERE barcode = :barcode")
    fun updateQuantity(barcode: String, quantity: Int)

    @Query("UPDATE OrderContent SET Quantity = :quantity WHERE orderId = :orderId AND productBarcode = :barcode")
    fun updateQuantityOrderContent(barcode: String, quantity: Int, orderId: Long)

    @Query("UPDATE Orders SET finalPrice = :finalPrice WHERE id = :id")
    fun updateOrderFinalPrice(id: Long, finalPrice: Float)

    @Delete
    fun deleteOrderContent(orderContent: OrderContent)
}
