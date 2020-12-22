package com.example.android.storemanagement.orders_tab


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.*
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.deleteFirebaseOrderContentData
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.deleteFirebaseOrderData
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseOrderDate
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseOrderStatus
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseProductQuantity
import com.example.android.storemanagement.firebase.FirebaseOrder
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_database.OrderViewModel
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_database.ProductViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_orders.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


open class OrdersFragment : Fragment() {

    var listener: OnNavigationChangedListener? = null
    private lateinit var topToolbar: Toolbar

    var ordersList: MutableList<Order> = mutableListOf()
    var firebaseOrdersList: MutableList<FirebaseOrder> = mutableListOf()

    lateinit var productsInOrderList: List<OrderContent>
    lateinit var productsList: List<Product>

    private val orderViewModel: OrderViewModel by lazy {
        ViewModelProviders.of(this).get(OrderViewModel::class.java)
    }

    private val orderContentViewModel: OrderContentViewModel by lazy {
        ViewModelProviders.of(this).get(OrderContentViewModel::class.java)
    }

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel::class.java)
    }

    lateinit var onNavigationChangedListener: OnNavigationChangedListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_orders,
            container,
            false
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    private fun initData() {
        orderViewModel.allOrders.toSingleEvent().observe(this,
            Observer<List<Order>> { orders ->
                ordersList = orders as MutableList<Order>
            }
        )
        orderContentViewModel.allOrderContents.toSingleEvent().observe(this,
            Observer<List<OrderContent>> { productsInOrder ->
                productsInOrderList = productsInOrder
            }
        )
        productViewModel.allProducts.toSingleEvent().observe(this,
            Observer<List<Product>> { products ->
                productsList = products
            }
        )
    }

    open fun addRowOrders(writeRow: (List<Any?>) -> Unit) {
        ordersList.forEach { order ->
            val orderContents = productsInOrderList.filter { it.orderId == order.id }
            orderContents.forEach { orderContent ->
                val products = productsList.filter { it.barcode == orderContent.productBarcode }
                products.forEach { product ->
                    writeRow(
                        listOf(
                            "",
                            order.id,
                            order.orderStatus,
                            order.date,
                            product.name,
                            orderContent.quantity,
                            product.price * orderContent.quantity
                        )
                    )
                }
            }
            writeRow(listOf("Final price", " - ", " - ", " - ", " - ", " - ", order.finalPrice))
            writeRow(emptyList())
        }
    }

    open fun addRowProducts(writeRow: (List<Any?>) -> Unit) {
        productsList.forEach { product ->
            writeRow(
                listOf(
                    product.barcode,
                    product.name,
                    product.price,
                    product.overcharge,
                    product.quantity
                )
            )
        }
    }

