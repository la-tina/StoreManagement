package com.example.android.storemanagement.orders_database

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.products_database.ProductRoomDatabase
import kotlinx.coroutines.*
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

    lateinit var currentOrderContent: LiveData<List<OrderContent>>

    //Create an init block that gets a reference to the ProductDao from the
    //ProductRoomDatabase and constructs the ProductRepository based on it.
    init {
        val ordersDao = ProductRoomDatabase.getDatabase(
            application
        ).orderDao()
        repository = OrderRepository(ordersDao)
        allOrders = repository.allOrders
    }

    //Because we're doing a database operation, we're using the IO Dispatcher.
    fun insert(order: Order, onCompleteAction: (Long) -> Unit) =
        scope.launch(Dispatchers.IO) {
            val orderId = repository.insert(order)

            withContext(Dispatchers.Main) {
                onCompleteAction(orderId)
            }
        }

    fun deleteOrder(order: Order) = scope.launch(Dispatchers.IO) {
        repository.deleteOrder(order)
    }

    fun updateFinalPrice(finalPrice: Float, id: Long) = scope.launch(Dispatchers.IO) {
        repository.updateFinalPrice(finalPrice, id)
    }

    fun getCurrentOrderContents(orderId: Long) = scope.launch(Dispatchers.IO) {
        repository.getCurrentOrderContents(orderId)
        currentOrderContent = repository.getCurrentOrderContents(orderId)
    }

    fun updateOrderStatus(id: Long, isOrdered: Boolean) = scope.launch(Dispatchers.IO) {
        repository.updateOrderStatus(id, isOrdered)
    }

    //when the ViewModel is no longer used
    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
