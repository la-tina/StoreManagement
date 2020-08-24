package com.example.android.storemanagement.edit_order

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentRepository
import com.example.android.storemanagement.products_database.ProductRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class EditOrderViewModel(application: Application) : AndroidViewModel(application) {

    private val productsInOrderDao = ProductRoomDatabase.getDatabase(application).orderContentDao()

    private val repository: OrderContentRepository = OrderContentRepository(productsInOrderDao)

    val allProductsInOrder: LiveData<List<OrderContent>> = repository.allOrderContents

    private var parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    fun updateQuantity(barcode: String, quantity: Int) {
        scope.launch(Dispatchers.IO) {
            repository.updateQuantity(barcode, quantity)
        }
    }
//    fun getProductsInOrder(orderId: Long) =
//        repository.allOrderContents.value?.filter { it.orderId == orderId }
}