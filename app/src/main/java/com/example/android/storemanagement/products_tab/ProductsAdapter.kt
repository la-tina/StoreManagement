package com.example.android.storemanagement.products_tab

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.R
import com.example.android.storemanagement.products_database.Product
import kotlinx.android.synthetic.main.product_item.view.*


class ProductsAdapter(private val context: Context) :
    RecyclerView.Adapter<ProductsHolder>() {

    private var products: MutableList<Product> = mutableListOf()

    // Gets the number of items in the list
    override fun getItemCount(): Int {
        return products.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsHolder {
        return ProductsHolder(
            LayoutInflater.from(context).inflate(
                R.layout.product_item,
                parent,
                false
            )
        )
    }

    // Binds each product in the list to a view
    override fun onBindViewHolder(holder: ProductsHolder, position: Int) {
        val current = products[position]
        holder.productType.text = current.name
        holder.productPrice.text = current.price.toString()
        holder.productQuantity.text = current.quantity.toString()
    }

    internal fun setProducts(products: List<Product>) {
        this.products.clear()
        this.products.addAll(products)
        notifyDataSetChanged()
    }
}

class ProductsHolder(textView: View) : RecyclerView.ViewHolder(textView) {
    // Holds the ProductTextView that will add each product to
    val productType = textView.product_item_text!!
    val productPrice = textView.product_item_price!!
    val productQuantity = textView.product_item_quantity!!
}






