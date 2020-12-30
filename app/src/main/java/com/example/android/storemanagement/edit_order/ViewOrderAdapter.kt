package com.example.android.storemanagement.edit_order

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.android.storemanagement.R
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator.isEditOrderQuantityCorrect
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator.isQuantityCorrect
import com.example.android.storemanagement.create_order.CreateOrderHolder
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.products_database.Product

class ViewOrderAdapter(
    private val context: Context) : RecyclerView.Adapter<CreateOrderHolder>() {

    private var productsInOrder = emptyList<OrderContent>() // Cached copy of products
    private var firebaseOrderContents = emptyList<FirebaseOrderContent>()
    private var products = emptyList<Product>()
    private var areFirebaseOrderContentsLoaded = true

    // Gets the number of items in the list
    override fun getItemCount(): Int {
        return if (areFirebaseOrderContentsLoaded) firebaseOrderContents.size else productsInOrder.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateOrderHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.create_order_item, parent, false)
        return CreateOrderHolder(view)
    }

    // Binds each product in the list to a view
    override fun onBindViewHolder(holder: CreateOrderHolder, position: Int) {
        val currentProductInOrder = productsInOrder[position]
        if (areFirebaseOrderContentsLoaded) {
            val currentFirebaseProductInOrder = firebaseOrderContents[position]
            holder.productName.text = currentFirebaseProductInOrder.productName
            holder.productPrice.text = currentFirebaseProductInOrder.productPrice
            holder.productQuantity.setText(currentFirebaseProductInOrder.quantity)

        } else {
            products.forEach { currentProduct ->
                if (currentProductInOrder.productBarcode == currentProduct.barcode) {
                    holder.productName.text = currentProduct.name
                    holder.productPrice.text = currentProduct.price.toString()
                    holder.productQuantity.setText(currentProductInOrder.quantity.toString())
                    holder.productQuantity.isEnabled = false
                }
            }
        }
    }

    internal fun setProductsInOrder(productsInOrder: List<OrderContent>?,
                                    firebaseProductsInOrder: List<FirebaseOrderContent>?) {
        if (productsInOrder != null) {
            this.productsInOrder = productsInOrder
            areFirebaseOrderContentsLoaded = false
        } else if (firebaseProductsInOrder != null) {
            this.firebaseOrderContents = firebaseProductsInOrder
            areFirebaseOrderContentsLoaded = true
        }
        notifyDataSetChanged()
    }

    internal fun setProducts(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }
}


