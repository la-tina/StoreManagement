package com.example.android.storemanagement.edit_order

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android.storemanagement.R
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator.isEditOrderQuantityCorrect
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator.isQuantityCorrect
import com.example.android.storemanagement.create_order.CreateOrderHolder
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.products_database.Product

class EditOrderAdapter(
    private val context: Context,
    private val updateFinalPriceAction: (Float) -> Unit,
    private val setOrderButtonEnabled: (Boolean) -> Unit,
    private val canOrderBeEdited: () -> Boolean
) : RecyclerView.Adapter<CreateOrderHolder>() {

    private var productsInOrder = emptyList<OrderContent>() // Cached copy of products
    private var products = emptyList<Product>()
    private var firebaseOrderContents = emptyList<FirebaseOrderContent>()
    private var firebaseUserProducts = emptyList<FirebaseProduct>()
    private var areFirebaseOrderContentsLoaded = false

    //barcode -> quantity
    var quantities = mutableMapOf<String, Int>()

    // Gets the number of items in the list
    override fun getItemCount(): Int {
        return if (areFirebaseOrderContentsLoaded) firebaseOrderContents.size else productsInOrder.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateOrderHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.create_order_item, parent, false)
        return CreateOrderHolder(view)
    }

    // Binds each product in the list to a view
    override fun onBindViewHolder(holder: CreateOrderHolder, position: Int) {
        if (areFirebaseOrderContentsLoaded) {
            val currentFirebaseProductInOrder = firebaseOrderContents[position]
            holder.productName.text = currentFirebaseProductInOrder.productName
            holder.productPrice.text = currentFirebaseProductInOrder.productPrice
            holder.productQuantity.setText(currentFirebaseProductInOrder.quantity)
            firebaseUserProducts.forEach { product ->
                if (product.barcode == currentFirebaseProductInOrder.productBarcode) {
                    holder.inStockProductQuantity.text =
                        context.resources.getString(R.string.in_stock_quantity).plus(" ").plus(product.quantity)
                }
            }

            if (!canOrderBeEdited()) {
                holder.productQuantity.isEnabled = false
            }
            holder.productQuantity.addTextChangedListener(
                getTextWatcher(
                    holder,
                    currentFirebaseProductInOrder.productBarcode
                )
            )
            Log.d("Watcher", "quantity addWatcher " + holder.productQuantity.text)
        } else {
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
            holder.productQuantity.addTextChangedListener(
                getTextWatcher(
                    holder,
                    currentProductInOrder.productBarcode
                )
            )
            Log.d("Watcher", "quantity addWatcher " + holder.productQuantity.text)
        }
    }

    private fun getTextWatcher(holder: CreateOrderHolder, barcode: String): TextWatcher =
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val shouldEnableOrderButton = isEditOrderQuantityCorrect(
                    holder.productQuantity,
                    holder.productQuantityLayout
                )
                setOrderButtonEnabled(shouldEnableOrderButton)

                if (isQuantityCorrect(
                        holder.productQuantity,
                        holder.productQuantityLayout
                    )
                ) {
//                    Log.d(
//                        "Watcher",
//                        "quantity " + quantities[barcode] + " current quantity " + holder.productQuantity.text.toString()
//                            .toInt() + " productname " + holder.productName.text.toString() + " lastEditedProduct " + lastEditedProduct
//                    )
//                    if (quantities[barcode] == holder.productQuantity.text.toString()
//                            .toInt() && barcode == lastEditedProduct && !beforeElementIndicator
//                    ) {
//                        Log.d("Watcher", "afterElementIndicator")
//                        afterElementIndicator = true
//                    } else {
//                        beforeElementIndicator = false
//                        lastEditedProduct = barcode
//                        afterElementIndicator = false

                    val quantity: Int = if (holder.productQuantity.text.toString()
                            .isBlank()
                    ) 0 else holder.productQuantity.text.toString().toInt()
                    updateQuantityForProduct(barcode, quantity)
                    updateFinalPriceAction(getFinalPrice())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                if (isQuantityCorrect(
//                        holder.productQuantity,
//                        holder.productQuantityLayout
//                    ) && !beforeElementIndicator
//                ) {
//                    beforeElementIndicator = true
//                    afterElementIndicator = false
//                    updateFinalPriceAction(getPrice(holder) * -1)
//                    Log.d(
//                        "Watcher",
//                        "watcher price before " + getPrice(holder) * -1 + " for product " + holder.productName.text.toString()
//                    )
//                }
                val quantity: Int = if (holder.productQuantity.text.toString()
                        .isBlank()
                ) 0 else holder.productQuantity.text.toString().toInt()
                updateQuantityForProduct(barcode, quantity)
                updateFinalPriceAction(getFinalPrice())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setOrderButtonEnabled(
                    isQuantityCorrect(
                        holder.productQuantity,
                        holder.productQuantityLayout
                    )
                )
                val quantity: Int = if (holder.productQuantity.text.toString()
                        .isBlank()
                ) 0 else holder.productQuantity.text.toString().toInt()
                updateQuantityForProduct(barcode, quantity)
                updateFinalPriceAction(getFinalPrice())
            }
        }

    private fun getFinalPrice(): Float {
        var finalPrice = 0F
        firebaseOrderContents.forEach { content ->
            if (!quantities.isNullOrEmpty() && quantities[content.productBarcode] != null) {
                if (!CreateOrderFieldValidator.isQuantityAboveLimit(quantities[content.productBarcode]!!)) {
                    finalPrice += quantities[content.productBarcode]!! * content.productPrice.toFloat()
                }
            } else {
                finalPrice += content.quantity.toInt() * content.productPrice.toFloat()
            }
        }
        return finalPrice
    }

    private fun updateQuantityForProduct(barcode: String, quantity: Int) {
        quantities[barcode] = quantity
    }

    internal fun setProductsInOrder(
        productsInOrder: List<OrderContent>?,
        firebaseProductsInOrder: List<FirebaseOrderContent>?,
        currentFirebaseUserProducts: List<FirebaseProduct>
    ) {
        if (productsInOrder != null) {
            this.productsInOrder = productsInOrder
            areFirebaseOrderContentsLoaded = false
        } else if (firebaseProductsInOrder != null) {
            this.firebaseOrderContents = firebaseProductsInOrder
            this.firebaseUserProducts = currentFirebaseUserProducts
            areFirebaseOrderContentsLoaded = true
        }
        notifyDataSetChanged()
    }

    internal fun setProducts(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }
}


