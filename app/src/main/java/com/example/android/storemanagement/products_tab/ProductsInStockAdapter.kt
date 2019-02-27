package com.example.android.storemanagement.products_tab

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.android.storemanagement.R
import com.example.android.storemanagement.products_database.Product


class ProductsInStockAdapter(private val context: Context) :
    RecyclerView.Adapter<ProductsHolder>() {

    private var products: MutableList<Product> = mutableListOf()

    override fun getItemCount(): Int =
        products.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsHolder {
        return ProductsHolder(
            LayoutInflater.from(context).inflate(
                R.layout.product_in_stock_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductsHolder, position: Int) {
        val current = products[position]
        holder.productType.text = current.name
        holder.productPrice.text = current.price.toString()
        holder.productQuantity.text = current.quantity.toString()
    }

    fun setProducts(products: List<Product>) {
        this.products.clear()
        this.products.addAll(products)
        notifyDataSetChanged()
    }
}