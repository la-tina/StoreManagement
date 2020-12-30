package com.example.android.storemanagement.products_tab.pending_products_tab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseOrder
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.orders_tab.OrderStatus
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsTabFragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_products_container.*


class PendingProductsFragment : ProductsTabFragment() {

    private lateinit var viewModel: PendingProductsViewModel
    private val notDeliveredOrdersList = mutableListOf<FirebaseOrder>()
    private val currentFirebaseOrderContents = mutableListOf<FirebaseOrderContent>()

    override fun setupViewModel() {
        viewModel =
            ViewModelProviders.of(this)
                .get(PendingProductsViewModel(requireActivity().application)::class.java)
    }

    override fun setOnNavigationChangedListener(onNavigationChangedListener: OnNavigationChangedListener) {
        listener = onNavigationChangedListener
    }

    override fun deleteProduct(product: Product?, firebaseProduct: FirebaseProduct?) {
        TODO("Not yet implemented")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_products_container, container, false)

    override fun onStart() {
        super.onStart()
        info_text.text = context?.getString(R.string.pending_products_info)
    }

    override fun onResume() {
        super.onResume()

        user = Firebase.auth.currentUser
        if (user != null) {
            getFirebaseOrders()
        } else {
            setupViewModel()
            setupRecyclerView()
        }
    }

    private fun getFirebaseOrders() {
        val uniqueId: String = user?.uid!!
        val database = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("Orders").child(uniqueId)

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                notDeliveredOrdersList.clear()
                val firebaseOrders = dataSnapshot.children

                for (item in firebaseOrders) {
                    val firebaseOrder: FirebaseOrder? = item.getValue(FirebaseOrder::class.java)

                    if (firebaseOrder != null) {
                        val order = FirebaseOrder(
                            firebaseOrder.finalPrice,
                            firebaseOrder.date,
                            firebaseOrder.orderStatus,
                            item.key!!
                        )
                        Log.d("TinaFirebase", "firebaseOrder onDataChange $order")
                        if (notDeliveredOrdersList.none {it.id == order.id} && shouldGetOrderData(order)) {
                            notDeliveredOrdersList.add(order)
                            getFirebaseOrderContents(order.id)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val firebaseNewOrder: FirebaseOrder? =
                    dataSnapshot.getValue(FirebaseOrder::class.java)
                if (firebaseNewOrder != null) {
                    val order = FirebaseOrder(
                        firebaseNewOrder.finalPrice,
                        firebaseNewOrder.date,
                        firebaseNewOrder.orderStatus,
                        dataSnapshot.key!!
                    )
                    Log.d("TinaFirebase", "firebaseOrder onChildAdded $order")
                    if (notDeliveredOrdersList.none {it.id == order.id} && shouldGetOrderData(order)) {
                        notDeliveredOrdersList.add(order)
                        getFirebaseOrderContents(order.id)
                    }
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val changedFirebaseOrder: FirebaseOrder? =
                    dataSnapshot.getValue(FirebaseOrder::class.java)
                if (changedFirebaseOrder != null) {
                    val order = FirebaseOrder(
                        changedFirebaseOrder.finalPrice,
                        changedFirebaseOrder.date,
                        changedFirebaseOrder.orderStatus,
                        dataSnapshot.key!!
                    )
                    Log.d("TinaFirebase", "firebaseOrder onChildAdded $order")
                    notDeliveredOrdersList.forEach { firebaseOrder ->
                        if (firebaseOrder.id == dataSnapshot.key!!) {
                            notDeliveredOrdersList.remove(firebaseOrder)
                            notDeliveredOrdersList.add(order)
                            getFirebaseOrderContents(order.id)
                        }
                    }
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val firebaseRemovedOrder: FirebaseOrder? =
                    dataSnapshot.getValue(FirebaseOrder::class.java)
                if (firebaseRemovedOrder != null) {
                    val order = FirebaseOrder(
                        firebaseRemovedOrder.finalPrice,
                        firebaseRemovedOrder.date,
                        firebaseRemovedOrder.orderStatus,
                        dataSnapshot.key!!
                    )
                    Log.d("TinaFirebase", "firebaseOrder onChildRemoved $order")
                    if (notDeliveredOrdersList.contains(order)) {
                        notDeliveredOrdersList.remove(order)
                    }
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun shouldGetOrderData(order: FirebaseOrder) =
        (order.orderStatus == OrderStatus.PENDING.toString()
                || order.orderStatus == OrderStatus.ORDERED.toString()
                || order.orderStatus == OrderStatus.CONFIRMED.toString())

    private fun getFirebaseOrderContents(fbOrderId: String) {
        val user: FirebaseUser? = Firebase.auth.currentUser
        if (user != null) {
            val uniqueId: String = user.uid
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val ref: DatabaseReference = database.getReference("OrderContent").child(uniqueId)
//            val productsQuery: Query = ref.orderByChild("orderId").equalTo(fbOrderId)
            ref.addValueEventListener(object : ValueEventListener {
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
                            if (currentFirebaseOrderContents.none { it.id == orderContent.id }) {

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
        products_recycler_view?.let { recyclerView ->
            recyclerView.layoutManager =
                LinearLayoutManager(requireContext())
            val pendingProductsAdapter = PendingProductsAdapter(
                requireContext()
            )
            recyclerView.adapter = pendingProductsAdapter

            // Observer on the LiveData
            // The onChanged() method fires when the observed data changes and the activity is
            // in the foreground.

//            viewModel.orders.observe(this, Observer { orders ->
//                // Update the cached copy of the products in the adapter.
//
//                val notDeliveredOrders = orders.filter {
//                    it.orderStatus == OrderStatus.PENDING.toString()
//                            || it.orderStatus == OrderStatus.ORDERED.toString()
//                            || it.orderStatus == OrderStatus.CONFIRMED.toString()
//                }
//
//                viewModel.pendingProducts.observe(this, Observer { pendingProducts ->
//
//                    val pendingOrderContents = mutableListOf<OrderContent>()
//                    pendingProducts.forEach { pendingProduct ->
//                        if (notDeliveredOrders.map { it.id }.contains(pendingProduct.orderId)) {
//                            pendingOrderContents.add(pendingProduct)
//                        }
//                    }
//
//                    viewModel.products.observe(this, Observer { products ->
//                        pendingProducts?.let {
//                            pendingProductsAdapter.setOrderContents(
//                                products.toList(),
//                                pendingOrderContents.toList(),
//                                notDeliveredOrders.toList()
//                            )
//                            setupEmptyView(empty_view_products, products_recycler_view)
//                        }
//                    })
//                })
//            })

            pendingProductsAdapter.setOrderContents(
                currentFirebaseOrderContents.toList(),
                notDeliveredOrdersList.toList()
            )
        }
    }
}


