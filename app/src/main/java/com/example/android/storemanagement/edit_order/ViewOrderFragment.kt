package com.example.android.storemanagement.edit_order

import android.util.Log
import androidx.lifecycle.Observer
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.ORDER_KEY
import com.example.android.storemanagement.R
import com.example.android.storemanagement.VIEW_ORDER_KEY
import com.example.android.storemanagement.create_order.InfoOrderFragment
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.Order
import kotlinx.android.synthetic.main.fragment_create_order.*

class ViewOrderFragment : InfoOrderFragment() {

    override var fragmentTitle: String = "View Order"
    override var buttonText: String = "Ok"
    private lateinit var currentOrder: Order
    private var currentOrderContents: MutableList<OrderContent>? = null

    private val orderContentViewModel: OrderContentViewModel by lazy {
        ViewModelProviders.of(this).get(OrderContentViewModel(requireActivity().application)::class.java)
    }

    override fun onStart() {
        super.onStart()
//        imageView.setImageDrawable("lemon.png")

        orderContentViewModel.allOrderContents.observe(this, Observer { viewState ->
            val order: Order = arguments?.getSerializable(ORDER_KEY) as Order
            currentOrder = order
            Log.d("Tina", "final price edit order " + currentOrder.finalPrice)
            final_price.isEnabled = false
            setupRecyclerView()
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
        setupEmptyView(empty_view_create_order, info_text, create_order_recycler_view)

        productViewModel.allProducts.observe(this, Observer { products ->
            products?.let {
                viewOrderAdapter.setProducts(it)
            }
        })
    }
}