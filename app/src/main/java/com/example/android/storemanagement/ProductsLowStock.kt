package com.example.android.storemanagement

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.database.ProductViewModel
import kotlinx.android.synthetic.main.create_order_item.*
import kotlinx.android.synthetic.main.fragment_products.*
import kotlinx.android.synthetic.main.fragment_products_low_stock.*
import java.util.Locale.filter


lateinit var productLowStockViewModel: ProductViewModel

class ProductsLowStock : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_products_low_stock, container, false)
    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        products_low_stock_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        val productsAdapter = ProductsAdapter(requireContext())
        products_low_stock_recycler_view.adapter = productsAdapter

        productLowStockViewModel = ViewModelProviders.of(this).get(ProductViewModel::class.java)

        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        productInStockViewModel.getLowStockProducts()
        productLowStockViewModel.lowStockProducts.observe(this, Observer { products ->
            // Update the cached copy of the words in the adapter.
            products?.let {
                productsAdapter.setProducts(it)
                //quantity < 5
            }
        })
    }
}
