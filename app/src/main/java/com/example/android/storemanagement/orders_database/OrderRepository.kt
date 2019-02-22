package com.example.android.storemanagement.orders_database

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread

class OrderRepository(private val orderDao: OrderDao) {

    //Observed LiveData will notify the observer when the data has changed.
    val allOrders: LiveData<List<Order>> = orderDao.getAllOrders()

    @WorkerThread
    suspend fun insert(order: Order) {
        orderDao.insert(order)
    }

    @WorkerThread
    fun deleteOrder(order: Order) {
        orderDao.deleteOrder(order)
    }

}