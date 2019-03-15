package com.example.android.storemanagement.products_tab.all_products_tab

import android.app.Application
import android.arch.lifecycle.LiveData
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsTabViewModel

class AllProductsViewModel(application: Application) : ProductsTabViewModel(application) {

    val allProducts: LiveData<List<Product>> = repository.allProducts

}