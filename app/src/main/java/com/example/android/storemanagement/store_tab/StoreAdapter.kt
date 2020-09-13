package com.example.android.storemanagement.store_tab

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.R
import com.example.android.storemanagement.products_database.Product
import kotlinx.android.synthetic.main.store_item.view.*
import kotlin.reflect.KFunction1


class StoreAdapter(
    private val context: Context,
    private val setOrderButtonEnabled: (Boolean) -> Unit,
    private val getProductQuantity: KFunction1<@ParameterName(name = "product") Product, Int>
) :
    RecyclerView.Adapter<StoreProductsHolder>() {

    companion object {
        const val MESSAGE_QUANTITY_ABOVE_MAX_SIZE = "Тhe maximum allowed quantity is 500лв."
    }

    private var products = emptyList<Product>() // Cached copy of products

    //productName -> quantity
    var quantities = mutableMapOf<String, Int>()

    // Gets the number of items in the list
    override fun getItemCount(): Int =
        products.size

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreProductsHolder =
        StoreProductsHolder(
            LayoutInflater.from(context).inflate(R.layout.store_item, parent, false)
        )

    // Binds each product in the list to a view
    override fun onBindViewHolder(holder: StoreProductsHolder, position: Int) {
        val currentProduct = products[position]

        holder.productName.text = currentProduct.name
        holder.productPrice.text = currentProduct.price.toString()
        holder.productQuantity.setText(currentProduct.quantity.toString())
        holder.productQuantity.addTextChangedListener(getTextWatcher(holder))
    }

    private fun getTextWatcher(holder: StoreProductsHolder): TextWatcher =
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                if (holder.productQuantity.text.toString().isNotEmpty()
                    && holder.productQuantity.text.toString().toInt() > 500
                ) {
                    holder.productQuantity.error = MESSAGE_QUANTITY_ABOVE_MAX_SIZE

                    val hasEnabled = false
                    setOrderButtonEnabled(hasEnabled)
                } else {
                    updateQuantityForProduct(
                        holder.productName.text.toString(),
                        holder.productQuantity.text.toString()
                    )
                    val hasEnabled = true
                    setOrderButtonEnabled(hasEnabled)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        }

    private fun updateQuantityForProduct(productName: String, quantity: String) {
        when (quantity) {
            "" -> quantities[productName] = 0
            else -> quantities[productName] = quantity.toInt()
        }
    }

    internal fun setProducts(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }
}

class StoreProductsHolder(view: View) : RecyclerView.ViewHolder(view) {
    val productName = view.store_item_text!!
    val productPrice = view.store_item_price!!
    val productQuantity = view.store_item_quantity!!
}