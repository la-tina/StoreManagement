package com.example.android.storemanagement.orders_tab


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.CREATE_ORDER_TAB
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_database.OrderViewModel
import kotlinx.android.synthetic.main.fragment_title.*


class OrdersFragment : Fragment() {

    private val viewModel: OrderViewModel by lazy {
        ViewModelProviders.of(this).get(OrderViewModel::class.java)
    }

    lateinit var onNavigationChangedListener: OnNavigationChangedListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.example.android.storemanagement.R.layout.fragment_title, container, false)
    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        orders_add_button?.setOnClickListener {
            if (::onNavigationChangedListener.isInitialized)
                onNavigationChangedListener.onNavigationChanged(CREATE_ORDER_TAB)
        }
    }

    private fun deleteOrder(order: Order) {
        viewModel.deleteOrder(order)
    }

    private fun setupEmptyView() {
        val orders = orders_recycler_view.adapter!!
        if (orders.itemCount == 0) {
            orders_recycler_view.visibility = View.GONE
            empty_view_orders.visibility = View.VISIBLE
        } else {
            orders_recycler_view.visibility = View.VISIBLE
            empty_view_orders.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        orders_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        val ordersAdapter = OrdersAdapter(requireContext(), ::deleteOrder)
        orders_recycler_view.adapter = ordersAdapter

        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        viewModel.allOrders.observe(this, Observer { orders ->
            // Update the cached copy of the words in the adapter.
            orders?.let {
                ordersAdapter.setOrders(it)
                setupEmptyView()
            }
        })
    }
}



