package com.example.android.storemanagement.products_tab.low_stock_products_tab

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsTabViewModel

class ProductsLowStockViewModel(application: Application) : ProductsTabViewModel(application) {

    val lowStockProducts: LiveData<List<Product>> = productRepository.lowStockProducts

}