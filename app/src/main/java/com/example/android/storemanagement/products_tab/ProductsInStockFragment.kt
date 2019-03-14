package com.example.android.storemanagement.products_tab

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.R
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_products_in_stock.*

class ProductsInStockFragment : ProductsTabFragment() {

    private lateinit var viewModel: ProductViewModel

    override fun setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(ProductViewModel(requireActivity().application)::class.java)
    }

    override fun setOnNavigationChangedListener(onNavigationChangedListener: OnNavigationChangedListener) {
        listener = onNavigationChangedListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_products_in_stock, container, false)

    override fun deleteProduct(product: Product) {
        viewModel.deleteProduct(product)
    }

    override fun setupRecyclerView() {
        products_in_stock_recycler_view?.let { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            val productsInStockAdapter = ProductsInStockAdapter(requireContext(), ::deleteProduct, ::openEditProductTab)
            recyclerView.adapter = productsInStockAdapter

            // Observer on the LiveData
            // The onChanged() method fires when the observed data changes and the activity is
            // in the foreground.

            viewModel.inStockProducts.observe(this, Observer { inStockProducts ->
                // Update the cached copy of the words in the adapter.
                inStockProducts?.let {
                    productsInStockAdapter.setProducts(it)
                    setupEmptyView(empty_view_products_in_stock, products_in_stock_recycler_view)
                    //quantity > 0
                }
            })
        }
    }
}
