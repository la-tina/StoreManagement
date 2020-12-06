package com.example.android.storemanagement.orders_database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.orders_tab.OrderStatus
import com.example.android.storemanagement.products_database.ProductRoomDatabase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    var firebaseDatabase = FirebaseDatabase.getInstance().reference

    //Add a private member variable to hold a reference to the repository.
    private val repository: OrderRepository

    //Add a private LiveData member variable to cache the list of products.
    val allOrders: LiveData<List<Order>>
//    val deletedOrderContent: LiveData<List<OrderContent>>

    lateinit var currentOrderContent: LiveData<List<OrderContent>>

    //Create an init block that gets a reference to the ProductDao from the
    //ProductRoomDatabase and constructs the ProductRepository based on it.
    init {
        val ordersDao = ProductRoomDatabase.getDatabase(
            application
        ).orderDao()
        repository = OrderRepository(ordersDao)
        allOrders = repository.allOrders
//        deletedOrderContent = repository.deletedOrderContent
    }

    //Because we're doing a database operation, we're using the IO Dispatcher.
    fun insert(order: Order, onCompleteAction: (Long) -> Unit) =
        scope.launch(Dispatchers.IO) {
            val orderId = repository.insert(order)

            firebaseDatabase.child("order").child(order.id.toString()).setValue(order)
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

    fun updateDate(date: String, id: Long) = scope.launch(Dispatchers.IO) {
        repository.updateDate(date, id)
    }

    fun getCurrentOrderContents(orderId: Long) = scope.launch(Dispatchers.IO) {
        repository.getCurrentOrderContents(orderId)
        currentOrderContent = repository.getCurrentOrderContents(orderId)
    }

    fun onOrderStatusChanged(id: Long, orderStatus: String) = scope.launch(Dispatchers.IO) {
        repository.onOrderStatusChanged(id, orderStatus)
    }

//    fun addDeletedProducts(id: Long, deletedOrderContent: List<OrderContent>) = scope.launch(Dispatchers.IO) {
//        repository.addDeletedProducts(id, deletedOrderContent)
//    }

    //when the ViewModel is no longer used
    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
