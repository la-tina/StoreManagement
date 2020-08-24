package com.example.android.storemanagement.orders_tab


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.CREATE_ORDER_TAB
import com.example.android.storemanagement.EDIT_ORDER_TAB
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_database.OrderViewModel
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_database.ProductViewModel
import com.example.android.storemanagement.toSingleEvent
import kotlinx.android.synthetic.main.fragment_title.*


open class OrdersFragment : Fragment() {

    var listener: OnNavigationChangedListener? = null

    private val viewModel: OrderViewModel by lazy {
        ViewModelProviders.of(this).get(OrderViewModel::class.java)
    }

    private val orderContentViewModel: OrderContentViewModel by lazy {
        ViewModelProviders.of(this).get(OrderContentViewModel::class.java)
    }

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel::class.java)
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
//        orderContentViewModel.allOrderContents.toSingleEvent().observe(this, Observer { allContents ->
//            productViewModel.allProducts.toSingleEvent().observe(this, Observer<List<Product>> { allProducts ->
//
//                allContents?.filter { it.orderId == order.id }?.forEach { currentProduct ->
//
//                    val previousQuantity =
//                        allProducts?.firstOrNull { it.barcode == currentProduct.productBarcode }?.quantity
//
//                    val newQuantity =
//                        if (previousQuantity != null)
//                            previousQuantity - currentProduct.quantity
//                        else
//                            currentProduct.quantity
//
//                    productViewModel.updateProductQuantity(currentProduct.productBarcode, newQuantity)
//                }
//
//            })
            viewModel.deleteOrder(order)
//        })
    }

//    private fun updateEditedQuantities(orderContent: OrderContent) {
//        productViewModel.allProducts.observe(this, Observer { allProducts ->
//            allProducts?.forEach { product ->
//
//                if (orderContent.productBarcode == product.barcode) {
//                    productViewModel.updateProductQuantity(
//                        product.barcode, product.quantity - orderContent.quantity
//                    )
//                }
//            }
//        })
//    }

    private fun openEditOrderTab(order: Order) {
        if (::onNavigationChangedListener.isInitialized) {
            //onNavigationChangedListener.onNavigationChanged(EDIT_ORDER_TAB)
            listener = onNavigationChangedListener
        }

        listener?.onNavigationChanged(tabNumber = EDIT_ORDER_TAB, order = order)
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
        val ordersAdapter = OrdersAdapter(requireContext(), ::deleteOrder, ::openEditOrderTab)
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



