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
import kotlinx.android.synthetic.main.order_item.view.*


class OrdersAdapter(
    private val context: Context,
    private val deleteOrderAction: (FirebaseOrder?) -> Unit,
    private val openEditOrderTab: (FirebaseOrder?) -> Unit,
    private val onOrderStatusChanged: (FirebaseOrder?, OrderStatus) -> Unit
) :
    RecyclerView.Adapter<OrdersHolder>() {

    private var firebaseOrders = emptyList<FirebaseOrder>() // Cached copy of firebase orders
    private var editItem: MenuItem? = null

    // Gets the number of items in the list
    override fun getItemCount(): Int = firebaseOrders.size

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersHolder =
        OrdersHolder(LayoutInflater.from(context).inflate(R.layout.order_item, parent, false))

    //Binds each order in the list to a view
    override fun onBindViewHolder(holder: OrdersHolder, position: Int) {
        val currentOrder: FirebaseOrder = firebaseOrders[position]
        if (currentOrder.finalPrice.isNotEmpty()) {
            val orderPrice = String.format("%.1f", currentOrder.finalPrice.toFloat()).toFloat().toString()
            holder.id.text = currentOrder.id
            holder.finalPrice.text = orderPrice
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
                OrderStatus.CANCELLED.toString() -> {
                    holder.status.text = context.getString(R.string.status_cancelled)
                    holder.status.setTextColor(context.getColor(R.color.dark_gray))
                    holder.view.setBackgroundColor(context.getColor(R.color.light_gray))
                }
                else -> {
                    holder.status.text = context.getString(R.string.status_pending)
                    holder.status.setTextColor(context.getColor(R.color.orange))
                    holder.view.setBackgroundColor(context.getColor(R.color.light_orange))
                }
            }

            holder.itemView.setOnClickListener {
                openEditOrderTab(currentOrder)
            }

            if (currentOrder.orderStatus == OrderStatus.DELIVERED.toString() || currentOrder.orderStatus == OrderStatus.CANCELLED.toString()) {
                holder.imageContextMenu.visibility = View.GONE
            } else {
                holder.imageContextMenu.visibility = View.VISIBLE
            }
            holder.imageContextMenu.setOnClickListener { view -> showPopup(view, currentOrder) }
        }
    }

    private fun showPopup(view: View, firebaseOrder: FirebaseOrder?) {
        PopupMenu(context, view).apply {
            inflate(R.menu.context_menu)

            if (firebaseOrder?.orderStatus == OrderStatus.ORDERED.toString()
                || firebaseOrder?.orderStatus == OrderStatus.CONFIRMED.toString()
            ) {
                menu.findItem(R.id.order).isVisible = false
                menu.findItem(R.id.edit).isVisible = false
                menu.findItem(R.id.delete).isVisible = true
            }

            setOnMenuItemClickListener { item: MenuItem? ->
                when (item!!.itemId) {
                    R.id.order -> {
                        Toast.makeText(
                            context,
                            context.getString(R.string.status_ordered),
                            Toast.LENGTH_SHORT
                        ).show()
                        onOrderStatusChanged(firebaseOrder, OrderStatus.ORDERED)
                    }
                    R.id.delivered -> {
                        Toast.makeText(
                            context,
                            context.getString(R.string.status_delivered),
                            Toast.LENGTH_SHORT
                        ).show()
                        onOrderStatusChanged(firebaseOrder, OrderStatus.DELIVERED)
                    }
                    R.id.edit -> {
                        editItem = item
                        Toast.makeText(
                            context,
                            context.getString(R.string.edit),
                            Toast.LENGTH_SHORT
                        ).show()
                        openEditOrderTab(firebaseOrder)
                    }
                    R.id.delete -> {
                        Toast.makeText(
                            context,
                            context.getString(R.string.deleted),
                            Toast.LENGTH_SHORT
                        ).show()
                        deleteOrderAction(firebaseOrder)
                    }
                }
                true
            }
            show()
        }
    }

    internal fun setOrders(firebaseOrders: List<FirebaseOrder>?) {
        this.firebaseOrders = firebaseOrders!!
        notifyDataSetChanged()
    }
}

class OrdersHolder(val view: View) : RecyclerView.ViewHolder(view) {
    // Holds the OrderTextView that will add each product to
    val id: TextView = view.order_id!!
    val finalPrice = view.order_item_price!!
    val date = view.order_date!!
    val imageContextMenu: ImageView = view.context_menu_image!!
    val status: TextView = view.status!!
}






