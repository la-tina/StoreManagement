package com.example.android.storemanagement.create_order


import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.setFirebaseOrderContentData
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.setFirebaseOrderData
import com.example.android.storemanagement.firebase.FirebaseOrder
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_tab.OrderStatus
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_create_order.*
import kotlinx.android.synthetic.main.fragment_create_order.info_text
import kotlinx.android.synthetic.main.fragment_products_container.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

open class CreateOrderFragment : InfoOrderFragment() {

    override var fragmentTitle: String = "Create Order"
    override var buttonText: String = "Add Order"
    lateinit var order: Order
    protected var user: FirebaseUser? = null
    protected var firebaseProductsList = mutableListOf<FirebaseProduct>()

    private lateinit var orderContentViewModel: OrderContentViewModel

    override fun onStart() {
        super.onStart()

        Toast.makeText(context, "Enter at least 1 quantity to make an order.", Toast.LENGTH_LONG)
            .show()
        finalPrice = 0F
        final_price.text = "0"
        info_text?.text = context?.getString(R.string.create_order_info)

        button_add_order.setOnClickListener {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formattedDate = current.format(formatter)

            if (user == null) {
                order = Order(finalPrice, formattedDate, OrderStatus.PENDING.toString())
                Log.d("Tina", "final price created order $finalPrice")
                ordersViewModel.updateFinalPrice(finalPrice, order.id)
                ordersViewModel.insert(order, ::updateQuantities)
            } else {
                val firebaseOrder = FirebaseOrder(
                    finalPrice.toString(),
                    formattedDate,
                    OrderStatus.PENDING.toString(),
                    ""
                )
                val fbOrderId = setFirebaseOrderData(firebaseOrder)
                updateFirebaseQuantities(fbOrderId)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        user = Firebase.auth.currentUser
        if (user != null) {
            // Get a reference to our posts
            getFirebaseProducts()
        }
    }

    private fun getFirebaseProducts() {
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
                        if (firebaseProductsList.none { it.barcode == product.barcode }) {
                            firebaseProductsList.add(product)
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
                            setupRecyclerView()
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
                        setupRecyclerView()
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
                            setupRecyclerView()
                        }
                    }
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        setupRecyclerView()
    }

    private fun updateQuantities(orderId: Long) {
        val quantities = (create_order_recycler_view.adapter as CreateOrderAdapter).quantities
        quantities.forEach { (productName, quantity) ->
            if (quantity != 0) {
                Log.d("Tina", "updated quantity for " + productName + "is " + quantity)
                createOrderContent(productName, orderId, quantity)
            }
        }
        parentFragmentManager.popBackStackImmediate()
    }

    private fun updateFirebaseQuantities(firebaseOrderId: String) {
        val quantities = (create_order_recycler_view.adapter as CreateOrderAdapter).quantities
        quantities.forEach { (productName, quantity) ->
            if (quantity != 0) {
                createFirebaseOrderContent(productName, firebaseOrderId, quantity)
            }
        }
        parentFragmentManager.popBackStackImmediate()
    }

    private fun createOrderContent(
        productName: String,
        orderId: Long,
        currentQuantity: Int
    ) {

        val currentBarcode = productViewModel.allProducts.value
            ?.first { product -> product.name == productName }!!.barcode

        val orderContent = OrderContent(currentBarcode, orderId, currentQuantity)
        orderContentViewModel.insert(orderContent)
//        orderContentViewModel.getProductsInOrder(orderId)
    }

    private fun createFirebaseOrderContent(
        productName: String,
        fbOrderId: String,
        currentQuantity: Int
    ) {
        val currentOrderContent = firebaseProductsList.first { it.name == productName }

        val firebaseOrderContent = FirebaseOrderContent(
            currentOrderContent.barcode,
            productName,
            (currentOrderContent.price.toFloat() + currentOrderContent.overcharge.toFloat()).toString(),
            "",
            currentQuantity.toString(),
            fbOrderId,
            ""
        )
        setFirebaseOrderContentData(firebaseOrderContent, fbOrderId)
    }

    private fun updateQuantity(productName: String, quantity: Int) {
        val currentQuantity = productViewModel.allProducts.value
            ?.first { product -> product.name == productName }?.quantity

        Log.v("Room", "Updating current quantity for $productName $currentQuantity")

        val finalQuantity = if (currentQuantity != null) quantity + currentQuantity else quantity

        Log.v("Room", "Updating final quantity for $productName $finalQuantity")

//        productViewModel.updateQuantity(productName, finalQuantity)
        Log.d("Tina", "updated quantity for " + productName + "is " + quantity)
    }

    override fun setupRecyclerView() {
        create_order_recycler_view.layoutManager =
            LinearLayoutManager(requireContext())

        val createOrdersAdapter = CreateOrderAdapter(
            requireContext(),
            ::updateFinalPrice,
            ::setOrderButtonEnabled
        )
        create_order_recycler_view.adapter = createOrdersAdapter

        //ordersViewModel = ViewModelProviders.of(this).get(OrderViewModel::class.java)
        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        createOrdersAdapter.setProducts(firebaseProductsList)
        setupEmptyView(empty_view_create_order, create_order_recycler_view)
    }
}
