package com.example.android.storemanagement.edit_order

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.R
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator.isEditOrderQuantityCorrect
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator.isQuantityCorrect
import com.example.android.storemanagement.create_order.CreateOrderHolder
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.orders_tab.OrderStatus
import com.example.android.storemanagement.products_database.Product
import kotlinx.android.synthetic.main.fragment_create_order.*

class EditOrderAdapter(
    private val context: Context,
    private val updateFinalPriceAction: (Float) -> Unit,
    private val setOrderButtonEnabled: (Boolean) -> Unit,
    private val canOrderBeEdited: () -> Boolean
) : RecyclerView.Adapter<CreateOrderHolder>() {

    private var productsInOrder = emptyList<OrderContent>() // Cached copy of products
    private var products = emptyList<Product>()

    //productName -> quantity
    var quantities = mutableMapOf<String, Int>()

    var enabledProducts = mutableMapOf<String, Boolean>()
    var beforeElementIndicator: Boolean = false
    var afterElementIndicator: Boolean = false
    var initialStart = true
    var lastEditedProduct: String? = null

    // Gets the number of items in the list
    override fun getItemCount(): Int {
        return productsInOrder.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateOrderHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.create_order_item, parent, false)
        return CreateOrderHolder(view)
    }

    private fun onCheckChangedAction(productName: String, isQuantityCorrect: Boolean) {
        enabledProducts[productName] = isQuantityCorrect
    }

    // Binds each product in the list to a view
    override fun onBindViewHolder(holder: CreateOrderHolder, position: Int) {
        val currentProductInOrder = productsInOrder[position]

        products.forEach { currentProduct ->
            if (currentProductInOrder.productBarcode == currentProduct.barcode) {
                holder.productName.text = currentProduct.name
                holder.productPrice.text = currentProduct.price.toString()
                holder.productQuantity.setText(currentProductInOrder.quantity.toString())
            }
        }

        if (!canOrderBeEdited()) {
            holder.productQuantity.isEnabled = false
        }
        holder.productQuantity.addTextChangedListener(getTextWatcher(holder))
        Log.d("Watcher", "quantity addWatcher " +  holder.productQuantity.text)
    }

    private fun getTextWatcher(holder: CreateOrderHolder): TextWatcher =
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val shouldEnableOrderButton = isEditOrderQuantityCorrect(holder.productQuantity, holder.productQuantityLayout) && enabledProducts.isNotEmpty()
                setOrderButtonEnabled(shouldEnableOrderButton)

                    if (isQuantityCorrect(holder.productQuantity, holder.productQuantityLayout) && !afterElementIndicator) {
                        Log.d("Watcher", "quantity " + quantities[holder.productName.text.toString()] + " current quantity " + holder.productQuantity.text.toString().toInt() + " productname " + holder.productName.text.toString() + " lastEditedProduct " + lastEditedProduct)
                        if (quantities[holder.productName.text.toString()] == holder.productQuantity.text.toString().toInt() && holder.productName.text.toString() == lastEditedProduct && !beforeElementIndicator) {
                            Log.d("Watcher", "afterElementIndicator")
                            afterElementIndicator = true
                        } else {
                            beforeElementIndicator = false
                            lastEditedProduct = holder.productName.text.toString()
                            onQuantityChanged(shouldEnableOrderButton, holder)
                            afterElementIndicator = false
                            updateFinalPriceAction(getPrice(holder))
                            val quantity: Int = if (holder.productQuantity.text.toString().isBlank()) 0 else holder.productQuantity.text.toString().toInt()
                            updateQuantityForProduct(holder.productName.text.toString(), quantity)
                        Log.d("Watcher", "quantity after " + holder.productQuantity.text.toString().toInt() + " for product " + holder.productName.text.toString())
                        Log.d("Watcher", "watcher price after " + getPrice(holder) + " for product " + holder.productName.text.toString())
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (isQuantityCorrect(holder.productQuantity, holder.productQuantityLayout) && !beforeElementIndicator) {
                    beforeElementIndicator = true
                    afterElementIndicator = false
                    updateFinalPriceAction(getPrice(holder) * -1)
                    Log.d("Watcher", "watcher price before " + getPrice(holder) * -1 + " for product " + holder.productName.text.toString())
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setOrderButtonEnabled(isQuantityCorrect(holder.productQuantity, holder.productQuantityLayout))
            }
        }

    private fun getPrice(holder: CreateOrderHolder): Float {
        val quantity = holder.productQuantity.text.toString().toInt()
        val productPriceInt = holder.productPrice.text.toString().toFloat()
        return quantity * productPriceInt
    }

    private fun onQuantityChanged(isQuantityCorrect: Boolean, holder: CreateOrderHolder) {
        onCheckChangedAction(holder.productName.text.toString(), isQuantityCorrect)
    }

    private fun updateQuantityForProduct(productName: String, quantity: Int) {
        quantities[productName] = quantity
    }

    internal fun setProductsInOrder(productsInOrder: List<OrderContent>?) {
        if (productsInOrder != null) {
            this.productsInOrder = productsInOrder
        }
        notifyDataSetChanged()
    }

    internal fun setProducts(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }
}


