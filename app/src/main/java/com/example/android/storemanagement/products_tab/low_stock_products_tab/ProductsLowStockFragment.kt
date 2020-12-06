package com.example.android.storemanagement.products_tab.low_stock_products_tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.R
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsTabFragment
import kotlinx.android.synthetic.main.fragment_products_container.*


class ProductsLowStockFragment : ProductsTabFragment() {

    private lateinit var viewModel: ProductsLowStockViewModel

    override fun setupViewModel() {
        viewModel = ViewModelProviders.of(this)
            .get(ProductsLowStockViewModel(requireActivity().application)::class.java)
    }

    override fun setOnNavigationChangedListener(onNavigationChangedListener: OnNavigationChangedListener) {
        listener = onNavigationChangedListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_products_container, container, false)

    override fun onStart() {
        super.onStart()
        info_text.text = context?.getString(R.string.low_stock_products_info)
    }

    override fun deleteProduct(product: Product) {
        viewModel.deleteProduct(product)
    }

    override fun setupRecyclerView() {
        products_recycler_view?.let { recyclerView ->
            recyclerView.layoutManager =
                LinearLayoutManager(requireContext())
            val productsLowStockAdapter =
                ProductsLowStockAdapter(
                    requireContext(),
                    ::deleteProduct,
                    ::openEditProductTab,
                    ::getProductQuantity
                )
            recyclerView.adapter = productsLowStockAdapter

            // Observer on the LiveData
            // The onChanged() method fires when the observed data changes and the activity is
            // in the foreground.

            viewModel.lowStockProducts.observe(this, Observer { products ->
                // Update the cached copy of the words in the adapter.
                products?.let {
                    productsLowStockAdapter.setProducts(it)
                    setupEmptyView(empty_view_products, products_recycler_view)
                    //quantity < 5
                }
            })
        }
    }
}
