package com.example.android.storemanagement


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.firebase.*
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.addFirebaseNotification
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.addFirebaseProductForUser
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.isNotificationAddedForUser
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseProductQuantity
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseProductQuantityForUser
import com.example.android.storemanagement.orders_tab.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.fragment_notifications.view.*


open class NotificationsFragment : Fragment() {
    private var notifications = mutableListOf<FirebaseNotification>()
    private var firebaseOrders = mutableListOf<FirebaseOrder>()
    lateinit var onNavigationChangedListener: OnNavigationChangedListener
    var listener: OnNavigationChangedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_notifications,
            container,
            false
        )
        view.toolbarNotifications.setNavigationIcon(R.drawable.ic_baseline_arrow_back)
        view.toolbarNotifications.setNavigationOnClickListener {
            parentFragmentManager.popBackStackImmediate()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        getFirebaseNotifications()
        activity?.toolbarMain?.overflowIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_baseline_notifications)
    }

    private fun getFirebaseNotifications() {
        val user = FirebaseAuth.getInstance().currentUser
        val uniqueId: String = user!!.uid
        val database = FirebaseDatabase.getInstance()
        val notificationsQuery: Query = database.getReference("Notifications").child(uniqueId)
        notificationsQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val firebaseNotification =
                    dataSnapshot.getValue(FirebaseNotification::class.java)
                if (firebaseNotification != null && notifications.none { it.orderId == firebaseNotification.orderId }) {
                    val notification = FirebaseNotification(
                        firebaseNotification.orderId,
                        firebaseNotification.fromUserId,
                        firebaseNotification.hasTheOrder,
                        firebaseNotification.seen
                    )
                    notifications.add(notification)
                    getFirebaseOrders(firebaseNotification.fromUserId)
                    activity?.runOnUiThread { setupRecyclerView(notifications, firebaseOrders) }
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val firebaseNotification =
                    dataSnapshot.getValue(FirebaseNotification::class.java)
                if (firebaseNotification != null && notifications.any { it.orderId == firebaseNotification?.orderId }) {
                    val notificationToBeDeleted: FirebaseNotification =
                        notifications.first { it.orderId == firebaseNotification?.orderId }
                    val notification = FirebaseNotification(
                        firebaseNotification.orderId,
                        firebaseNotification.fromUserId,
                        firebaseNotification.hasTheOrder,
                        firebaseNotification.seen
                    )
                    notifications.remove(notificationToBeDeleted)
                    notifications.add(notification)
                    getFirebaseOrders(firebaseNotification.fromUserId)
                    activity?.runOnUiThread { setupRecyclerView(notifications, firebaseOrders) }
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        activity?.runOnUiThread { setupRecyclerView(notifications, firebaseOrders) }
    }

    private fun getFirebaseOrders(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("Orders").child(userId)
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val firebaseOrder =
                    dataSnapshot.getValue(FirebaseOrder::class.java)
                if (!firebaseOrders.contains(firebaseOrder)) {
                    firebaseOrders.add(firebaseOrder!!)
                    activity?.runOnUiThread { setupRecyclerView(notifications, firebaseOrders) }
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        activity?.runOnUiThread { setupRecyclerView(notifications, firebaseOrders) }
    }

    private fun openEditOrderFragment(firebaseUser: FirebaseUserInternal, firebaseOrder: FirebaseOrder?) {
        if (::onNavigationChangedListener.isInitialized) {
            //onNavigationChangedListener.onNavigationChanged(EDIT_ORDER_TAB)
            listener = onNavigationChangedListener
        }

        listener?.onNavigationChanged(
            tabNumber = EDIT_ORDER_TAB,
            firebaseOrder = firebaseOrder,
            firebaseUser = firebaseUser
        )
    }

    private fun onOrderStatusChanged(orderId: String, userId: String, orderStatus: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val uniqueId: String = user!!.uid
        Toast.makeText(context, orderStatus, Toast.LENGTH_SHORT).show()
        updateFirebaseOrderStatus(userId, orderId, orderStatus)
        when (orderStatus) {
            OrderStatus.CONFIRMED.toString() -> {
                updateProductsOnStatusChange(userId, orderId, OrderStatus.CONFIRMED.toString())
            }
            OrderStatus.DELIVERED.toString() -> {
                updateProductsOnStatusChange(userId, orderId, OrderStatus.DELIVERED.toString())
            }
        }

        val notification = FirebaseNotification(orderId, uniqueId, false.toString(), false.toString())
        isNotificationAddedForUser(userId, notification) { isNotificationExisting ->
            if (!isNotificationExisting) {
                addFirebaseNotification(notification, userId)
            }
        }
    }

    private fun updateFirebaseOrderStatus(userId: String, firebaseOrderId: String, status: String) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Orders")
        val ordersQuery: Query = myRef.child(userId).child(firebaseOrderId)
        ordersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val reference: DatabaseReference =
                    dataSnapshot.ref.child("orderStatus")
                reference.setValue(status)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun updateProductsOnStatusChange(userId: String, fbOrderId: String, orderStatus: String) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("OrderContent").child(userId)

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val firebaseOrderContents = dataSnapshot.children

                for (item in firebaseOrderContents) {
                    val firebaseOrderContent: FirebaseOrderContent? =
                        item.getValue(FirebaseOrderContent::class.java)

                    if (firebaseOrderContent != null && firebaseOrderContent.orderId == fbOrderId) {
                        getCurrentUserFirebaseProduct(
                            userId,
                            firebaseOrderContent,
                            orderStatus
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // if the order has status Confirmed - we change the current user products' quantity
    // if the order has status Delivered - we change the orderUserId products' quantities
    private fun getCurrentUserFirebaseProduct(
        orderUserId: String,
        firebaseOrderContent: FirebaseOrderContent,
        orderStatus: String
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val uniqueId: String = user!!.uid
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = if (orderStatus == OrderStatus.CONFIRMED.toString()) database.getReference("Products")
            .child(uniqueId) else database.getReference("Products").child(orderUserId)

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val firebaseProducts = dataSnapshot.children

                if (orderStatus == OrderStatus.DELIVERED.toString())
                    if (!dataSnapshot.exists()) {
                        val newFirebaseProduct = FirebaseProduct(
                            firebaseOrderContent.productName,
                            firebaseOrderContent.productPrice,
                            "0",
                            firebaseOrderContent.productBarcode,
                            firebaseOrderContent.quantity,
                            ""
                        )
                        addFirebaseProductForUser(orderUserId, newFirebaseProduct)
                    }

                for (item in firebaseProducts) {
                    val firebaseProduct: FirebaseProduct? =
                        item.getValue(FirebaseProduct::class.java)

                    when (orderStatus) {
                        OrderStatus.CONFIRMED.toString() -> {
                            if (firebaseProduct != null && firebaseProduct.barcode == firebaseOrderContent.productBarcode) {
                                val newQuantity =
                                    (firebaseProduct.quantity.toInt() - firebaseOrderContent.quantity.toInt()).toString()
                                updateFirebaseProductQuantity(firebaseProduct.barcode, newQuantity)
                            }
                        }
                        OrderStatus.DELIVERED.toString() -> {
                            if (firebaseProduct != null && firebaseProduct.barcode == firebaseOrderContent.productBarcode) {
                                val newQuantity =
                                    (firebaseProduct.quantity.toInt() + firebaseOrderContent.quantity.toInt()).toString()
                                updateFirebaseProductQuantityForUser(orderUserId, firebaseProduct.barcode, newQuantity)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupEmptyView() {
        val users = notifications_recycler_view?.adapter
        if (users?.itemCount == 0) {
            notifications_recycler_view?.visibility = View.GONE
            empty_view_notifications?.visibility = View.VISIBLE
        } else {
            notifications_recycler_view?.visibility = View.VISIBLE
            empty_view_notifications?.visibility = View.GONE
        }
    }

    private fun setupRecyclerView(firebaseNotifications: List<FirebaseNotification>, firebaseOrders: List<FirebaseOrder>) {
        notifications_recycler_view?.layoutManager =
            LinearLayoutManager(requireContext())
        val notificationsAdapter = NotificationsAdapter(
            requireContext(), ::onOrderStatusChanged, ::openEditOrderFragment
        )
        notifications_recycler_view?.adapter = notificationsAdapter

        notificationsAdapter.setNotifications(firebaseNotifications, firebaseOrders)
        setupEmptyView()
    }
}