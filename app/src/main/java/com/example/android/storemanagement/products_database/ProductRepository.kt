package com.example.android.storemanagement.products_database

import androidx.lifecycle.LiveData
import androidx.annotation.WorkerThread
import android.util.Log
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.setFirebaseProductData
import com.example.android.storemanagement.firebase.FirebaseProduct

//A Repository class abstracts access to multiple data sources
//A Repository manages queries and allows you to use multiple backends.
class ProductRepository (private val productDao: ProductDao) {

    //Observed LiveData will notify the observer when the data has changed.
    val allProducts: LiveData<List<Product>> = productDao.getAllProducts()
    val inStockProducts: LiveData<List<Product>> = productDao.getInStockProducts()
    val lowStockProducts: LiveData<List<Product>> = productDao.getLowStockProducts()

    @WorkerThread
    suspend fun insert(product: Product) {
        Log.v("Room","Inserting product: $product")
        productDao.insert(product)
    }

    @WorkerThread
    suspend fun deleteProduct(product: Product) {
        Log.v("Room","Deleting product: $product")
        productDao.deleteProduct(product)
    }

    @WorkerThread
    suspend fun updateQuantity(barcode: String, quantity: Int) {
        Log.v("Room","Updating product quantity , product: $barcode quantity : $quantity")
        productDao.updateQuantity(barcode, quantity)
    }

    @WorkerThread
    suspend fun updateName(barcode: String, name: String) {
        Log.v("Room","Updating product name , productBarcode: $barcode name : $name")
        productDao.updateName(barcode, name)
    }

    @WorkerThread
    suspend fun updatePrice(product: String, price: Float) {
        Log.v("Room","Updating product price , product: $product price : $price")
        productDao.updatePrice(product, price)
    }

    @WorkerThread
    suspend fun updateOvercharge(product: String, overcharge: Float) {
        Log.v("Room","Updating product overcharge , product: $product overcharge : $overcharge")
        productDao.updateOvercharge(product, overcharge)
    }

    @WorkerThread
    suspend fun getAllProducts(): LiveData<List<Product>>{
        Log.v("Room","Get all products")
        return allProducts
    }

    @WorkerThread
    fun updateProductQuantity(barcode: String, quantity: Int){
        productDao.updateProductQuantity(barcode, quantity)
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