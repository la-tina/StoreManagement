package com.example.android.storemanagement.products_tab.all_products_tab

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.R
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsTabFragment
import com.example.android.storemanagement.toSingleEvent
import kotlinx.android.synthetic.main.fragment_all_products.*


class AllProductsFragment : ProductsTabFragment() {

    private lateinit var viewModel: AllProductsViewModel

    override fun setupViewModel() {
        viewModel =
            ViewModelProviders.of(this).get(AllProductsViewModel(requireActivity().application)::class.java)
    }

    override fun setOnNavigationChangedListener(onNavigationChangedListener: OnNavigationChangedListener) {
        listener = onNavigationChangedListener
    }

    override fun deleteProduct(product: Product) {
        orderContentViewModel.allOrderContents.toSingleEvent().observe(this, Observer { orderContents ->
            orderContents.filter { it.productBarcode == product.barcode }.forEach { orderContent ->
                orderViewModel.allOrders.toSingleEvent().observe(this, Observer { orders->
                    orders.filter{it.id == orderContent.orderId}.forEach { order ->
                        if (order.isOrdered){
                            val deletedOrderContent: MutableList<OrderContent> = mutableListOf()
//                            if (order.deletedOrderContent.isNotEmpty()){
//                                order.deletedOrderContent.forEach { orderContent ->
//                                    deletedOrderContent.add(orderContent)
//                                }
//                            }

                            deletedOrderContent.add(orderContent)
                            val deleteOrderContentList = deletedOrderContent.toList()
//                            orderViewModel.addDeletedProducts(order.id, deleteOrderContentList)
                        }
                    }
                })
            }
        })
        viewModel.deleteProduct(product)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_all_products, container, false)


    override fun setupRecyclerView() {
        products_recycler_view?.let { recyclerView ->
            recyclerView.layoutManager =
                LinearLayoutManager(requireContext())
            val productsAdapter = AllProductsAdapter(
                requireContext(),
                ::deleteProduct,
                ::openEditProductTab,
                ::getProductQuantity
            )
            recyclerView.adapter = productsAdapter

            // Observer on the LiveData
            // The onChanged() method fires when the observed data changes and the activity is
            // in the foreground.

            viewModel.allProducts.observe(this, Observer { products ->
                // Update the cached copy of the products in the adapter.
                products?.let {
                    productsAdapter.setProducts(it)
                    setupEmptyView(empty_view_products, products_recycler_view)
                }
            })
        }
    }
}


