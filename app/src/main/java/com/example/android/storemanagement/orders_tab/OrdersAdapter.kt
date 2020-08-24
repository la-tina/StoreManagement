package com.example.android.storemanagement.orders_tab

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.android.storemanagement.R
import com.example.android.storemanagement.orders_database.Order
import kotlinx.android.synthetic.main.order_item.view.*
import kotlin.reflect.KSuspendFunction1

class OrdersAdapter(
    private val context: Context,
    private val deleteOrderAction: (Order) -> Unit,
    private val openEditOrderTab: (Order) -> Unit
    //private val updateQuantitiesOnDelete: (Order) -> Unit
) :
    RecyclerView.Adapter<OrdersHolder>() {

    private var orders = emptyList<Order>() // Cached copy of products

    // Gets the number of items in the list
    override fun getItemCount(): Int =
        orders.size

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersHolder =
        OrdersHolder(LayoutInflater.from(context).inflate(R.layout.order_item, parent, false))

    //Binds each product in the list to a view
    override fun onBindViewHolder(holder: OrdersHolder, position: Int) {
        val currentOrder = orders[position]
        val orderPrice = String.format("%.1f", currentOrder.finalPrice).toFloat().toString()
        holder.finalPrice.text = orderPrice
        //String.format("%.1f", finalPrice).toFloat().toString()
        holder.date.text = currentOrder.date

        holder.imageContextMenu.setOnClickListener { view -> showPopup(view, currentOrder) }
    }

    private fun showPopup(view: View, order: Order) {
        PopupMenu(context, view).apply {
            inflate(R.menu.context_menu)
            setOnMenuItemClickListener { item: MenuItem? ->
                when (item!!.itemId) {
                    R.id.edit -> {
                        Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                        openEditOrderTab(order)
                    }
                    R.id.delete -> {
                        Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                        deleteOrderAction(order)
                        //updateQuantitiesOnDelete(order)
                    }
                }
                true
            }
            show()
        }
    }

    internal fun setOrders(orders: List<Order>) {
        this.orders = orders
        notifyDataSetChanged()
    }

    fun getOrderAtPosition(position: Int): Order =
        orders[position]
}

class OrdersHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the OrderTextView that will add each product to
    val finalPrice = view.order_item_price!!
    val date = view.order_date!!
    val imageContextMenu: ImageView = view.context_menu_image!!
}






