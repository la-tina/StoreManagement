package com.example.android.storemanagement.create_order

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.R
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.store_tab.StoreProductsHolder
import kotlinx.android.synthetic.main.create_order_item.view.*

class CreateOrderAdapter(
    private val context: Context,
    private val updateFinalPriceAction: (Float) -> Unit,
    private val setOrderButtonEnabled: (Boolean) -> Unit
) : RecyclerView.Adapter<CreateOrderHolder>() {

    private var products = emptyList<Product>() // Cached copy of products

    //productName -> quantity
    var quantities = mutableMapOf<String, Int>()

    var enabledProducts = mutableMapOf<String, Boolean>()

    // Gets the number of items in the list
    override fun getItemCount(): Int {
        return products.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateOrderHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.create_order_item, parent, false)
        return CreateOrderHolder(view)
    }

    private fun onCheckChangedAction(productName: String, isChecked: Boolean) {
        enabledProducts[productName] = isChecked
        val hasEnabled = enabledProducts.values.any { it }
        setOrderButtonEnabled(hasEnabled)
    }

    // Binds each product in the list to a view
    override fun onBindViewHolder(holder: CreateOrderHolder, position: Int) {
        val currentProduct = products[position]

        holder.productName.text = currentProduct.name
        holder.productPrice.text = currentProduct.price.toString()
        holder.productQuantity.addTextChangedListener(getTextWatcher(holder))

        holder.orderCheckBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckChanged(isChecked, holder)
        }
        holder.orderCheckBox.addTextChangedListener(getTextWatcher(holder))
    }

    private fun getTextWatcher(holder: CreateOrderHolder): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (holder.productQuantity.text.toString().isEmpty()) return
                updateQuantityForProduct(holder.productName.text.toString(), holder.productQuantity.text.toString().toInt())
                updateFinalPriceAction(getPrice(holder))
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (holder.productQuantity.text.toString().isEmpty()) return
                updateFinalPriceAction(getPrice(holder) * -1)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        }
    }

    private fun getPrice(holder: CreateOrderHolder): Float {
        val quantity = holder.productQuantity.text.toString().toInt()
        val productPriceInt = holder.productPrice.text.toString().toFloat()
        return quantity * productPriceInt
    }

    private fun onCheckChanged(isChecked: Boolean, holder: CreateOrderHolder) {
        holder.productQuantity.isEnabled = isChecked
        onCheckChangedAction(holder.productName.text.toString(), isChecked)
    }

    private fun updateQuantityForProduct(productName: String, quantity: Int) {
        quantities[productName] = quantity
    }

    internal fun setProducts(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }
}

class CreateOrderHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the ProductTextView that will add each product to
    val productName = view.order_item_text!!
    val productPrice = view.order_item_price!!
    val productQuantity = view.order_item_quantity!!
    val orderCheckBox = view.order_check_box!!
}


