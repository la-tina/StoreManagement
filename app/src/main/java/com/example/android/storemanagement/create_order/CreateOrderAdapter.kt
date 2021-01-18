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
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.firebase.FirebaseUserInternal
import kotlinx.android.synthetic.main.create_order_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


class CreateOrderAdapter(
    private val context: Context,
    private val updateFinalPriceAction: (Float) -> Unit,
    private val setOrderButtonEnabled: (Boolean) -> Unit,
    private val firebaseUser: FirebaseUserInternal
) : RecyclerView.Adapter<CreateOrderHolder>() {

    private var firebaseProducts = mutableListOf<FirebaseProduct>() // Cached copy of products

    //barcode -> quantity
    var quantities = mutableMapOf<String, Int>()
    var enabledProducts = mutableMapOf<String, Boolean>()
    var shouldEnableOrderButton = false

    // Gets the number of items in the list
    override fun getItemCount(): Int {
        return firebaseProducts.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateOrderHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.create_order_item, parent, false)
        return CreateOrderHolder(view)
    }

    private fun onFieldErrorChangedAction(productBarcode: String, isQuantityCorrect: Boolean) {
        enabledProducts[productBarcode] = isQuantityCorrect
    }

    // Binds each product in the list to a view
    override fun onBindViewHolder(holder: CreateOrderHolder, position: Int) {
        val currentProduct = firebaseProducts[position]

        holder.productName.text = currentProduct.name
        val overcharge = if (currentProduct.overcharge.isBlank()) 0F else currentProduct.overcharge.toFloat()
        holder.productPrice.text = (currentProduct.price.toFloat() + overcharge).toString()
        holder.inStockProductQuantity.visibility = View.VISIBLE
//        firebaseUserProducts.forEach { product ->
//            if (currentProduct.barcode == currentFirebaseProductInOrder.productBarcode) {
        var finalProductAvailableQuantity = currentProduct.quantity.toInt()
        val orderContents = mutableListOf<FirebaseOrderContent>()
        onInStockQuantityCalculated(currentProduct.barcode, holder, finalProductAvailableQuantity)
        FirebaseDatabaseOperations.getFirebaseUserOrderContents(
            firebaseUser.id,
            currentProduct.barcode
        ) { orderContent, childAction ->
            when (childAction) {
                FirebaseDatabaseOperations.ChildAction.ChildAdded -> {
                    if (orderContents.none { it.id == orderContent.id }) {
                        orderContents.add(orderContent)
                        val orderContentQuantity = orderContent.quantity
                        finalProductAvailableQuantity -= orderContentQuantity.toInt()
                    }

                    onInStockQuantityCalculated(currentProduct.barcode, holder, finalProductAvailableQuantity)
                }
                FirebaseDatabaseOperations.ChildAction.ChildChanged -> {
                    val changedOrderContent =
                        orderContents.first { it.id == orderContent.id }
                    orderContents.remove(changedOrderContent)
                    finalProductAvailableQuantity += changedOrderContent.quantity.toInt()
                    orderContents.add(orderContent)

                    val orderContentQuantity = orderContent.quantity
                    finalProductAvailableQuantity -= orderContentQuantity.toInt()

                    onInStockQuantityCalculated(currentProduct.barcode, holder, finalProductAvailableQuantity)
                }
                FirebaseDatabaseOperations.ChildAction.ChildRemoved -> {
                    val removedOrderContent =
                        orderContents.first { it.id == orderContent.id }
                    orderContents.remove(removedOrderContent)

                    val orderContentQuantity = orderContent.quantity
                    finalProductAvailableQuantity += orderContentQuantity.toInt()

                    onInStockQuantityCalculated(currentProduct.barcode, holder, finalProductAvailableQuantity)
                }
            }
        }
    }

    private fun onInStockQuantityCalculated(
        barcode: String,
        holder: CreateOrderHolder,
        finalProductAvailableQuantity: Int
    ) {
        holder.inStockProductQuantity.text =
            context.resources.getString(R.string.in_stock_quantity).plus(" ").plus(finalProductAvailableQuantity)
        holder.productQuantity.addTextChangedListener(getTextWatcher(barcode, holder, finalProductAvailableQuantity))

        setErroneousField(barcode, holder, finalProductAvailableQuantity)
        setOrderButtonEnabled(
            isQuantityCorrect(
                holder.productQuantity,
                holder.productQuantityLayout,
                finalProductAvailableQuantity
            ) && enabledProducts.none { !it.value }
        )
    }

    private fun getTextWatcher(barcode: String, holder: CreateOrderHolder, finalProductAvailableQuantity: Int): TextWatcher =
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                holder.productQuantity.text?.toString()?.let { quantity ->
                    setErroneousField(barcode, holder, finalProductAvailableQuantity)

                    shouldEnableOrderButton = isQuantityCorrect(
                        holder.productQuantity,
                        holder.productQuantityLayout,
                        finalProductAvailableQuantity
                    ) && enabledProducts.none { !it.value }
                    setOrderButtonEnabled(shouldEnableOrderButton)

                    if (isQuantityCorrect(holder.productQuantity, holder.productQuantityLayout, finalProductAvailableQuantity)) {
                        onQuantityChanged(barcode, shouldEnableOrderButton)
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
                setErroneousField(barcode, holder, finalProductAvailableQuantity)

//                val shouldEnableOrderButton = isQuantityCorrect(
//                    holder.productQuantity,
//                    holder.productQuantityLayout,
//                    finalProductAvailableQuantity
//                ) && enabledProducts.none { !it.value }
//                setOrderButtonEnabled(shouldEnableOrderButton)

                if (isQuantityCorrect(holder.productQuantity, holder.productQuantityLayout, finalProductAvailableQuantity)) {
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
                setErroneousField(barcode, holder, finalProductAvailableQuantity)

//                val shouldEnableOrderButton = isQuantityCorrect(
//                    holder.productQuantity,
//                    holder.productQuantityLayout,
//                    finalProductAvailableQuantity
//                ) && enabledProducts.none { !it.value }
//                setOrderButtonEnabled(shouldEnableOrderButton)

                holder.productQuantity.text?.toString()?.let { quantity ->
                    val quantityEdited: Int = if (quantity.isEmpty()) 0 else quantity.toInt()
                    updateQuantityForProduct(holder.productName.text.toString(), quantityEdited)
                    updateFinalPriceAction(getPrice())
                    Log.d("Tina", "quantity onTextChanged$quantityEdited")
                }
            }
        }

    private fun setErroneousField(
        barcode: String,
        holder: CreateOrderHolder,
        finalProductAvailableQuantity: Int
    ) {
        when {
            isQuantityCorrect(
                holder.productQuantity,
                holder.productQuantityLayout,
                finalProductAvailableQuantity
            ) -> {
                onQuantityChanged(barcode, true)
            }
            holder.productQuantityLayout.isErrorEnabled -> {
                onQuantityChanged(barcode, false)
            }
            holder.productQuantity.text.isEmpty() -> {
                onQuantityChanged(barcode, true)
            }
        }
    }

    private fun getPrice(): Float {
        var finalPrice = 0F
        firebaseProducts.forEach { product ->
            if (!quantities.isNullOrEmpty() && quantities[product.barcode] != null) {
                if (!isQuantityAboveLimit(quantities[product.barcode]!!)) {
                    val overcharge = if (product.overcharge.isEmpty()) 0F else product.overcharge.toFloat()
                    finalPrice += quantities[product.barcode]!! * (product.price.toFloat() + overcharge)
                }
            }
        }
        return finalPrice
    }

    private fun onQuantityChanged(barcode: String, isQuantityCorrect: Boolean) {
        onFieldErrorChangedAction(barcode, isQuantityCorrect)
    }

    private fun updateQuantityForProduct(productBarcode: String, quantity: Int) {
        quantities[productBarcode] = quantity
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