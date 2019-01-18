package com.example.android.storemanagement.database

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.example.android.storemanagement.Product
import com.example.android.storemanagement.ProductDao

//A Repository class abstracts access to multiple data sources
//A Repository manages queries and allows you to use multiple backends.
class ProductRepository (private val productDao: ProductDao) {

    //Observed LiveData will notify the observer when the data has changed.
    val allProducts: LiveData<List<Product>> = productDao.getAllProducts()

    @WorkerThread
    suspend fun insert(product: Product) {
        productDao.insert(product)
    }

}