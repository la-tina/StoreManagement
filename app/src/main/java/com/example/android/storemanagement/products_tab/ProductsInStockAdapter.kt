package com.example.android.storemanagement.products_tab

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.android.storemanagement.R
import com.example.android.storemanagement.products_database.Product


class ProductsInStockAdapter(
    private val context: Context,
    private val deleteProductAction: (Product) -> Unit,
    private val openEditProductTab: (Product) -> Unit
) :
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
        val currentProduct = products[position]
        holder.productType.text = currentProduct.name
        holder.productPrice.text = currentProduct.price.toString()
        holder.productQuantity.text = currentProduct.quantity.toString()
        holder.imageContextMenu.setOnClickListener { view -> showPopup(view, currentProduct) }
    }

    private fun showPopup(view: View, product: Product) {
        PopupMenu(context, view).apply {
            inflate(R.menu.context_menu)
            setOnMenuItemClickListener { item: MenuItem? ->
                when (item!!.itemId) {
                    R.id.edit -> {
                        Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                        openEditProductTab(product)
                    }
                    R.id.delete -> {
                        Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                        deleteProductAction(product)
                    }
                }
                true
            }
            show()
        }
    }

    fun setProducts(products: List<Product>) {
        this.products.clear()
        this.products.addAll(products)
        notifyDataSetChanged()
    }
}