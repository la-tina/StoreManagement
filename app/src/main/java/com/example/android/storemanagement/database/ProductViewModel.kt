package com.example.android.storemanagement.database

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.example.android.storemanagement.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


//The ViewModel's role is to provide data to the UI and survive configuration changes.

 class ProductViewModel(application: Application) : AndroidViewModel(application)
{
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

    //Create an init block that gets a reference to the ProductDao from the
    //ProductRoomDatabase and constructs the ProductRepository based on it.
    init {
        val productsDao = ProductRoomDatabase.getDatabase(
            application,
            scope
        ).productDao()
        repository = ProductRepository(productsDao)
        allProducts = repository.allProducts
    }

    //Because we're doing a database operation, we're using the IO Dispatcher.
    fun insert(product: Product) = scope.launch(Dispatchers.IO) {
        repository.insert(product)
    }

    //when the ViewModel is no longer used
    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }


}



