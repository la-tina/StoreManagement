package com.example.android.storemanagement.products_tab.pending_products_tab

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseOrder
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.orders_tab.OrderStatus
import kotlinx.android.synthetic.main.pending_product_item.view.*
import kotlinx.android.synthetic.main.product_item.view.product_item_price
import kotlinx.android.synthetic.main.product_item.view.product_item_quantity
import kotlinx.android.synthetic.main.product_item.view.product_item_text

class PendingProductsAdapter(private val context: Context) :
    RecyclerView.Adapter<PendingProductsViewHolder>() {

    private var pendingOrderContents: MutableList<FirebaseOrderContent> = mutableListOf()
    private var notDeliveredOrders: MutableList<FirebaseOrder> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingProductsViewHolder =
        PendingProductsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.pending_product_item, parent, false)
        )

    override fun onBindViewHolder(holder: PendingProductsViewHolder, position: Int) {
        val currentProductInOrder = pendingOrderContents[position]

        notDeliveredOrders.forEach { order ->
            if (order.id == currentProductInOrder.orderId) {

                when (order.orderStatus) {
                    OrderStatus.PENDING.toString() -> {
                        holder.view.setBackgroundColor(
                            context.getColor(R.color.light_orange)
                        )
                        holder.productStatus.setTextColor(context.getColor(R.color.orange))
                        holder.productStatus.text = context.getString(R.string.status_pending)
                    }
                    OrderStatus.ORDERED.toString() -> {
                        holder.view.setBackgroundColor(
                            context.getColor(R.color.light_yellow)
                        )
                        holder.productStatus.setTextColor(context.getColor(R.color.yellow))
                        holder.productStatus.text = context.getString(R.string.status_ordered)

                    }
                    OrderStatus.CONFIRMED.toString() -> {
                        holder.view.setBackgroundColor(
                            context.getColor(R.color.light_green)
                        )
                        holder.productStatus.setTextColor(context.getColor(R.color.green))
                        holder.productStatus.text = context.getString(R.string.status_confirmed)
                    }
                }
            }
        }
        holder.productName.text = currentProductInOrder.productName
        holder.productPrice.text = currentProductInOrder.productPrice
        holder.productQuantity.text = currentProductInOrder.quantity
    }


    override fun getItemCount(): Int = pendingOrderContents.size

    fun setOrderContents(
        orderContents: List<FirebaseOrderContent>,
        notDeliveredOrders: List<FirebaseOrder>
    ) {
        this.pendingOrderContents.clear()
        this.notDeliveredOrders.clear()
        this.pendingOrderContents.addAll(orderContents)
        this.notDeliveredOrders.addAll(notDeliveredOrders)
        notifyDataSetChanged()
    }
}

class PendingProductsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    // Holds the ProductTextView that will add each product to
    val productName = view.product_item_text!!
    val productPrice = view.product_item_price!!
    val productQuantity = view.product_item_quantity!!
    val productStatus = view.product_item_status!!
}
