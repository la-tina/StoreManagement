package com.example.android.storemanagement.database

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.example.android.storemanagement.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


//The ViewModel's role is to provide data to the UI and survive configuration changes.

 class ProductViewModel(application: Application) : AndroidViewModel(application)
{
    var quantities = mutableMapOf<String, Int>()

    //Define a parentJob, and a coroutineContext.
    //The coroutineContext, by default, uses the parentJob and the main dispatcher
    //to create a new instance of a CoroutineScope based on the coroutineContext.

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    //Add a private member variable to hold a reference to the repository.
    private val repository: ProductRepository

    //Add a private LiveData member variable to cache the list of products.
    val allProducts: LiveData<List<Product>>

    val inStockProducts: LiveData<List<Product>>

    val lowStockProducts: LiveData<List<Product>>

    //Create an init block that gets a reference to the ProductDao from the
    //ProductRoomDatabase and constructs the ProductRepository based on it.
    init {
        val productsDao = ProductRoomDatabase.getDatabase(
            application
        ).productDao()
        repository = ProductRepository(productsDao)
        allProducts = repository.allProducts
        inStockProducts = repository.inStockProducts
        lowStockProducts = repository.lowStockProducts
    }

    //Because we're doing a database operation, we're using the IO Dispatcher.
    fun insert(product: Product) = scope.launch(Dispatchers.IO) {
        repository.insert(product)
    }

    fun deleteProduct(product: Product) = scope.launch(Dispatchers.IO) {
        repository.deleteProduct(product)
    }

    fun updateQuantity(product: String, quantity: Int) = scope.launch(Dispatchers.IO) {
        repository.updateQuantity(product, quantity)
        quantities[product] = quantity
    }

    fun getInStockProducts() = scope.launch(Dispatchers.IO){
        repository.getInStockProducts()
    }

    fun getLowStockProducts() = scope.launch(Dispatchers.IO){
        repository.getLowStockProducts()
    }

    //when the ViewModel is no longer used
    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}



