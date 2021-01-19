package com.example.android.storemanagement.edit_order

import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.ORDER_KEY
import com.example.android.storemanagement.create_order.InfoOrderFragment
import com.example.android.storemanagement.firebase.FirebaseOrder
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.Order
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_create_order.*

class ViewOrderFragment : InfoOrderFragment() {

    override var fragmentTitle: String = "View Order"
    override var buttonText: String = "Ok"
    private lateinit var currentOrder: Order
    private var currentOrderContents: MutableList<OrderContent>? = null
    var currentFirebaseOrderContents: MutableList<FirebaseOrderContent> = mutableListOf()

    private val orderContentViewModel: OrderContentViewModel by lazy {
        ViewModelProvider(this).get(OrderContentViewModel(requireActivity().application)::class.java)
    }

    override fun onStart() {
        super.onStart()
//        imageView.setImageDrawable("lemon.png")
        val order = arguments?.getSerializable(ORDER_KEY)
        final_price.isEnabled = false
        if (order is Order) {
            orderContentViewModel.allOrderContents.observe(this, Observer { viewState ->
                val order: Order = arguments?.getSerializable(ORDER_KEY) as Order
                currentOrder = order
                Log.d("Tina", "final price edit order " + currentOrder.finalPrice)
                setupRecyclerView()
            })
        } else if (order is FirebaseOrder) {
            getFirebaseOrderContents(order.id)
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
                                firebaseOrderContent.userId,
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
        }
    }

    override fun setupRecyclerView() {
        create_order_recycler_view.layoutManager =
            LinearLayoutManager(requireContext())

        val viewOrderAdapter = ViewOrderAdapter(
            requireContext()
        )
        create_order_recycler_view.adapter = viewOrderAdapter

        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        viewOrderAdapter.setProductsInOrder(currentOrderContents, currentFirebaseOrderContents)
        setupEmptyView(empty_view_create_order, create_order_recycler_view)

        productViewModel.allProducts.observe(this, Observer { products ->
            products?.let {
                viewOrderAdapter.setProducts(it)
            }
        })
    }
}