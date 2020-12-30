package com.example.android.storemanagement.products_tab

import android.content.Context
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.products_database.Product


abstract class ProductsAdapter(
    private val context: Context,
    private val deleteProductAction: (Product?, FirebaseProduct?) -> Unit,
    private val openEditProductTab: (Product?, FirebaseProduct?) -> Unit
) : RecyclerView.Adapter<ProductsHolder>() {

    protected var products: MutableList<Product> = mutableListOf()
    protected var firebaseProducts: MutableList<FirebaseProduct> = mutableListOf()
    private var areFirebaseProductsLoaded = false

    // Gets the number of items in the list
    override fun getItemCount(): Int = if (areFirebaseProductsLoaded) firebaseProducts.size else products.size

    // Binds each product in the list to a view
    override fun onBindViewHolder(holder: ProductsHolder, position: Int) {
        if (areFirebaseProductsLoaded) {
            val currentProduct = firebaseProducts[position]
            holder.productType.text = currentProduct.name
            holder.productPrice.text = (currentProduct.price.toFloat() + currentProduct.overcharge.toFloat()).toString()
            holder.productQuantity.text = currentProduct.quantity
            holder.imageContextMenu.setOnClickListener { view -> showPopup(view, null, currentProduct) }
            holder.itemView.setOnClickListener {
                openEditProductTab(null, currentProduct)
            }
        } else {
            val currentProduct = products[position]
            holder.productType.text = currentProduct.name
            holder.productPrice.text = (currentProduct.price + currentProduct.overcharge).toString()
            holder.productQuantity.text = currentProduct.quantity.toString()
            holder.imageContextMenu.setOnClickListener { view -> showPopup(view, currentProduct, null) }
            holder.itemView.setOnClickListener {
                openEditProductTab(currentProduct, null)
            }
        }
    }

    protected fun showPopup(view: View, product: Product?, firebaseProduct: FirebaseProduct?) {
        PopupMenu(context, view).apply {
            inflate(R.menu.context_menu)
            menu.findItem(R.id.order).isVisible = false
            menu.findItem(R.id.delivered).isVisible = false
            menu.findItem(R.id.order).isVisible = false
            setOnMenuItemClickListener { item: MenuItem? ->
                when (item!!.itemId) {
                    R.id.edit -> {
                        Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                        openEditProductTab(product, firebaseProduct)
                    }
                    R.id.delete -> {
                        Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                        deleteProductAction(product, firebaseProduct)
                    }
                }
                true
            }
            show()
        }
    }

    internal fun setProducts(products: List<Product>?, fbProducts: List<FirebaseProduct>?) {
        if (products != null) {
            this.products.clear()
            this.products.addAll(products)
            areFirebaseProductsLoaded = false
        } else if (fbProducts != null){
            this.firebaseProducts.clear()
            this.firebaseProducts.addAll(fbProducts)
            areFirebaseProductsLoaded = true
        }

        notifyDataSetChanged()
    }
}
