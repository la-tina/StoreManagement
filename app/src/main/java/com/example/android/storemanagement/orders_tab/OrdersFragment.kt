package com.example.android.storemanagement.orders_tab


import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.*
import com.example.android.storemanagement.R
import com.example.android.storemanagement.Utils.getFormattedDate
import com.example.android.storemanagement.firebase.*
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.addFirebaseNotification
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.addFirebaseProduct
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.deleteFirebaseOrderData
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseOrderDate
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseOrderStatus
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseProductQuantity
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_database.OrderViewModel
import com.example.android.storemanagement.products_database.ProductViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_orders.*


open class OrdersFragment : Fragment() {

    var listener: OnNavigationChangedListener? = null
    private lateinit var topToolbar: Toolbar
    protected var user: FirebaseUser? = null

    //    var ordersList = mutableListOf<Order>()
    var firebaseOrdersList = mutableListOf<FirebaseOrder>()
    var firebaseOrderContentsList = mutableListOf<FirebaseOrderContent>()

    lateinit var productsInOrderList: List<OrderContent>
    var firebaseProductsList = mutableListOf<FirebaseProduct>()

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
        val view = inflater.inflate(
            R.layout.fragment_orders,
            container,
            false
        )
        topToolbar = view!!.findViewById(R.id.toolbarTop)
        topToolbar.inflateMenu(R.menu.orders_filter_menu)
        topToolbar.overflowIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_baseline_filter_alt)
        return view
    }

    override fun onStart() {
        super.onStart()
        setHasOptionsMenu(true)
        topToolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
            when (item.itemId) {
                R.id.new_old_date ->
                    filterByNewDate()
                R.id.old_new_date ->
                    filterByOldDate()
                R.id.pending ->
                    filterPendingOrders()
                R.id.ordered ->
                    filterOrderedOrders()
                R.id.confirmed ->
                    filterConfirmedOrders()
                R.id.delivered ->
                    filterDeliveredOrders()
                R.id.final_price_ascending ->
                    filterByAscendingFinalPrice()
                R.id.final_price_descending ->
                    filterByDescendingFinalPrice()
            }
            true
        })
    }

    private fun filterByNewDate() {
        val newDateComparator = compareByDescending<FirebaseOrder> { it.date }
        val sortedOrdersList = firebaseOrdersList.sortedWith(newDateComparator)
        setupRecyclerView(sortedOrdersList)
    }

    private fun filterByOldDate() {
        val oldDateComparator = compareBy<FirebaseOrder> { it.date }
        val sortedOrdersList = firebaseOrdersList.sortedWith(oldDateComparator)
        setupRecyclerView(sortedOrdersList)
    }

    private fun filterPendingOrders() {
        val pendingOrders = firebaseOrdersList.filter { it.orderStatus == OrderStatus.PENDING.toString() }
        setupRecyclerView(pendingOrders)
    }

    private fun filterOrderedOrders() {
        val pendingOrders = firebaseOrdersList.filter { it.orderStatus == OrderStatus.ORDERED.toString() }
        setupRecyclerView(pendingOrders)
    }

    private fun filterConfirmedOrders() {
        val pendingOrders = firebaseOrdersList.filter { it.orderStatus == OrderStatus.CONFIRMED.toString() }
        setupRecyclerView(pendingOrders)
    }

    private fun filterDeliveredOrders() {
        val pendingOrders = firebaseOrdersList.filter { it.orderStatus == OrderStatus.DELIVERED.toString() }
        setupRecyclerView(pendingOrders)
    }

    private fun filterByAscendingFinalPrice() {
        val finalPriceComparator = compareBy<FirebaseOrder> { it.finalPrice.toFloat() }
        val sortedOrdersList = firebaseOrdersList.sortedWith(finalPriceComparator)
        setupRecyclerView(sortedOrdersList)
    }

    private fun filterByDescendingFinalPrice() {
        val finalPriceComparator = compareByDescending<FirebaseOrder> { it.finalPrice.toFloat() }
        val sortedOrdersList = firebaseOrdersList.sortedWith(finalPriceComparator)
        setupRecyclerView(sortedOrdersList)
    }

    override fun onResume() {
        super.onResume()
        orders_add_button?.setOnClickListener {
            if (user == null) {
                val dialog = AlertDialog.Builder(context!!, R.style.AlertDialog)
                    .setTitle(R.string.info)
                    .setMessage(R.string.no_user_info)
                    .setPositiveButton(R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                    }.show()

                val textView = dialog.findViewById<View>(android.R.id.message) as TextView
                textView.textSize = 17f
                textView.setTextColor(ContextCompat.getColor(context!!, R.color.darkBarColor))
            } else {
                if (::onNavigationChangedListener.isInitialized)
                    onNavigationChangedListener.onNavigationChanged(USERS_TAB)
            }
        }

        // Get a reference to our posts
        user = Firebase.auth.currentUser
        getFirebaseOrders()
        getFirebaseProducts()
        getFirebaseOrderContents()
        setupRecyclerView(firebaseOrdersList)
    }

    private fun getFirebaseOrderContents() {
        user = Firebase.auth.currentUser
        if (user != null) {
            // Get a reference to our posts
            val uniqueId: String = user?.uid!!
            val database = FirebaseDatabase.getInstance()
            val ref: DatabaseReference = database.getReference("OrderContent").child(uniqueId)

            // Attach a listener to read the data at our posts reference
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    firebaseOrderContentsList.clear()
                    val firebaseOrderContents = dataSnapshot.children

                    for (item in firebaseOrderContents) {
                        val firebaseOrderContent: FirebaseOrderContent? =
                            item.getValue(FirebaseOrderContent::class.java)

                        if (firebaseOrderContent != null) {
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
                            if (firebaseOrderContentsList.none { it.id == orderContent.id }) {
                                firebaseOrderContentsList.add(orderContent)
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

            ref.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                    val firebaseNewOrderContent: FirebaseOrderContent? =
                        dataSnapshot.getValue(FirebaseOrderContent::class.java)
                    if (firebaseNewOrderContent != null) {
                        val orderContent = FirebaseOrderContent(
                            firebaseNewOrderContent.productBarcode,
                            firebaseNewOrderContent.productName,
                            firebaseNewOrderContent.productPrice,
                            firebaseNewOrderContent.productOvercharge,
                            firebaseNewOrderContent.quantity,
                            firebaseNewOrderContent.orderId,
                            firebaseNewOrderContent.userId,
                            dataSnapshot.key!!
                        )
                        if (firebaseOrderContentsList.none { it.id == orderContent.id }) {
                            firebaseOrderContentsList.add(orderContent)
                        }
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                    val firebaseChangedOrderContent: FirebaseOrderContent? =
                        dataSnapshot.getValue(FirebaseOrderContent::class.java)
                    if (firebaseChangedOrderContent != null) {
                        val orderContent = FirebaseOrderContent(
                            firebaseChangedOrderContent.productBarcode,
                            firebaseChangedOrderContent.productName,
                            firebaseChangedOrderContent.productPrice,
                            firebaseChangedOrderContent.productOvercharge,
                            firebaseChangedOrderContent.quantity,
                            firebaseChangedOrderContent.orderId,
                            firebaseChangedOrderContent.userId,
                            dataSnapshot.key!!
                        )
                        val changedOrderContent =
                            firebaseOrderContentsList.first { t -> t.id == dataSnapshot.key!! }
                        firebaseOrderContentsList.remove(changedOrderContent)
                        firebaseOrderContentsList.add(orderContent)
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    val firebaseRemovedOrderContent: FirebaseOrderContent? =
                        dataSnapshot.getValue(FirebaseOrderContent::class.java)
                    if (firebaseRemovedOrderContent != null) {
                        val orderContent = FirebaseOrderContent(
                            firebaseRemovedOrderContent.productBarcode,
                            firebaseRemovedOrderContent.productName,
                            firebaseRemovedOrderContent.productPrice,
                            firebaseRemovedOrderContent.productOvercharge,
                            firebaseRemovedOrderContent.quantity,
                            firebaseRemovedOrderContent.orderId,
                            firebaseRemovedOrderContent.userId,
                            dataSnapshot.key!!
                        )
                        if (!firebaseProductsList.none { it.id == orderContent.id }) {
                            firebaseProductsList.remove(firebaseProductsList.first { it.id == orderContent.id })
                        }
                    }
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    private fun updateDeliveredProducts(fbOrderId: String) {
        if (user != null) {
            val uniqueId: String = user?.uid!!
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val ref: DatabaseReference = database.getReference("OrderContent")
            val orderContentsQuery: Query =
                ref.child(uniqueId).orderByChild("orderId").equalTo(fbOrderId)
            // Attach a listener to read the data at our posts reference
            orderContentsQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val firebaseOrderContents = dataSnapshot.children

                    for (item in firebaseOrderContents) {
                        val firebaseOrderContent: FirebaseOrderContent? =
                            item.getValue(FirebaseOrderContent::class.java)

                        if (firebaseOrderContent != null) {
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
                            if (firebaseProductsList.none { it.barcode == orderContent.productBarcode }) {
                                val firebaseProduct = FirebaseProduct(
                                    orderContent.productName,
                                    orderContent.productPrice,
                                    orderContent.productOvercharge,
                                    orderContent.productBarcode,
                                    orderContent.quantity,
                                    ""
                                )
                                addFirebaseProduct(firebaseProduct)
                            } else {
                                updateProductQuantity(orderContent.productBarcode, orderContent.quantity.toInt())
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    fun updateProductQuantity(productBarcode: String, quantityToBeAdded: Int) {
        val productsToBeChanged =
            firebaseProductsList.filter { it.barcode == productBarcode }
        productsToBeChanged.forEach { firebaseProducts ->
            val newQuantity =
                (quantityToBeAdded + firebaseProducts.quantity.toInt()).toString()
            updateFirebaseProductQuantity(productBarcode, newQuantity)
        }
    }

    open fun addRowOrders(writeRow: (List<Any?>) -> Unit) {
        firebaseOrdersList.forEach { order ->
            firebaseOrderContentsList.filter { it.orderId == order.id }.forEach { orderContent ->
                writeRow(
                    listOf(
                        "",
                        order.id,
                        order.orderStatus,
                        order.date,
                        orderContent.productName,
                        orderContent.quantity,
                        orderContent.productPrice.toFloat() * orderContent.quantity.toInt()
                    )
                )
            }

            writeRow(listOf("Final price", " - ", " - ", " - ", " - ", " - ", order.finalPrice))
            writeRow(emptyList())
        }
    }

    open fun addRowProducts(writeRow: (List<Any?>) -> Unit) {
        firebaseProductsList.forEach { product ->
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

    private fun getFirebaseProducts() {
        user = Firebase.auth.currentUser
        if (user != null) {
            // Get a reference to our posts
            val uniqueId: String = user?.uid!!
            val database = FirebaseDatabase.getInstance()
            val ref: DatabaseReference = database.getReference("Products").child(uniqueId)

            // Attach a listener to read the data at our posts reference
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    firebaseProductsList.clear()
                    val firebaseProducts = dataSnapshot.children

                    for (item in firebaseProducts) {
                        val firebaseProduct: FirebaseProduct? =
                            item.getValue(FirebaseProduct::class.java)

                        if (firebaseProduct != null) {
                            val product = FirebaseProduct(
                                firebaseProduct.name,
                                firebaseProduct.price,
                                firebaseProduct.overcharge,
                                firebaseProduct.barcode,
                                firebaseProduct.quantity,
                                item.key!!
                            )
                            Log.d("TinaFirebase", "firebaseProduct onDataChange $product")
                            if (!firebaseProductsList.contains(product)) {
                                firebaseProductsList.add(product)
                                activity?.runOnUiThread {
                                    setupRecyclerView(firebaseOrdersList)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

            ref.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                    val firebaseNewProduct: FirebaseProduct? =
                        dataSnapshot.getValue(FirebaseProduct::class.java)
                    if (firebaseNewProduct != null) {
                        val product = FirebaseProduct(
                            firebaseNewProduct.name,
                            firebaseNewProduct.price,
                            firebaseNewProduct.overcharge,
                            firebaseNewProduct.barcode,
                            firebaseNewProduct.quantity,
                            dataSnapshot.key!!
                        )
                        Log.d("TinaFirebase", "firebaseProduct onChildAdded $product")
                        if (firebaseProductsList.none { it.barcode == product.barcode }) {
                            firebaseProductsList.add(product)
                            activity?.runOnUiThread {
                                setupRecyclerView(firebaseOrdersList)
                            }
                        }
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                    val changedFirebaseProduct: FirebaseProduct? =
                        dataSnapshot.getValue(FirebaseProduct::class.java)
                    if (changedFirebaseProduct != null) {
                        val product = FirebaseProduct(
                            changedFirebaseProduct.name,
                            changedFirebaseProduct.price,
                            changedFirebaseProduct.overcharge,
                            changedFirebaseProduct.barcode,
                            changedFirebaseProduct.quantity,
                            dataSnapshot.key!!
                        )
                        Log.d("TinaFirebase", "firebaseProduct onChildAdded $product")
                        val changedProduct =
                            firebaseProductsList.first { t -> t.id == dataSnapshot.key!! }
                        firebaseProductsList.remove(changedProduct)
                        firebaseProductsList.add(product)
                        activity?.runOnUiThread {
                            setupRecyclerView(firebaseOrdersList)
                        }
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    val firebaseRemovedProduct: FirebaseProduct? =
                        dataSnapshot.getValue(FirebaseProduct::class.java)
                    if (firebaseRemovedProduct != null) {
                        val product = FirebaseProduct(
                            firebaseRemovedProduct.name,
                            firebaseRemovedProduct.price,
                            firebaseRemovedProduct.overcharge,
                            firebaseRemovedProduct.barcode,
                            firebaseRemovedProduct.quantity,
                            dataSnapshot.key!!
                        )
                        Log.d("TinaFirebase", "firebaseProduct onChildRemoved $product")
                        if (!firebaseProductsList.none { it.barcode == product.barcode }) {
                            firebaseProductsList.remove(product)
                            activity?.runOnUiThread {
                                setupRecyclerView(firebaseOrdersList)
                            }
                        }
                    }
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    private fun getFirebaseOrders() {
        val uniqueId: String = user?.uid!!
        val database = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("Orders").child(uniqueId)

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                firebaseOrdersList.clear()
                val firebaseOrders = dataSnapshot.children

                for (item in firebaseOrders) {
                    val firebaseOrder: FirebaseOrder? = item.getValue(FirebaseOrder::class.java)

                    if (firebaseOrder != null) {
                        val order = FirebaseOrder(
                            firebaseOrder.finalPrice,
                            firebaseOrder.date,
                            firebaseOrder.orderStatus,
                            item.key!!,
                            firebaseOrder.userId
                        )

                        Log.d("TinaFirebase", "firebaseOrder onDataChange $order")
                        if (!firebaseOrdersList.contains(order)) {
                            firebaseOrdersList.add(order)
                            activity?.runOnUiThread {
                                setupRecyclerView(firebaseOrdersList)
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
                        dataSnapshot.key!!,
                        firebaseNewOrder.userId
                    )

                    Log.d("TinaFirebase", "firebaseOrder onChildAdded $order")
                    if (!firebaseOrdersList.contains(order)) {
                        firebaseOrdersList.add(order)
                        activity?.runOnUiThread {
                            setupRecyclerView(firebaseOrdersList)
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
                        dataSnapshot.key!!,
                        changedFirebaseOrder.userId
                    )

                    Log.d("TinaFirebase", "firebaseOrder onChildAdded $order")
                    if (firebaseOrdersList.contains(order)) {
                        firebaseOrdersList.forEach { firebaseOrder ->
                            if (firebaseOrder.id == dataSnapshot.key!!) {
                                firebaseOrdersList.remove(firebaseOrder)
                                firebaseOrdersList.add(order)
                            }
                        }
                    }
                    activity?.runOnUiThread {
                        setupRecyclerView(firebaseOrdersList)
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
                        dataSnapshot.key!!,
                        firebaseRemovedOrder.userId
                    )

                    Log.d("TinaFirebase", "firebaseOrder onChildRemoved $order")
                    if (firebaseOrdersList.contains(order)) {
                        firebaseOrdersList.remove(order)
                        activity?.runOnUiThread {
                            setupRecyclerView(firebaseOrdersList)
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

        orderViewModel.updateDate(formattedDate, id)
        updateFirebaseOrderDate(id.toString(), formattedDate)
    }

    private fun updateFirebaseDate(firebaseId: String) {
        val formattedDate = getFormattedDate()
        updateFirebaseOrderDate(firebaseId, formattedDate)
    }

    private fun onOrderStatusChanged(
        order: Order?,
        firebaseOrder: FirebaseOrder?,
        orderStatus: OrderStatus
    ) {
        if (firebaseOrder != null) {
            updateFirebaseDate(firebaseOrder.id)
            updateFirebaseOrderStatus(firebaseOrder.id, orderStatus.toString())

            when (orderStatus) {
                OrderStatus.ORDERED -> {
                    Log.d("Koni", "onOrderOrdered")
                    val firebaseNotification = FirebaseNotification(firebaseOrder.id, user?.uid!!, true.toString(), false.toString())
                    addFirebaseNotification(firebaseNotification, firebaseOrder.userId)
                }
                OrderStatus.DELIVERED -> {
                    Log.d("Koni", "onOrderDelivered")

                    updateDeliveredProducts(firebaseOrder.id)
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

//    private fun updateProductQuantity(orderContent: OrderContent) {
//        Log.d("Koni", "updateProductQuantity invoked")
//        productsList.forEach { product ->
//            Log.d("Koni", "allProducts?.forEach invoked")
//            if (orderContent.productBarcode == product.barcode) {
//                val newQuantity = product.quantity + orderContent.quantity
//                //Log.d("Koni", "productViewModel.updateProductQuantity invoked")
//                productViewModel.updateProductQuantity(product.barcode, newQuantity)
//                Log.d("Koni", "updated quantity $newQuantity")
//
//            }
//        }
//    }

    private fun openViewOrderTab(order: Order) {
        if (::onNavigationChangedListener.isInitialized) {
            //onNavigationChangedListener.onNavigationChanged(EDIT_ORDER_TAB)
            listener = onNavigationChangedListener
        }

        listener?.onNavigationChanged(tabNumber = VIEW_ORDER_TAB, order = order)
    }

    private fun openEditOrderTab(order: Order?, firebaseOrder: FirebaseOrder?) {
        FirebaseDatabaseOperations.getFirebaseUser(user!!.uid) { user -> openEditOrderFragment(user, firebaseOrder) }
    }

    private fun openEditOrderFragment(userInternal: FirebaseUserInternal, firebaseOrder: FirebaseOrder?) {
        if (::onNavigationChangedListener.isInitialized) {
            //onNavigationChangedListener.onNavigationChanged(EDIT_ORDER_TAB)
            listener = onNavigationChangedListener
        }

        listener?.onNavigationChanged(
            tabNumber = EDIT_ORDER_TAB,
            firebaseOrder = firebaseOrder,
            firebaseUser = userInternal
        )
    }

    private fun setupEmptyView() {
        val orders = orders_recycler_view?.adapter
        if (orders?.itemCount == 0) {
            orders_recycler_view?.visibility = View.GONE
            empty_view_orders?.visibility = View.VISIBLE
        } else {
            orders_recycler_view?.visibility = View.VISIBLE
            empty_view_orders?.visibility = View.GONE
        }
    }

    private fun setupRecyclerView(firebaseOrders: List<FirebaseOrder>) {
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

        activity?.runOnUiThread {
            ordersAdapter.setOrders(null, firebaseOrders)
            setupEmptyView()
        }
    }
}
