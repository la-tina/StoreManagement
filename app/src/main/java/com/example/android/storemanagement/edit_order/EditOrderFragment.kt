package com.example.android.storemanagement.edit_order

import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.ORDER_KEY
import com.example.android.storemanagement.R
import com.example.android.storemanagement.create_order.InfoOrderFragment
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.deleteFirebaseOrderContentData
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.deleteFirebaseOrderData
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseOrderContent
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseOrderDate
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseOrderFinalPrice
import com.example.android.storemanagement.firebase.FirebaseOrder
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_tab.OrderStatus
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_create_order.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

open class EditOrderFragment : InfoOrderFragment() {

    override var fragmentTitle: String = ""
    override var buttonText: String = "Save"
    var currentOrderContents: List<OrderContent>? = null
    var currentFirebaseOrderContents: MutableList<FirebaseOrderContent> = mutableListOf()

    var currentOrder: Order? = null
    var currentFirebaseOrder: FirebaseOrder? = null

    private val orderContentViewModel: OrderContentViewModel by lazy {
        ViewModelProviders.of(this)
            .get(OrderContentViewModel(requireActivity().application)::class.java)
    }

    override fun onStart() {
        super.onStart()
//        imageView.setImageDrawable("lemon.png")
        orderContentViewModel.allOrderContents.observe(this, Observer { viewState ->
            onOrderContentsChanged(viewState)
        })
    }

    private fun onOrderContentsChanged(viewState: List<OrderContent>?) {
        val order = arguments?.getSerializable(ORDER_KEY)
        if (order is Order) {
            setupRecyclerView()

            currentOrder = order
            toolbarTopOrder.title = if (!canOrderBeEdited()) "Order Content" else "Edit Order"
            info_text?.text =
                if (!canOrderBeEdited()) context?.getString(R.string.view_order_info) else context?.getString(
                    R.string.edit_order_info
                )
//        if (finalPrice == 0F){
//            finalPrice = currentOrder.finalPrice
//        }
            Log.d("Tina", "final price edit order " + currentOrder?.finalPrice)
            final_price.text = currentOrder?.finalPrice.toString()

            currentOrderContents = viewState?.filter { it.orderId == order.id }

            setupRecyclerView()

            if (!canOrderBeEdited()) {
                button_add_order.visibility = View.GONE
                constraintLayout.maxHeight = 160
            }
            button_add_order.setOnClickListener { onEditButtonClicked(order, null) }

        } else if (order is FirebaseOrder) {
            currentFirebaseOrder = order
            toolbarTopOrder.title = if (!canOrderBeEdited()) "Order Content" else "Edit Order"
            info_text?.text =
                if (!canOrderBeEdited()) context?.getString(R.string.view_order_info) else context?.getString(
                    R.string.edit_order_info
                )
//        if (finalPrice == 0F){
//            finalPrice = currentOrder.finalPrice
//        }
            Log.d("Tina", "final price edit order " + currentFirebaseOrder?.finalPrice)
            final_price.text = currentFirebaseOrder?.finalPrice.toString()

            getFirebaseOrderContents(order.id)

            if (!canOrderBeEdited()) {
                button_add_order.visibility = View.GONE
                constraintLayout.maxHeight = 160
            }
            button_add_order.setOnClickListener { onEditButtonClicked(null, order) }
        }
    }

