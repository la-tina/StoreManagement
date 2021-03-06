package com.example.android.storemanagement.order_content_database

import androidx.lifecycle.LiveData
import androidx.annotation.WorkerThread
import android.util.Log

class OrderContentRepository(private val orderContentDao: OrderContentDao) {
    //Observed LiveData will notify the observer when the data has changed.
    val allOrderContents: LiveData<List<OrderContent>> = orderContentDao.getAllOrderContents()

    @WorkerThread
    suspend fun insert(product: OrderContent) {
        Log.v("Room", "Inserting order content : $product")
        orderContentDao.insert(product)
    }

    @WorkerThread
    suspend fun deleteOrderContent(orderContent: OrderContent) {
        Log.v("Room", "Deleting order content : $orderContent")
        orderContentDao.deleteOrderContent(orderContent)
    }

    @WorkerThread
    suspend fun updateQuantity(barcode: String, quantity: Int) {
        Log.v("Room", "Updating order content barcode : $barcode , quantity : $quantity")
        orderContentDao.updateQuantity(barcode, quantity)
    }

    @WorkerThread
    suspend fun updateQuantityOrderContent(barcode: String, quantity: Int, orderId: Long) {
        Log.v("Room", "Updating order content barcode : $barcode , quantity : $quantity")
        orderContentDao.updateQuantityOrderContent(barcode, quantity, orderId)
    }

    @WorkerThread
    suspend fun updateOrderFinalPrice(id: Long, finalPrice: Float) {
        Log.v("Room", "Updating order content barcode : $id , quantity : $finalPrice")
        orderContentDao.updateOrderFinalPrice(id, finalPrice)
    }
}