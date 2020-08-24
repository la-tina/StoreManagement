package com.example.android.storemanagement.create_order


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_database.OrderViewModel
import kotlinx.android.synthetic.main.fragment_create_order.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

open class CreateOrderFragment : InfoOrderFragment() {

    override val fragmentTitle: String = "Create Order"
    override val buttonText: String = "Add Order"

    private lateinit var orderContentViewModel: OrderContentViewModel

    override fun onStart() {
        super.onStart()
        setupRecyclerView()

        Toast.makeText(context, "You must enter at least 1 quantity!", Toast.LENGTH_LONG).show()

        button_add_order.setOnClickListener {

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formattedDate = current.format(formatter)

            val order = Order(finalPrice, formattedDate)
            ordersViewModel.insert(order, ::updateQuantities)
        }
    }

    private fun updateQuantities(orderId: Long) {
        val quantities = (create_order_recycler_view.adapter as CreateOrderAdapter).quantities
        quantities.forEach { (productName, quantity) ->
            updateQuantity(productName, quantity)
            createOrderContent(productName, orderId, quantity)
        }
        fragmentManager?.popBackStackImmediate()
    }

    private fun createOrderContent(productName: String, orderId: Long, currentQuantity: Int) {

        val currentBarcode = productViewModel.allProducts.value
            ?.first { product -> product.name == productName }!!.barcode

        val orderContent = OrderContent(currentBarcode, orderId, currentQuantity)
        orderContentViewModel.insert(orderContent)
//        orderContentViewModel.getProductsInOrder(orderId)
    }

    private fun updateQuantity(productName: String, quantity: Int) {
        val currentQuantity = productViewModel.allProducts.value
            ?.first { product -> product.name == productName }?.quantity

        Log.v("Room", "Updating current quantity for $productName $currentQuantity")

        val finalQuantity = if (currentQuantity != null) quantity + currentQuantity else quantity

        Log.v("Room", "Updating final quantity for $productName $finalQuantity")

        productViewModel.updateQuantity(productName, finalQuantity)
    }

    override fun setupRecyclerView() {
        create_order_recycler_view.layoutManager = LinearLayoutManager(requireContext())

        val createOrdersAdapter = CreateOrderAdapter(
            requireContext(),
            ::updateFinalPrice,
            ::setOrderButtonEnabled
        )
        create_order_recycler_view.adapter = createOrdersAdapter

        //ordersViewModel = ViewModelProviders.of(this).get(OrderViewModel::class.java)

        orderContentViewModel = ViewModelProviders.of(this).get(OrderContentViewModel::class.java)
        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        productViewModel.allProducts.observe(this, Observer { products ->
            // Update the cached copy of the products in the adapter.
            products?.let {
                createOrdersAdapter.setProducts(it)
                setupEmptyView(empty_view_create_order, create_order_recycler_view)
            }
        })
    }
}