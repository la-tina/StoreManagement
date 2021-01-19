package com.example.android.storemanagement.notifications

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseDatabaseNotificationsOperations.updateFirebaseSeenIndicator
import com.example.android.storemanagement.firebase.FirebaseDatabaseOrdersOperations.getFirebaseOrder
import com.example.android.storemanagement.firebase.FirebaseDatabaseUsersOperations.getFirebaseUser
import com.example.android.storemanagement.firebase.FirebaseNotification
import com.example.android.storemanagement.firebase.FirebaseOrder
import com.example.android.storemanagement.firebase.FirebaseUserInternal
import com.example.android.storemanagement.orders_tab.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.notification_item.view.*
import kotlinx.android.synthetic.main.user_item.view.user_email
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


class NotificationsAdapter(
    private val context: Context,
    private val onOrderStatusChanged: (orderId: String, userId: String, orderStatus: String) -> Unit,
    private val openEditOrderFragment: (firebaseUser: FirebaseUserInternal, firebaseOrder: FirebaseOrder) -> Unit
) :
    RecyclerView.Adapter<NotificationsHolder>() {

    private var notifications = emptyList<FirebaseNotification>() // Cached copy of notifications
    private var orders = emptyList<FirebaseOrder>()

    // Gets the number of items in the list
    override fun getItemCount(): Int = notifications.size

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsHolder =
        NotificationsHolder(LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false))

    override fun onBindViewHolder(holder: NotificationsHolder, position: Int) {
        val currentNotification: FirebaseNotification = notifications[position]
        val user = FirebaseAuth.getInstance().currentUser
        val userId: String =
            if (currentNotification.hasTheOrder == false.toString()) user!!.uid else currentNotification.fromUserId

        holder.newIndicator.visibility = if (currentNotification.seen == true.toString()) View.INVISIBLE else View.VISIBLE
        if (currentNotification.seen == false.toString()) {
            Log.d("TinaFB", "seen " + currentNotification.seen)
            val aniFade: Animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            holder.newIndicator.startAnimation(aniFade)
            CoroutineScope(Dispatchers.IO).launch {
                delay(TimeUnit.SECONDS.toMillis(2))
                withContext(Dispatchers.Main) {
                    updateFirebaseSeenIndicator()
                }
            }
        }
//        holder.newIndicator.visibility = if (currentNotification.seen == true.toString()) View.GONE else View.VISIBLE

        getFirebaseOrder(userId, currentNotification.orderId) { order ->
            holder.orderId.text = currentNotification.orderId
            holder.orderId.setTextColor(context.getColor(R.color.colorPrimary))
            when (order.orderStatus) {
                OrderStatus.ORDERED.toString() -> {
                    holder.view.setBackgroundColor(context.getColor(R.color.light_yellow))
                    holder.orderStatus.setTextColor(context.getColor(R.color.yellow))
                    holder.confirmButton.text = context.getString(R.string.confirm)
                    holder.confirmButton.visibility = View.VISIBLE
                    holder.cancelButton.visibility = View.VISIBLE
                }

                OrderStatus.CONFIRMED.toString() -> {
                    holder.view.setBackgroundColor(context.getColor(R.color.light_green))
                    holder.orderStatus.setTextColor(context.getColor(R.color.green))
                    holder.confirmButton.text = context.getString(R.string.delivered)
                    holder.confirmButton.visibility = View.VISIBLE
                    holder.cancelButton.visibility = View.INVISIBLE
                }

                OrderStatus.DELIVERED.toString() -> {
                    holder.view.setBackgroundColor(context.getColor(R.color.light_primary))
                    holder.orderStatus.setTextColor(context.getColor(R.color.colorPrimary))
                    holder.confirmButton.visibility = View.GONE
                    holder.cancelButton.visibility = View.GONE
                }

                OrderStatus.CANCELLED.toString() -> {
                    holder.view.setBackgroundColor(context.getColor(R.color.light_gray))
                    holder.orderStatus.setTextColor(context.getColor(R.color.dark_gray))
                    holder.confirmButton.visibility = View.GONE
                    holder.cancelButton.visibility = View.GONE
                }
            }

            holder.confirmButton.setOnClickListener {
                if (order.orderStatus == OrderStatus.ORDERED.toString())
                    onOrderStatusChanged(
                        currentNotification.orderId,
                        currentNotification.fromUserId,
                        OrderStatus.CONFIRMED.toString()
                    )
                else if (order.orderStatus == OrderStatus.CONFIRMED.toString())
                    onOrderStatusChanged(
                        currentNotification.orderId,
                        userId,
                        OrderStatus.DELIVERED.toString()
                    )
            }

            holder.cancelButton.setOnClickListener {
                onOrderStatusChanged(
                    currentNotification.orderId,
                    userId,
                    OrderStatus.CANCELLED.toString()
                )
            }

            holder.itemView.setOnClickListener {
                getFirebaseUser(userId) { user -> openEditOrderFragment(user, order) }
            }

            holder.orderStatus.text = order.orderStatus
            holder.orderDate.text = order.date

            getFirebaseUser(currentNotification.fromUserId) { user ->
                holder.fromUser.text = user.email
            }
//            openCreateOrderFragment(currentUser)
        }
    }

    fun setNotifications(notifications: List<FirebaseNotification>, firebaseOrders: List<FirebaseOrder>) {
        this.notifications = notifications
        this.orders = firebaseOrders
        notifyDataSetChanged()
    }
}

class NotificationsHolder(val view: View) : RecyclerView.ViewHolder(view) {
    // Holds the OrderTextView that will add each product to
    val orderId = view.order_id!!
    val orderDate = view.order_date!!
    val orderStatus = view.order_status!!
    val fromUser = view.user_email!!
    val confirmButton = view.confirm_button!!
    val cancelButton = view.cancel_button!!
    val newIndicator = view.new_indicator!!
}
