package com.example.android.storemanagement.store_tab

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android.storemanagement.R
import com.example.android.storemanagement.create_order.CreateOrderFieldValidator.isQuantityCorrect
import com.example.android.storemanagement.create_order.CreateOrderHolder
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.products_database.Product
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class StoreAdapter(
    private val context: Context,
    private val setStoreButtonEnabled: (Boolean) -> Unit,
) :
    RecyclerView.Adapter<CreateOrderHolder>() {

    private var products = mutableListOf<Product>() // Cached copy of products
    private var firebaseProducts = mutableListOf<FirebaseProduct>() // Cached copy of products
    private var areFirebaseProductsLoaded = false

    //barcode -> quantity
    var quantities = mutableMapOf<String, Int>()
    var enabledProducts = mutableMapOf<String, Boolean>()

    // Gets the number of items in the list
    override fun getItemCount(): Int = if (areFirebaseProductsLoaded) firebaseProducts.size else
        products.size

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateOrderHolder =
        CreateOrderHolder(
            LayoutInflater.from(context).inflate(R.layout.create_order_item, parent, false)
        )

    // Binds each product in the list to a view
    override fun onBindViewHolder(holder: CreateOrderHolder, position: Int) {
        if (areFirebaseProductsLoaded) {
            val user: FirebaseUser? = Firebase.auth.currentUser
            val currentProduct = firebaseProducts[position]
            var finalProductAvailableQuantity = currentProduct.quantity.toInt()
            val orderContents = mutableListOf<FirebaseOrderContent>()
            setButtonEnabled(holder, currentProduct.barcode, currentProduct.quantity.toInt(), finalProductAvailableQuantity)
            FirebaseDatabaseOperations.getFirebaseUserOrderContents(
                user?.uid!!,
                currentProduct.barcode
            ) { orderContent, childAction ->
                when (childAction) {
                    FirebaseDatabaseOperations.ChildAction.ChildAdded -> {
                        if (orderContents.none { it.id == orderContent.id }) {
                            orderContents.add(orderContent)
                            val orderContentQuantity = orderContent.quantity
                            finalProductAvailableQuantity -= orderContentQuantity.toInt()
                        }

                        setButtonEnabled(holder, currentProduct.barcode, currentProduct.quantity.toInt(), finalProductAvailableQuantity)
                    }
                    FirebaseDatabaseOperations.ChildAction.ChildChanged -> {
                        val changedOrderContent =
                            orderContents.first { it.id == orderContent.id }
                        orderContents.remove(changedOrderContent)
                        finalProductAvailableQuantity += changedOrderContent.quantity.toInt()
                        orderContents.add(orderContent)

                        val orderContentQuantity = orderContent.quantity
                        finalProductAvailableQuantity -= orderContentQuantity.toInt()

                        setButtonEnabled(holder, currentProduct.barcode, currentProduct.quantity.toInt(), finalProductAvailableQuantity)
                    }
                    FirebaseDatabaseOperations.ChildAction.ChildRemoved -> {
                        val removedOrderContent =
                            orderContents.first { it.id == orderContent.id }
                        orderContents.remove(removedOrderContent)

                        val orderContentQuantity = orderContent.quantity
                        finalProductAvailableQuantity += orderContentQuantity.toInt()

                        setButtonEnabled(holder, currentProduct.barcode, currentProduct.quantity.toInt(), finalProductAvailableQuantity)
                    }
                }
            }
            holder.productName.text = currentProduct.name
            val overcharge = if (currentProduct.overcharge.isBlank()) 0F else currentProduct.overcharge.toFloat()
            holder.productPrice.text = (currentProduct.price.toFloat() + overcharge).toString()
            holder.productQuantity.addTextChangedListener(
                getTextWatcher(
                    holder,
                    currentProduct.barcode,
                    currentProduct.quantity.toInt(),
                    finalProductAvailableQuantity
                )
            )
        } else {
            val currentProduct = products[position]
            holder.productName.text = currentProduct.name
            holder.productPrice.text = (currentProduct.price + currentProduct.overcharge).toString()
            holder.productQuantity.setText(currentProduct.quantity.toString())
            holder.productQuantity.addTextChangedListener(getTextWatcher(holder, currentProduct.barcode, currentProduct.quantity, currentProduct.quantity))
        }
    }

    private fun setButtonEnabled(
        holder: CreateOrderHolder,
        barcode: String,
        uncalculatedProductQuantity: Int,
        finalProductAvailableQuantity: Int
    ) {
        Log.d("setButtonEnabled",
            "uncalculated $uncalculatedProductQuantity finalProductAvailableQuantity $finalProductAvailableQuantity"
        )
        holder.inStockProductQuantity.text =
            context.resources.getString(R.string.in_stock_quantity).plus(" ").plus(finalProductAvailableQuantity)
        holder.productQuantity.addTextChangedListener(getTextWatcher(holder, barcode, uncalculatedProductQuantity, finalProductAvailableQuantity))

        setErroneousField(holder, barcode, finalProductAvailableQuantity)
        setStoreButtonEnabled(
            isQuantityCorrect(
                holder.productQuantity,
                holder.productQuantityLayout,
                finalProductAvailableQuantity
            ) && enabledProducts.none { !it.value }
        )
    }

    private fun setErroneousField(
        holder: CreateOrderHolder,
        barcode: String,
        finalProductAvailableQuantity: Int
    ) {
        when {
            isQuantityCorrect(
                holder.productQuantity,
                holder.productQuantityLayout,
                finalProductAvailableQuantity
            ) -> {
                enabledProducts[barcode] = true
            }
            holder.productQuantityLayout.isErrorEnabled -> {
                enabledProducts[barcode] = false
            }
            holder.productQuantity.text.isEmpty() -> {
                enabledProducts[barcode] = true
            }
        }
    }

    private fun getTextWatcher(holder: CreateOrderHolder, barcode: String, uncalculatedProductQuantity: Int, finalProductAvailableQuantity: Int): TextWatcher =
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                setErroneousField(holder, barcode, finalProductAvailableQuantity)
                val shouldEnableOrderButton = isQuantityCorrect(
                    holder.productQuantity,
                    holder.productQuantityLayout,
                    finalProductAvailableQuantity
                ) && enabledProducts.none { !it.value }
                setStoreButtonEnabled(shouldEnableOrderButton)
                enabledProducts[barcode] = shouldEnableOrderButton

                if (shouldEnableOrderButton) {
                    Log.d("setButtonEnabled shouldEnableOrderButton",
                        "uncalculated $uncalculatedProductQuantity finalProductAvailableQuantity $finalProductAvailableQuantity"
                    )
                    updateQuantityForProduct(
                        barcode,
                        calculatedProductQuantity(uncalculatedProductQuantity, finalProductAvailableQuantity, holder.productQuantity.text.toString().toInt()).toString()
                    )
                    setStoreButtonEnabled(shouldEnableOrderButton)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

    private fun calculatedProductQuantity(
        uncalculatedProductQuantity: Int,
        availableProductAvailableQuantity: Int,
        enteredProductQuantity: Int
    ): Int {
        return if (areFirebaseProductsLoaded) {
            uncalculatedProductQuantity - (availableProductAvailableQuantity - enteredProductQuantity)
        } else {
            enteredProductQuantity
        }
    }

    private fun updateQuantityForProduct(barcode: String, quantity: String) {
        when (quantity) {
            "" -> quantities[barcode] = 0
            else -> quantities[barcode] = quantity.toInt()
        }
    }

    internal fun setProducts(products: List<Product>?, fbProducts: List<FirebaseProduct>?) {
        if (products != null) {
            this.products.clear()
            this.products.addAll(products)
            areFirebaseProductsLoaded = false
        } else if (fbProducts != null) {
            this.firebaseProducts.clear()
            this.firebaseProducts.addAll(fbProducts)
            areFirebaseProductsLoaded = true
        }

        notifyDataSetChanged()
    }
}