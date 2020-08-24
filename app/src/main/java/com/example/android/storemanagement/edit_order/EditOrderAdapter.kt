package com.example.android.storemanagement.edit_order

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.android.storemanagement.R
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator
import com.example.android.storemanagement.create_order.CreateOrderHolder
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.products_database.Product

class EditOrderAdapter(
    private val context: Context,
    private val updateFinalPriceAction: (Float) -> Unit,
    private val setOrderButtonEnabled: (Boolean) -> Unit
) : RecyclerView.Adapter<CreateOrderHolder>() {

    private var productsInOrder = emptyList<OrderContent>() // Cached copy of products
    private var products = emptyList<Product>()

    //productName -> quantity
    var quantities = mutableMapOf<String, Int>()

    var enabledProducts = mutableMapOf<String, Boolean>()
    var beforeElementIndicator: Boolean = false
    var afterElementIndicator: Boolean = false

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
        holder.productQuantity.addTextChangedListener(getTextWatcher(holder))

    }

    private fun getTextWatcher(holder: CreateOrderHolder): TextWatcher =
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val quantity = holder.productQuantity.text.toString()

                val shouldOrderButtonBeEnabled =
                    CreateOrderFieldValidator.isQuantityCorrect(holder.productQuantity) && enabledProducts.isNotEmpty()
                setOrderButtonEnabled(shouldOrderButtonBeEnabled)

                if (CreateOrderFieldValidator.isQuantityCorrect(holder.productQuantity) && !afterElementIndicator) {
                    onQuantityChanged(true, holder)
                    setOrderButtonEnabled(shouldOrderButtonBeEnabled)
                    updateFinalPriceAction(getPrice(holder))
                    beforeElementIndicator = false
                    afterElementIndicator = true
                    updateQuantityForProduct(holder.productName.text.toString(), quantity.toInt())
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                setOrderButtonEnabled(true)
                if (CreateOrderFieldValidator.isQuantityCorrect(holder.productQuantity) && !beforeElementIndicator){
                    beforeElementIndicator = true
                    afterElementIndicator = false
                    updateFinalPriceAction(getPrice(holder) * -1)
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
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


