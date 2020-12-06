package com.example.android.storemanagement.products_tab.pending_products_tab

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsTabViewModel

class PendingProductsViewModel(application: Application) : ProductsTabViewModel(application) {

    val pendingProducts: LiveData<List<OrderContent>> = orderContentRepository.allOrderContents
    val products: LiveData<List<Product>> = productRepository.allProducts
    val orders: LiveData<List<Order>> = ordersRepository.allOrders

}