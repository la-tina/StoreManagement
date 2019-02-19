package com.example.android.storemanagement.database

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.example.android.storemanagement.Product
import com.example.android.storemanagement.ProductDao
import android.os.AsyncTask


//A Repository class abstracts access to multiple data sources
//A Repository manages queries and allows you to use multiple backends.
class ProductRepository (private val productDao: ProductDao) {

    //Observed LiveData will notify the observer when the data has changed.
    val allProducts: LiveData<List<Product>> = productDao.getAllProducts()
    val inStockProducts: LiveData<List<Product>> = productDao.getInStockProducts()
    val lowStockProducts: LiveData<List<Product>> = productDao.getLowStockProducts()

    @WorkerThread
    suspend fun insert(product: Product) {
        productDao.insert(product)
    }

    @WorkerThread
    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    @WorkerThread
    suspend fun updateQuantity(product: String, quantity: Int) {
        productDao.updateQuantity(product, quantity)
    }

    @WorkerThread
    suspend fun getInStockProducts(){
        productDao.getInStockProducts()
    }

    @WorkerThread
    suspend fun getLowStockProducts(){
        productDao.getLowStockProducts()
    }
}