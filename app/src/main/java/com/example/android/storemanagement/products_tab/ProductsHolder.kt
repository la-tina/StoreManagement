package com.example.android.storemanagement.products_tab

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.product_item.view.*

class ProductsHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the ProductTextView that will add each product to
    val productType = view.product_item_text!!
    val productPrice = view.product_item_price!!
    val productQuantity = view.product_item_quantity!!
    val imageContextMenu: ImageView = view.context_menu_image!!
}