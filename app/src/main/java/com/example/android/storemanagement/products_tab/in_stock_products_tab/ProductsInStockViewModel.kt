package com.example.android.storemanagement.products_tab.in_stock_products_tab

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_database.ProductRepository
import com.example.android.storemanagement.products_database.ProductRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ProductsInStockViewModel(application: Application) : AndroidViewModel(application) {

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    private val repository: ProductRepository

    val inStockProducts: LiveData<List<Product>>

    init {
        val productsDao = ProductRoomDatabase.getDatabase(
            application
        ).productDao()
        repository = ProductRepository(productsDao)
        inStockProducts = repository.inStockProducts
    }

    fun deleteProduct(product: Product) = scope.launch(Dispatchers.IO) {
        repository.deleteProduct(product)
    }
}