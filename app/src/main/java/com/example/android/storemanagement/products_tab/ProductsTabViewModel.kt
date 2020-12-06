package com.example.android.storemanagement.products_tab

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.android.storemanagement.order_content_database.OrderContentRepository
import com.example.android.storemanagement.orders_database.OrderRepository
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_database.ProductRepository
import com.example.android.storemanagement.products_database.ProductRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class ProductsTabViewModel(application: Application) : AndroidViewModel(application) {

    private val productsDao = ProductRoomDatabase.getDatabase(application).productDao()
    val productRepository: ProductRepository = ProductRepository(productsDao)

    private val productsInOrderDao = ProductRoomDatabase.getDatabase(application).orderContentDao()
    val orderContentRepository: OrderContentRepository = OrderContentRepository(productsInOrderDao)

    private val ordersDao = ProductRoomDatabase.getDatabase(application).orderDao()
    val ordersRepository: OrderRepository = OrderRepository(ordersDao)

    private var parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    fun deleteProduct(product: Product) {
        scope.launch(Dispatchers.IO) {
            productRepository.deleteProduct(product)
        }
    }
}