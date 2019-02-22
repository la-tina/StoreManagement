package com.example.android.storemanagement.orders_tab

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.R
import com.example.android.storemanagement.orders_database.Order
import kotlinx.android.synthetic.main.order_item.view.*

class OrdersAdapter(private val context: Context) :
    RecyclerView.Adapter<OrdersHolder>(){

    private var orders = emptyList<Order>() // Cached copy of products

    // Gets the number of items in the list
    override fun getItemCount(): Int {
        return orders.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersHolder {
        return OrdersHolder(
            LayoutInflater.from(context).inflate(
                R.layout.order_item,
                parent,
                false
            )
        )
    }

    //Binds each product in the list to a view
    override fun onBindViewHolder(holder: OrdersHolder, position: Int) {
        val currentOrder = orders[position]
        holder.finalPrice.text = currentOrder.finalPrice.toString()
        //String.format("%.1f", finalPrice).toFloat().toString()
        holder.date.text = currentOrder.date
    }

    internal fun setOrders(orders: List<Order>) {
        this.orders = orders
        notifyDataSetChanged()
    }

    fun getOrderAtPosition(position: Int): Order {
        return orders[position]
    }
}

class OrdersHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the OrderTextView that will add each product to
    val finalPrice = view.product_item_price!!
    val date = view.order_date!!
}






