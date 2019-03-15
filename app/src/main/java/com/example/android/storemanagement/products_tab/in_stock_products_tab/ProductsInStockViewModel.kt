package com.example.android.storemanagement.products_tab.in_stock_products_tab

import android.app.Application
import android.arch.lifecycle.LiveData
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsTabViewModel

class ProductsInStockViewModel(application: Application) : ProductsTabViewModel(application) {

    val inStockProducts: LiveData<List<Product>> = repository.inStockProducts

}