//    override fun onStart() {
//        super.onStart()
//        setupRecyclerView()
//    }

    override fun onResume() {
        super.onResume()
        orders_add_button?.setOnClickListener {
            if (::onNavigationChangedListener.isInitialized)
                onNavigationChangedListener.onNavigationChanged(CREATE_ORDER_TAB)
        }

        // Get a reference to our posts
        val user: FirebaseUser? = Firebase.auth.currentUser
        val uniqueId: String = user?.uid!!
        val database = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("Orders").child(uniqueId)

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                ordersList.clear()
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
                        if (!firebaseOrdersList.contains(order)) {
                            firebaseOrdersList.add(order)
                            activity?.runOnUiThread {
                                setupRecyclerView()
                            }
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
                    if (!firebaseOrdersList.contains(order)) {
                        firebaseOrdersList.add(order)
                        activity?.runOnUiThread {
                            setupRecyclerView()
                        }
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
                    val changedOrder = firebaseOrdersList.first { t -> t.id == dataSnapshot.key!! }
                    firebaseOrdersList.remove(changedOrder)
                    firebaseOrdersList.add(order)
                    activity?.runOnUiThread {
                        setupRecyclerView()
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
                    if (firebaseOrdersList.contains(order)) {
                        firebaseOrdersList.remove(order)
                        activity?.runOnUiThread {
                            setupRecyclerView()
                        }
                    }
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun updateDate(id: Long) {
        val formattedDate = getFormattedDate()

        if (formattedDate != null) {
            orderViewModel.updateDate(formattedDate, id)
        }
        updateFirebaseOrderDate(id.toString(), formattedDate.toString())
    }

    private fun updateFirebaseDate(firebaseId: String) {
        val formattedDate = getFormattedDate()
        updateFirebaseOrderDate(firebaseId, formattedDate.toString())
    }

    private fun getFormattedDate(): String? {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return current.format(formatter)
    }

    private fun onOrderStatusChanged(
        order: Order?,
        firebaseOrder: FirebaseOrder?,
        orderStatus: OrderStatus
    ) {
        if (order != null) {
            updateDate(order.id)
            Log.d("Koni", " orderStatus.toString() $orderStatus")
            orderViewModel.onOrderStatusChanged(order.id, orderStatus.toString())

            when (orderStatus) {
                OrderStatus.ORDERED -> {
                    Log.d("Koni", "onOrderOrdered")
                }
                OrderStatus.DELIVERED -> {
                    Log.d("Koni", "onOrderDelivered")
                    productsInOrderList.filter { it.orderId == order.id }
                        .forEach { currentProduct ->
                            Log.d(
                                "Koni",
                                "updated quantities " + currentProduct.productBarcode + " quantity " + currentProduct.quantity
                            )
                            updateProductQuantity(currentProduct)
                        }
                }
            }
        } else if (firebaseOrder != null) {
            updateFirebaseDate(firebaseOrder.id)
            updateFirebaseOrderStatus(firebaseOrder.id, orderStatus.toString())

            when (orderStatus) {
                OrderStatus.ORDERED -> {
                    Log.d("Koni", "onOrderOrdered")
                }
                OrderStatus.DELIVERED -> {
                    Log.d("Koni", "onOrderDelivered")
//                    productsInOrderList.filter { it.orderId == order.id }.forEach { currentProduct ->
//                        Log.d(
//                            "Koni",
//                            "updated quantities " + currentProduct.productBarcode + " quantity " + currentProduct.quantity
//                        )
//                        updateProductQuantity(currentProduct)
//                    }
                }
            }
        }
    }

    private fun deleteOrder(order: Order?, firebaseOrder: FirebaseOrder?) {
        Log.d("Koni", "deleteOrder invoked")
//        if (!order.isOrdered){
//            productsInOrderList.filter { it.orderId == order.id }.forEach { currentProduct ->
//                updateProductQuantity(currentProduct)
//                Log.d("T", "updated quantities")
//            }
//        }
        if (order != null) {
            orderViewModel.deleteOrder(order)
        } else if (firebaseOrder != null) {
            deleteFirebaseOrderData(firebaseOrder.id)
        }
    }

    //on Edit order
//    private fun updateProductQuantity(orderContent: OrderContent) {
//        Log.d("Koni", "updateProductQuantity invoked")
//        productsList.forEach { product ->
//            Log.d("Koni", "allProducts?.forEach invoked")
//            if (orderContent.productBarcode == product.barcode) {
//                val newQuantity = product.quantity - orderContent.quantity
//                val productQuantity = if (newQuantity >= 0) newQuantity else 0
//                //Log.d("Koni", "productViewModel.updateProductQuantity invoked")
//                productViewModel.updateProductQuantity(product.barcode, productQuantity)
//                Log.d("Koni", "updated quantity $productQuantity")
//
//            }
//        }
//    }

    private fun updateProductQuantity(orderContent: OrderContent) {
        Log.d("Koni", "updateProductQuantity invoked")
        productsList.forEach { product ->
            Log.d("Koni", "allProducts?.forEach invoked")
            if (orderContent.productBarcode == product.barcode) {
                val newQuantity = product.quantity + orderContent.quantity
                //Log.d("Koni", "productViewModel.updateProductQuantity invoked")
                productViewModel.updateProductQuantity(product.barcode, newQuantity)
                updateFirebaseProductQuantity(product, newQuantity.toString())
                Log.d("Koni", "updated quantity $newQuantity")

            }
        }
    }

    private fun openViewOrderTab(order: Order) {
        if (::onNavigationChangedListener.isInitialized) {
            //onNavigationChangedListener.onNavigationChanged(EDIT_ORDER_TAB)
            listener = onNavigationChangedListener
        }

        listener?.onNavigationChanged(tabNumber = VIEW_ORDER_TAB, order = order)
    }

    private fun openEditOrderTab(order: Order?, firebaseOrder: FirebaseOrder?) {
//        if (order.orderStatus == OrderStatus.ORDERED.toString()
//            || order.orderStatus == OrderStatus.CONFIRMED.toString()
//            || order.orderStatus == OrderStatus.DELIVERED.toString()) {
//            openViewOrderTab(order)
//        } else {
        if (::onNavigationChangedListener.isInitialized) {
            //onNavigationChangedListener.onNavigationChanged(EDIT_ORDER_TAB)
            listener = onNavigationChangedListener
        }

        listener?.onNavigationChanged(
            tabNumber = EDIT_ORDER_TAB,
            order = order,
            firebaseOrder = firebaseOrder
        )
//        }
    }

    private fun setupEmptyView() {
        if (firebaseOrdersList?.size == 0) {
            orders_recycler_view?.visibility = View.GONE
            empty_view_orders?.visibility = View.VISIBLE
        } else {
            orders_recycler_view?.visibility = View.VISIBLE
            empty_view_orders?.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        orders_recycler_view?.layoutManager =
            LinearLayoutManager(requireContext())
        val ordersAdapter = OrdersAdapter(
            requireContext(),
            ::deleteOrder,
            ::openEditOrderTab,
            ::onOrderStatusChanged
        )
        orders_recycler_view?.adapter = ordersAdapter

        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

//        orderViewModel.allOrders.observe(this, Observer { orders ->
//            // Update the cached copy of the words in the adapter.
//            orders?.let {
//                ordersAdapter.setOrders(it)
//                setupEmptyView()
//            }
//        })

        ordersAdapter.setOrders(null, firebaseOrdersList)
        setupEmptyView()
    }
}
