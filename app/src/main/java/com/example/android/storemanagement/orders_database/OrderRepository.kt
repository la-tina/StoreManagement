package com.example.android.storemanagement.orders_database

import androidx.lifecycle.LiveData
import androidx.annotation.WorkerThread
import android.util.Log
import com.example.android.storemanagement.order_content_database.OrderContent

class OrderRepository(private val orderDao: OrderDao) {

    //Observed LiveData will notify the observer when the data has changed.
    val allOrders: LiveData<List<Order>> = orderDao.getAllOrders()
//    val deletedOrderContent: LiveData<List<OrderContent>> = orderDao.getDeletedOrderContents()

    @WorkerThread
    suspend fun insert(order: Order): Long {
        Log.v("Room", "Inserting order : $order")
        return orderDao.insert(order)
    }

    @WorkerThread
    fun deleteOrder(order: Order) {
        Log.v("Room", "Deleting order : $order")
        orderDao.deleteOrder(order)
    }

    @WorkerThread
    fun updateFinalPrice(finalPrice: Float, id: Long) {
        Log.v("Room", "Updating order price : $finalPrice with id : $id")
        orderDao.updateFinalPrice(finalPrice, id)
    }

    @WorkerThread
    fun updateDate(date: String, id: Long) {
        Log.v("Room", "Updating order date : $date with id : $id")
        orderDao.updateDate(date, id)
    }

    @WorkerThread
    fun getCurrentOrderContents(orderId: Long): LiveData<List<OrderContent>>{
        Log.v("Room", "Get current order contents with id : $orderId")
        return orderDao.getCurrentOrderContents(orderId)
    }

    @WorkerThread
    fun updateOrderStatus(id: Long, isOrdered: Boolean) {
        Log.v("Room", "Updating isOrdered status : $isOrdered for id : $id")
        orderDao.updateOrderStatus(id, isOrdered)
    }

//    @WorkerThread
//    fun addDeletedProducts(id: Long, deletedOrderContent: List<OrderContent>) {
//        Log.v("Room", "Updating isOrdered status : $deletedOrderContent for id : $id")
//        orderDao.addDeletedProducts(id, deletedOrderContent)
//    }

//    @WorkerThread
//    fun getCurrentProducts(orderId: Long){
//        orderDao.getCurrentProducts(orderId)
//    }
}