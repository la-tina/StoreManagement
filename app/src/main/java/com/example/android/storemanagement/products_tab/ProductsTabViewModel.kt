package com.example.android.storemanagement.products_tab

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
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

    protected val repository: ProductRepository = ProductRepository(productsDao)

    private var parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    fun deleteProduct(product: Product) {
        scope.launch(Dispatchers.IO) {
            repository.deleteProduct(product)
        }
    }
}