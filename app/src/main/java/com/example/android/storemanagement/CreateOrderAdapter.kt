package com.example.android.storemanagement

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        return CreateOrderHolder(updateFinalPriceAction, ::updateQuantityForProduct, ::onCheckChangedAction, view)
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
    }

    private fun updateQuantityForProduct(productName: String, quantity: Int) {
        quantities[productName] = quantity
    }

    internal fun setProducts(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }
}

class CreateOrderHolder(
    updateFinalPriceAction: (Float) -> Unit,
    updateQuantityForProductAction: (String, Int) -> Unit,
    val onCheckChangedAction: (String, Boolean) -> Unit,
    view: View
) : RecyclerView.ViewHolder(view) {
    // Holds the ProductTextView that will add each product to
    val productName = view.order_item_text!!
    val productPrice = view.order_item_price!!

    private val orderItemQuantity = view.order_item_quantity!!
    //private val quantityInt = orderItemQuantity.text!!.toString().toInt()
    //private val productPriceInt = productPrice.text!!.toString().toFloat()

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (orderItemQuantity.text.toString().isEmpty()) return
            updateQuantityForProductAction(productName.text.toString(), orderItemQuantity.text.toString().toInt())
            updateFinalPriceAction(getPrice())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (orderItemQuantity.text.toString().isEmpty()) return
            updateFinalPriceAction(getPrice() * -1)
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    private fun getPrice(): Float {
        val quantity = orderItemQuantity.text.toString().toInt()
        val productPriceInt = productPrice.text.toString().toFloat()
        return quantity * productPriceInt
    }

    init {
        view.order_check_box.setOnCheckedChangeListener { _, isChecked ->
            onCheckChanged(isChecked, view)
        }
        view.order_item_quantity.addTextChangedListener(textWatcher)
    }

    private fun onCheckChanged(isChecked: Boolean, view: View) {
        view.order_item_quantity.isEnabled = isChecked
        onCheckChangedAction(productName.text.toString(), isChecked)
    }
}

