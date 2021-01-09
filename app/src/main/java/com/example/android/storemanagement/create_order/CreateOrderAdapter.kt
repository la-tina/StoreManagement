package com.example.android.storemanagement.create_order

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android.storemanagement.R
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator.isQuantityAboveLimit
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator.isQuantityCorrect
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.firebase.FirebaseUserInternal
import kotlinx.android.synthetic.main.create_order_item.view.*
import kotlinx.android.synthetic.main.fragment_create_order.view.*


class CreateOrderAdapter(
    private val context: Context,
    private val updateFinalPriceAction: (Float) -> Unit,
    private val setOrderButtonEnabled: (Boolean) -> Unit,
    private val firebaseUser: FirebaseUserInternal
) : RecyclerView.Adapter<CreateOrderHolder>() {

    private var firebaseProducts = mutableListOf<FirebaseProduct>() // Cached copy of products

    //productName -> quantity
    var quantities = mutableMapOf<String, Int>()

    var enabledProducts = mutableMapOf<String, Boolean>()

    // Gets the number of items in the list
    override fun getItemCount(): Int {
        return firebaseProducts.size
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
        val currentProduct = firebaseProducts[position]

        holder.productName.text = currentProduct.name
        val overcharge = if (currentProduct.overcharge.isBlank()) 0F else currentProduct.overcharge.toFloat()
        holder.productPrice.text = (currentProduct.price.toFloat() + overcharge).toString()
        holder.inStockProductQuantity.visibility = View.VISIBLE
        holder.inStockProductQuantity.text = context.resources.getString(R.string.in_stock_quantity).plus(" ").plus(currentProduct.quantity)
        holder.productQuantity.addTextChangedListener(getTextWatcher(holder))

        setOrderButtonEnabled(
            isQuantityCorrect(
                holder.productQuantity,
                holder.productQuantityLayout
            )
        )
//            onQuantityChanged(true, holder)

    }

    private fun getTextWatcher(holder: CreateOrderHolder): TextWatcher =
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                holder.productQuantity.text?.toString()?.let { quantity ->
//                    holder.productQuantityLayout.error = null
//                    holder.productQuantityLayout.isErrorEnabled = false

                    if (isQuantityCorrect(holder.productQuantity, holder.productQuantityLayout))
                        onQuantityChanged(true, holder)

                    val shouldEnableOrderButton =
                        isQuantityCorrect(
                            holder.productQuantity,
                            holder.productQuantityLayout
                        ) && enabledProducts.isNotEmpty()

                    if (isQuantityCorrect(holder.productQuantity, holder.productQuantityLayout)) {

                        onQuantityChanged(shouldEnableOrderButton, holder)
                        setOrderButtonEnabled(shouldEnableOrderButton)

                        updateQuantityForProduct(
                            holder.productName.text.toString(),
                            quantity.toInt()
                        )
                        updateFinalPriceAction(getPrice())
                        Log.d("TinaOrder", "afterTextChanged quantity " + quantity.toInt())
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (isQuantityCorrect(holder.productQuantity, holder.productQuantityLayout)) {
                    holder.productQuantity.text?.toString()?.let { quantity ->
                        updateQuantityForProduct(
                            holder.productName.text.toString(),
                            quantity.toInt()
                        )
                        updateFinalPriceAction(getPrice())
                        Log.d("TinaOrder", "beforeTextChanged quantity " + quantity.toInt())
                    }
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                holder.productQuantity.text?.toString()?.let { quantity ->
                    val quantityEdited: Int = if (quantity.isEmpty()) 0 else quantity.toInt()
                    updateQuantityForProduct(holder.productName.text.toString(), quantityEdited)
                    updateFinalPriceAction(getPrice())
                    Log.d("Tina", "quantity onTextChanged$quantityEdited")
                }
                setOrderButtonEnabled(
                    isQuantityCorrect(
                        holder.productQuantity,
                        holder.productQuantityLayout
                    )
                )
            }
        }

    private fun getPrice(): Float {
        var finalPrice = 0F
        firebaseProducts.forEach { product ->
            if (!quantities.isNullOrEmpty() && quantities[product.name] != null) {
                if (!isQuantityAboveLimit(quantities[product.name]!!)) {
                    val overcharge = if (product.overcharge.isEmpty()) 0F else product.overcharge.toFloat()
                    finalPrice += quantities[product.name]!! * (product.price.toFloat() + overcharge)
                }
            }
        }
        return finalPrice
    }

    private fun onQuantityChanged(isQuantityCorrect: Boolean, holder: CreateOrderHolder) {
        onCheckChangedAction(holder.productName.text.toString(), isQuantityCorrect)
    }

    private fun updateQuantityForProduct(productName: String, quantity: Int) {
        quantities[productName] = quantity
    }

    internal fun setProducts(fbProducts: List<FirebaseProduct>) {
        this.firebaseProducts.clear()
        this.firebaseProducts.addAll(fbProducts)

        notifyDataSetChanged()
    }
}

class CreateOrderHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the ProductTextView that will add each product to
    val productName = view.product_item_text!!
    val productPrice = view.order_item_price!!
    val productQuantityLayout = view.order_item_quantity_layout!!
    val productQuantity = view.order_item_quantity!!
    val inStockProductQuantity = view.product_item_quantity
}