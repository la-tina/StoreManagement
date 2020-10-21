package com.example.android.storemanagement.edit_order

import android.util.Log
import androidx.lifecycle.Observer
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.ORDER_KEY
import com.example.android.storemanagement.VIEW_ORDER_KEY
import com.example.android.storemanagement.create_order.InfoOrderFragment
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.Order
import kotlinx.android.synthetic.main.fragment_create_order.*

class ViewOrderFragment : InfoOrderFragment() {

    override val fragmentTitle: String = "View Order"
    override val buttonText: String = "Ok"
    private lateinit var currentOrder: Order
    private var currentOrderContents: MutableList<OrderContent>? = null

    private val orderContentViewModel: OrderContentViewModel by lazy {
        ViewModelProviders.of(this).get(OrderContentViewModel(requireActivity().application)::class.java)
    }

    override fun onStart() {
        super.onStart()
        button_add_order.isEnabled = true
        Log.d("Koni", "viewOrder")
        orderContentViewModel.allOrderContents.observe(this, Observer { allContents ->
            val order: Order = arguments?.getSerializable(VIEW_ORDER_KEY) as Order
            setupRecyclerView()
            currentOrder = order

            Log.d("Tina", "final price edit order " + currentOrder.finalPrice)
            final_price.text = currentOrder.finalPrice.toString()

            allContents?.filter { it.orderId == order.id }?.forEach {orderContent ->
                currentOrderContents?.add(orderContent)
            }

//            if (currentOrder.deletedOrderContent.isNotEmpty()){
//                currentOrder.deletedOrderContent.forEach { deletedContent ->
//                    currentOrderContents?.add(deletedContent)
//                }
//            }

            setupRecyclerView()

            button_add_order.setOnClickListener {  parentFragmentManager.popBackStackImmediate() }
        })
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

        viewOrderAdapter.setProductsInOrder(currentOrderContents)
        setupEmptyView(empty_view_create_order, create_order_recycler_view)

        productViewModel.allProducts.observe(this, Observer { products ->
            products?.let {
                viewOrderAdapter.setProducts(it)
            }
        })
    }
}