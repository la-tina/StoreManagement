package com.example.android.storemanagement.orders_tab

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseOrder
import com.example.android.storemanagement.orders_database.Order
import kotlinx.android.synthetic.main.order_item.view.*


class OrdersAdapter(
    private val context: Context,
    private val deleteOrderAction: (Order?, FirebaseOrder?) -> Unit,
    private val openEditOrderTab: (Order?, FirebaseOrder?) -> Unit,
    private val onOrderStatusChanged: (Order?, FirebaseOrder?, OrderStatus) -> Unit
    //private val updateQuantitiesOnDelete: (Order) -> Unit
) :
    RecyclerView.Adapter<OrdersHolder>() {

    private var orders = emptyList<Order>() // Cached copy of orders
    private var firebaseOrders = emptyList<FirebaseOrder>() // Cached copy of firebase orders
    private var editItem: MenuItem? = null

    private var areFirebaseOrdersLoaded = false

    // Gets the number of items in the list
    override fun getItemCount(): Int = if (areFirebaseOrdersLoaded) firebaseOrders.size else orders.size

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersHolder =
        OrdersHolder(LayoutInflater.from(context).inflate(R.layout.order_item, parent, false))

    //Binds each order in the list to a view
    override fun onBindViewHolder(holder: OrdersHolder, position: Int) {
        val currentOrder: Any = if (areFirebaseOrdersLoaded) firebaseOrders[position] else orders[position]
        if (currentOrder is Order) {
            val orderPrice = String.format("%.1f", currentOrder.finalPrice).toFloat().toString()
            holder.id.text = currentOrder.id.toString()
            holder.finalPrice.text = orderPrice
            //String.format("%.1f", finalPrice).toFloat().toString()
            holder.date.text = currentOrder.date

            when (currentOrder.orderStatus) {
                OrderStatus.ORDERED.toString() -> {
                    holder.status.text = context.getString(R.string.status_ordered)
                    holder.status.setTextColor(context.getColor(R.color.yellow))
                    holder.view.setBackgroundColor(context.getColor(R.color.light_yellow))
                }
                OrderStatus.CONFIRMED.toString() -> {
                    holder.status.text = context.getString(R.string.status_confirmed)
                    holder.status.setTextColor(context.getColor(R.color.green))
                    holder.view.setBackgroundColor(context.getColor(R.color.light_green))
                }
                OrderStatus.DELIVERED.toString() -> {
                    holder.status.text = context.getString(R.string.status_delivered)
                    holder.status.setTextColor(context.getColor(R.color.colorPrimary))
                    holder.view.setBackgroundColor(context.getColor(R.color.light_primary))
                }
                else -> {
                    holder.status.text = context.getString(R.string.status_pending)
                    holder.status.setTextColor(context.getColor(R.color.orange))
                    holder.view.setBackgroundColor(context.getColor(R.color.light_orange))
                }
            }

            holder.itemView.setOnClickListener {
                openEditOrderTab(currentOrder, null)
            }

            if (currentOrder.orderStatus == OrderStatus.DELIVERED.toString()) {
                holder.imageContextMenu.visibility = View.GONE
            } else {
                holder.imageContextMenu.visibility = View.VISIBLE
            }
            holder.imageContextMenu.setOnClickListener { view -> showPopup(view, currentOrder, null) }
        } else if (currentOrder is FirebaseOrder && currentOrder.finalPrice.isNotEmpty()) {
            val orderPrice = String.format("%.1f", currentOrder.finalPrice.toFloat()).toFloat().toString()
            holder.id.text = currentOrder.id
            holder.finalPrice.text = orderPrice
            //String.format("%.1f", finalPrice).toFloat().toString()
            holder.date.text = currentOrder.date

            when (currentOrder.orderStatus) {
                OrderStatus.ORDERED.toString() -> {
                    holder.status.text = context.getString(R.string.status_ordered)
                    holder.status.setTextColor(context.getColor(R.color.yellow))
                    holder.view.setBackgroundColor(context.getColor(R.color.light_yellow))
                }
                OrderStatus.CONFIRMED.toString() -> {
                    holder.status.text = context.getString(R.string.status_confirmed)
                    holder.status.setTextColor(context.getColor(R.color.green))
                    holder.view.setBackgroundColor(context.getColor(R.color.light_green))
                }
                OrderStatus.DELIVERED.toString() -> {
                    holder.status.text = context.getString(R.string.status_delivered)
                    holder.status.setTextColor(context.getColor(R.color.colorPrimary))
                    holder.view.setBackgroundColor(context.getColor(R.color.light_primary))
                }
                else -> {
                    holder.status.text = context.getString(R.string.status_pending)
                    holder.status.setTextColor(context.getColor(R.color.orange))
                    holder.view.setBackgroundColor(context.getColor(R.color.light_orange))
                }
            }

            holder.itemView.setOnClickListener {
                openEditOrderTab(null, currentOrder)
            }

            if (currentOrder.orderStatus == OrderStatus.DELIVERED.toString()) {
                holder.imageContextMenu.visibility = View.GONE
            } else {
                holder.imageContextMenu.visibility = View.VISIBLE
            }
            holder.imageContextMenu.setOnClickListener { view -> showPopup(view, null, currentOrder) }
        }
    }

    private fun showPopup(view: View, order: Order?, firebaseOrder: FirebaseOrder?) {
        PopupMenu(context, view).apply {
            inflate(R.menu.context_menu)

            if (areFirebaseOrdersLoaded) {
                if (firebaseOrder?.orderStatus == OrderStatus.ORDERED.toString()
                    || firebaseOrder?.orderStatus == OrderStatus.CONFIRMED.toString()
                ) {
                    menu.findItem(R.id.order).isVisible = false
                    menu.findItem(R.id.edit).isVisible = false
                    menu.findItem(R.id.delete).isVisible = true
                }
            } else {
                if (order?.orderStatus == OrderStatus.ORDERED.toString()
                    || order?.orderStatus == OrderStatus.CONFIRMED.toString()
                ) {
                    menu.findItem(R.id.order).isVisible = false
                    menu.findItem(R.id.edit).isVisible = false
                    menu.findItem(R.id.delete).isVisible = true
                }
            }

            setOnMenuItemClickListener { item: MenuItem? ->
                when (item!!.itemId) {
                    R.id.order -> {
                        Toast.makeText(
                            context,
                            context.getString(R.string.status_ordered),
                            Toast.LENGTH_SHORT
                        ).show()
                        onOrderStatusChanged(order, firebaseOrder, OrderStatus.ORDERED)
                    }
                    R.id.delivered -> {
                        Toast.makeText(
                            context,
                            context.getString(R.string.status_delivered),
                            Toast.LENGTH_SHORT
                        ).show()
                        onOrderStatusChanged(order, firebaseOrder, OrderStatus.DELIVERED)
                    }
                    R.id.edit -> {
                        editItem = item
                        Toast.makeText(
                            context,
                            context.getString(R.string.edit),
                            Toast.LENGTH_SHORT
                        ).show()
                        openEditOrderTab(order, firebaseOrder)
                    }
                    R.id.delete -> {
                        Toast.makeText(
                            context,
                            context.getString(R.string.deleted),
                            Toast.LENGTH_SHORT
                        ).show()
                        deleteOrderAction(order, firebaseOrder)
                        //updateQuantitiesOnDelete(order)
                    }
                }
                true
            }
            show()
        }
    }

    internal fun setOrders(orders: List<Order>?, firebaseOrders: List<FirebaseOrder>?) {
        if (orders == null) {
            this.firebaseOrders = firebaseOrders!!
            areFirebaseOrdersLoaded = true
        } else {
            this.orders = orders
            areFirebaseOrdersLoaded = false
        }

        notifyDataSetChanged()
    }

    fun getOrderAtPosition(position: Int): Order =
        orders[position]

    fun getFirebaseOrderAtPosition(position: Int): FirebaseOrder =
        firebaseOrders[position]
}

class OrdersHolder(val view: View) : RecyclerView.ViewHolder(view) {
    // Holds the OrderTextView that will add each product to
    val id: TextView = view.order_id!!
    val finalPrice = view.order_item_price!!
    val date = view.order_date!!
    val imageContextMenu: ImageView = view.context_menu_image!!
    val status: TextView = view.status!!
}






