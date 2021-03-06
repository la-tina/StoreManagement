package com.example.android.storemanagement.products_tab.all_products_tab

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsAdapter
import com.example.android.storemanagement.products_tab.ProductsHolder


class AllProductsAdapter(
    private val context: Context,
    deleteProductAction: (Product?, FirebaseProduct?) -> Unit,
    openEditProductTab: (Product?, FirebaseProduct?) -> Unit
) : ProductsAdapter(context, deleteProductAction, openEditProductTab) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsHolder =
        ProductsHolder(LayoutInflater.from(context).inflate(R.layout.product_item, parent, false))

    override fun onBindViewHolder(holder: ProductsHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.productImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.watermelon))
    }
}
