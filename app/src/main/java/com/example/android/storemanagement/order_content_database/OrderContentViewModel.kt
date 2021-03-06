package com.example.android.storemanagement.order_content_database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_database.ProductRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class OrderContentViewModel(application: Application) : AndroidViewModel(application) {

    var quantities = mutableMapOf<String, Int>()

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    //Add a private member variable to hold a reference to the repository.
    private val repository: OrderContentRepository

    //Add a private LiveData member variable to cache the list of products.
    val allOrderContents: LiveData<List<OrderContent>>

    //Create an init block that gets a reference to the ProductDao from the
    //ProductRoomDatabase and constructs the ProductRepository based on it.
    init {
        val ordersDao = ProductRoomDatabase.getDatabase(
            application
        ).orderContentDao()
        repository = OrderContentRepository(ordersDao)
        allOrderContents = repository.allOrderContents
    }

    fun insert(productInOrder: OrderContent) = scope.launch(Dispatchers.IO) {
        repository.insert(productInOrder)
    }

    fun updateQuantity(barcode: String, quantity: Int, orderId: Long) = scope.launch(Dispatchers.IO) {
//        repository.updateQuantity(barcode, quantity)
        repository.updateQuantityOrderContent(barcode, quantity, orderId)
        quantities[barcode] = quantity
    }

    fun deleteOrderContent(orderContent: OrderContent) = scope.launch(Dispatchers.IO) {
//        repository.updateQuantity(barcode, quantity)
        repository.deleteOrderContent(orderContent)
    }

    fun updateOrderFinalPrice(id: Long, finalPrice: Float) = scope.launch(Dispatchers.IO) {
        repository.updateOrderFinalPrice(id, finalPrice)
    }
 }