    private fun getFirebaseOrderContents(fbOrderId: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        if (user != null) {
            val uniqueId: String = user.uid
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val myRef: DatabaseReference = database.getReference("OrderContent").child(uniqueId)

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val firebaseOrderContents = dataSnapshot.children

                    for (item in firebaseOrderContents) {
                        val firebaseOrderContent: FirebaseOrderContent? =
                            item.getValue(FirebaseOrderContent::class.java)

                        if (firebaseOrderContent != null && firebaseOrderContent.orderId == fbOrderId) {
                            val orderContent = FirebaseOrderContent(
                                firebaseOrderContent.productBarcode,
                                firebaseOrderContent.productName,
                                firebaseOrderContent.productPrice,
                                firebaseOrderContent.productOvercharge,
                                firebaseOrderContent.quantity,
                                firebaseOrderContent.orderId,
                                item.key!!
                            )
                            if (!currentFirebaseOrderContents.contains(orderContent) && firebaseOrderContent.orderId == fbOrderId) {
                                currentFirebaseOrderContents.add(orderContent)
                                activity?.runOnUiThread {
                                    setupRecyclerView()
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
            myRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    private fun canOrderBeEdited(): Boolean = if (currentOrder != null)
        currentOrder?.orderStatus == OrderStatus.PENDING.toString() else currentFirebaseOrder?.orderStatus == OrderStatus.PENDING.toString()

    private fun onEditButtonClicked(order: Order?, firebaseOrder: FirebaseOrder?) {
        if (order != null) {
            val quantities = (create_order_recycler_view.adapter as EditOrderAdapter).quantities

            quantities.forEach { (barcode, quantity) ->
                updateQuantity(barcode, quantity, order.id)
            }
//        orderContentViewModel.updateOrderFinalPrice(order.id, finalPrice)

//        updateFinalPrice()
            ordersViewModel.updateFinalPrice(finalPrice, order.id)
        } else {
            val quantities = (create_order_recycler_view.adapter as EditOrderAdapter).quantities

            quantities.forEach { (barcode, quantity) ->
                currentFirebaseOrderContents.forEach { content ->
                    if (content.productBarcode == barcode) {
                        updateFirebaseQuantity(content.id, quantity)
                    }
                }
            }
            if (currentFirebaseOrderContents.isNullOrEmpty()) {
                deleteFirebaseOrderData(currentFirebaseOrder!!.id)
            }
            updateFirebaseOrderFinalPrice(firebaseOrder!!.id, finalPrice.toString())
            updateFirebaseOrderDate(firebaseOrder.id, getFormattedDate()!!)
        }

        parentFragmentManager.popBackStackImmediate()
    }

    private fun getFormattedDate(): String? {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return current.format(formatter)
    }

    private fun deleteOrderContent() {

//        currentOrderContents?.filter { it.quantity != 0 }?.forEach {newOrderContent ->
//            newOrderContents?.add(newOrderContent)
//        }
        currentOrderContents?.filter { it.quantity == 0 }?.forEach { orderContent ->
            orderContentViewModel.deleteOrderContent(orderContent)
        }
        if (currentOrderContents!!.isNullOrEmpty()) {
            deleteCurrentOrder()
        }
    }

    private fun deleteCurrentOrder() {
        ordersViewModel.deleteOrder(currentOrder!!)
    }

    private fun updateFirebaseQuantity(firebaseOrderContentId: String, quantity: Int) {
        if (quantity == 0) {
            deleteFirebaseOrderContentData(firebaseOrderContentId)
//            activity?.runOnUiThread{
//                currentFirebaseOrderContents.remove(currentFirebaseOrderContents.first{it.id == firebaseOrderContentId})
//            }
        } else {
            updateFirebaseOrderContent(firebaseOrderContentId, quantity.toString())
        }
    }


    private fun updateQuantity(
        barcode: String,
        editedQuantity: Int,
        orderId: Long?
    ) {
        val currentProduct = productViewModel.allProducts.value
            ?.first { product -> product.barcode == barcode }

        val currentQuantity = currentProduct!!.quantity

        val orderedQuantity = orderContentViewModel.allOrderContents.value
            ?.first { product -> product.productBarcode == currentProduct.barcode }!!.quantity

        val currentBarcode = orderContentViewModel.allOrderContents.value
            ?.first { product -> product.productBarcode == currentProduct.barcode }!!.productBarcode

        val finalQuantity: Int = if (orderedQuantity > editedQuantity)
            currentQuantity - (orderedQuantity - editedQuantity)
        else currentQuantity + (editedQuantity - orderedQuantity)

//        productViewModel.updateQuantity(productName, finalQuantity)
        if (editedQuantity == 0) {
            deleteOrderContent()
        } else {
            if (orderId != null) {
                orderContentViewModel.updateQuantity(currentBarcode, editedQuantity, orderId)
            }
        }
    }

    override fun setupRecyclerView() {
        create_order_recycler_view?.layoutManager =
            LinearLayoutManager(requireContext())

        val editOrdersAdapter = EditOrderAdapter(
            requireContext(),
            ::updateFinalPrice,
            ::setOrderButtonEnabled,
            ::canOrderBeEdited
        )
        create_order_recycler_view?.adapter = editOrdersAdapter

        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        editOrdersAdapter.setProductsInOrder(currentOrderContents, currentFirebaseOrderContents)
        setupEmptyView(empty_view_create_order, info_text, create_order_recycler_view)

        productViewModel.allProducts.observe(this, Observer { products ->
            products?.let {
                editOrdersAdapter.setProducts(it)
            }
        })
    }
}