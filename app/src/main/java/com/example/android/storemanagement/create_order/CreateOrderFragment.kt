package com.example.android.storemanagement.create_order


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.R
import com.example.android.storemanagement.USER_KEY
import com.example.android.storemanagement.Utils.getFormattedDate
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.setFirebaseOrderContentData
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.setFirebaseOrderData
import com.example.android.storemanagement.firebase.FirebaseOrder
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.firebase.FirebaseUserInternal
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_tab.OrderStatus
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_create_order.*

open class CreateOrderFragment : InfoOrderFragment() {

    override var fragmentTitle: String = "Create Order"
    override var buttonText: String = "Add Order"
    lateinit var order: Order
    protected lateinit var user: FirebaseUser
    protected lateinit var firebaseUser: FirebaseUserInternal

    private lateinit var orderContentViewModel: OrderContentViewModel

    override fun onStart() {
        super.onStart()
        firebaseUser = arguments?.getSerializable(USER_KEY) as FirebaseUserInternal
        Toast.makeText(context, "Enter at least 1 quantity to make an order.", Toast.LENGTH_SHORT)
            .show()
        finalPrice = 0F
        final_price.text = "0"
        info_text?.text = context?.getString(R.string.create_order_info)

        button_add_order.setOnClickListener {

//            if (user == null) {
//                order = Order(finalPrice, formattedDate, OrderStatus.PENDING.toString())
//                Log.d("Tina", "final price created order $finalPrice")
//                ordersViewModel.updateFinalPrice(finalPrice, order.id)
//                ordersViewModel.insert(order, ::updateQuantities)
//            } else {
            val firebaseOrder = FirebaseOrder(
                finalPrice.toString(),
                getFormattedDate(),
                OrderStatus.PENDING.toString(),
                "",
                firebaseUser.id
            )
            val fbOrderId = setFirebaseOrderData(firebaseOrder)
            updateFirebaseQuantities(fbOrderId)

//            }
        }
    }

    override fun onResume() {
        super.onResume()
        firebaseUser = arguments?.getSerializable(USER_KEY) as FirebaseUserInternal

        user_info?.text = firebaseUser.name.plus(context?.resources?.getString(R.string.user_products_info))
        user = Firebase.auth.currentUser!!
        // Get a reference to our posts
        getFirebaseProducts(firebaseUser.id)
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
        val overcharge = if (currentOrderContent.overcharge.isBlank()) 0F else currentOrderContent.overcharge.toFloat()
        val firebaseOrderContent = FirebaseOrderContent(
            currentOrderContent.barcode,
            productName,
            (currentOrderContent.price.toFloat() + overcharge).toString(),
            "0",
            currentQuantity.toString(),
            fbOrderId,
            firebaseUser.id,
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
            ::setOrderButtonEnabled,
            firebaseUser
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
