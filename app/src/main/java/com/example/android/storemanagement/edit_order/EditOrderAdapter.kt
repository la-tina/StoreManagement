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
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator.isQuantityCorrectForOrder
import com.example.android.storemanagement.create_order.CreateOrderHolder
import com.example.android.storemanagement.firebase.ChildAction
import com.example.android.storemanagement.firebase.FirebaseDatabaseOrderContentsOperations.getFirebaseUserOrderContents
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.products_database.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class EditOrderAdapter(
    private val context: Context,
    private val updateFinalPriceAction: (Float) -> Unit,
    private val setOrderButtonEnabled: (Boolean) -> Unit,
    private val canOrderBeEdited: () -> Boolean
) : RecyclerView.Adapter<CreateOrderHolder>() {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var productsInOrder = emptyList<OrderContent>() // Cached copy of products
    private var products = emptyList<Product>()
    private var firebaseOrderContents = emptyList<FirebaseOrderContent>()
    private var firebaseUserProducts = emptyList<FirebaseProduct>()
    private var areFirebaseOrderContentsLoaded = false

    var enabledProductsWithErrors = mutableMapOf<String, Boolean>()

    //barcode -> quantity
    var quantities = mutableMapOf<String, Int>()
    var shouldEnableEditButton = false

    private fun onFieldErrorChangedAction(productName: String, isQuantityCorrect: Boolean) {
        enabledProductsWithErrors[productName] = isQuantityCorrect
    }

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
//        if (areFirebaseOrderContentsLoaded) {
        val currentFirebaseProductInOrder = firebaseOrderContents[position]
        holder.productName.text = currentFirebaseProductInOrder.productName
        holder.productPrice.text = currentFirebaseProductInOrder.productPrice
        holder.productQuantity.setText(currentFirebaseProductInOrder.quantity)
        var finalProductAvailableQuantity = 0
        val orderContents = mutableListOf<FirebaseOrderContent>()

        firebaseUserProducts.forEach { product ->
            if (product.barcode == currentFirebaseProductInOrder.productBarcode) {
                finalProductAvailableQuantity = product.quantity.toInt() + holder.productQuantity.text.toString().toInt()
                getFirebaseUserOrderContents(
                    currentFirebaseProductInOrder.userId,
                    currentFirebaseProductInOrder.productBarcode
                ) { orderContent, childAction ->
                    when (childAction) {
                        ChildAction.ChildAdded -> {
                            if (orderContents.none {it.id == orderContent.id}) {
                                orderContents.add(orderContent)
                                val orderContentQuantity = orderContent.quantity
                                finalProductAvailableQuantity -= orderContentQuantity.toInt()

                            }
                            onInStockQuantityCalculated(holder, finalProductAvailableQuantity, currentFirebaseProductInOrder)
                            Log.d("Watcher", "quantity addWatcher " + holder.productQuantity.text)
                        }
                        ChildAction.ChildChanged -> {
                            val changedOrderContent =
                                orderContents.first { it.id == orderContent.id }
                            orderContents.remove(changedOrderContent)
                            finalProductAvailableQuantity += changedOrderContent.quantity.toInt()
                            orderContents.add(orderContent)

                            val orderContentQuantity = orderContent.quantity
                            finalProductAvailableQuantity -= orderContentQuantity.toInt()

                            onInStockQuantityCalculated(holder, finalProductAvailableQuantity, currentFirebaseProductInOrder)
                            Log.d("Watcher", "quantity addWatcher " + holder.productQuantity.text)
                        }
                        ChildAction.ChildRemoved -> {
                            val removedOrderContent =
                                orderContents.first { it.id == orderContent.id }
                            orderContents.remove(removedOrderContent)

                            val orderContentQuantity = orderContent.quantity
                            finalProductAvailableQuantity += orderContentQuantity.toInt()

                            onInStockQuantityCalculated(holder, finalProductAvailableQuantity, currentFirebaseProductInOrder)
                            Log.d("Watcher", "quantity addWatcher " + holder.productQuantity.text)
                        }
                    }
                }
            }
        }
    }

    private fun onInStockQuantityCalculated(
        holder: CreateOrderHolder,
        finalProductAvailableQuantity: Int,
        currentFirebaseProductInOrder: FirebaseOrderContent
    ) {
        holder.inStockProductQuantity.text =
            context.resources.getString(R.string.in_stock_quantity).plus(" ")
                .plus(finalProductAvailableQuantity)
        if (!canOrderBeEdited()) {
            holder.productQuantity.isEnabled = false
        }
        holder.productQuantity.addTextChangedListener(
            getTextWatcher(
                holder,
                currentFirebaseProductInOrder.productBarcode,
                finalProductAvailableQuantity
            )
        )
        setErroneousField(holder, finalProductAvailableQuantity)
        val shouldEnableOrderButton = isEditOrderQuantityCorrect(
            holder.productQuantity,
            holder.productQuantityLayout,
            finalProductAvailableQuantity
        ) && enabledProductsWithErrors.none { !it.value }
        setOrderButtonEnabled(shouldEnableOrderButton)
    }

    private fun getTextWatcher(holder: CreateOrderHolder, barcode: String, finalProductAvailableQuantity: Int): TextWatcher =
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                setErroneousField(holder, finalProductAvailableQuantity)

                shouldEnableEditButton = isEditOrderQuantityCorrect(
                    holder.productQuantity,
                    holder.productQuantityLayout,
                    finalProductAvailableQuantity) && enabledProductsWithErrors.none { !it.value }
                setOrderButtonEnabled(shouldEnableEditButton)

                if (isQuantityCorrectForOrder(
                        holder.productQuantity,
                        holder.productQuantityLayout,
                        finalProductAvailableQuantity)) {
                    val quantity: Int =
                        if (holder.productQuantity.text.toString().isBlank()) 0
                        else holder.productQuantity.text.toString().toInt()
                    updateQuantityForProduct(barcode, quantity)
                    updateFinalPriceAction(getFinalPrice())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                setErroneousField(holder, finalProductAvailableQuantity)
                val quantity: Int = if (holder.productQuantity.text.toString()
                        .isBlank()
                ) 0 else holder.productQuantity.text.toString().toInt()
                updateQuantityForProduct(barcode, quantity)
                updateFinalPriceAction(getFinalPrice())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setOrderButtonEnabled(
                    isQuantityCorrectForOrder(
                        holder.productQuantity,
                        holder.productQuantityLayout,
                        finalProductAvailableQuantity
                    )
                )
                val quantity: Int = if (holder.productQuantity.text.toString()
                        .isBlank()
                ) 0 else holder.productQuantity.text.toString().toInt()
                updateQuantityForProduct(barcode, quantity)
                updateFinalPriceAction(getFinalPrice())
            }
        }

    private fun setErroneousField(
        holder: CreateOrderHolder,
        finalProductAvailableQuantity: Int
    ) {
        when {
            isQuantityCorrectForOrder(
                holder.productQuantity,
                holder.productQuantityLayout,
                finalProductAvailableQuantity
            ) -> {
                onFieldErrorChangedAction(holder.productName.text.toString(), true)
            }
            holder.productQuantityLayout.isErrorEnabled -> {
                onFieldErrorChangedAction(holder.productName.text.toString(), false)
            }
            holder.productQuantity.text.isEmpty() -> {
                onFieldErrorChangedAction(holder.productName.text.toString(), true)
            }
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


