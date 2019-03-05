package com.example.android.storemanagement.orders_database

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    //Add a private member variable to hold a reference to the repository.
    private val repository: OrderRepository

    //Add a private LiveData member variable to cache the list of products.
    val allOrders: LiveData<List<Order>>

    //Create an init block that gets a reference to the ProductDao from the
    //ProductRoomDatabase and constructs the ProductRepository based on it.
    init {
        val ordersDao = OrderRoomDatabase.getDatabase(
            application
        ).orderDao()
        repository = OrderRepository(ordersDao)
        allOrders = repository.allOrders
    }

    //Because we're doing a database operation, we're using the IO Dispatcher.
    fun insert(order: Order) = scope.launch(Dispatchers.IO) {
        repository.insert(order)
    }

    fun deleteOrder(order: Order) = scope.launch(Dispatchers.IO)  {
        repository.deleteOrder(order)
    }

    //when the ViewModel is no longer used
    override fun onCleared()  {
        super.onCleared()
        parentJob.cancel()
    }
}
