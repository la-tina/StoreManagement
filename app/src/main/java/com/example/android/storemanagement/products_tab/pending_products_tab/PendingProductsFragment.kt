package com.example.android.storemanagement.products_tab.pending_products_tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.R
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.orders_tab.OrderStatus
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsTabFragment
import kotlinx.android.synthetic.main.fragment_products_container.*


class PendingProductsFragment : ProductsTabFragment() {

    private lateinit var viewModel: PendingProductsViewModel

    override fun setupViewModel() {
        viewModel =
            ViewModelProviders.of(this)
                .get(PendingProductsViewModel(requireActivity().application)::class.java)
    }

    override fun setOnNavigationChangedListener(onNavigationChangedListener: OnNavigationChangedListener) {
        listener = onNavigationChangedListener
    }

    override fun deleteProduct(product: Product) {
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

            viewModel.orders.observe(this, Observer { orders ->
                // Update the cached copy of the products in the adapter.

                val notDeliveredOrders = orders.filter {
                    it.orderStatus == OrderStatus.PENDING.toString()
                            || it.orderStatus == OrderStatus.ORDERED.toString()
                            || it.orderStatus == OrderStatus.CONFIRMED.toString()
                }

                viewModel.pendingProducts.observe(this, Observer { pendingProducts ->

                    val pendingOrderContents = mutableListOf<OrderContent>()
                    pendingProducts.forEach { pendingProduct ->
                        if (notDeliveredOrders.map { it.id }.contains(pendingProduct.orderId)) {
                            pendingOrderContents.add(pendingProduct)
                        }
                    }

                    viewModel.products.observe(this, Observer { products ->
                        pendingProducts?.let {
                            pendingProductsAdapter.setOrderContents(
                                products.toList(),
                                pendingOrderContents.toList(),
                                notDeliveredOrders.toList()
                            )
                            setupEmptyView(empty_view_products, products_recycler_view)
                        }
                    })
                })
            })
        }
    }
}


