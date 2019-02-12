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
import kotlinx.android.synthetic.main.fragment_products_in_stock.*
import kotlinx.android.synthetic.main.fragment_products_low_stock.*


lateinit var productInStockViewModel: ProductViewModel

class ProductsInStock : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_products_in_stock, container, false)
    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView()
    }


    private fun setupRecyclerView() {
        products_in_stock_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        val productsAdapter = ProductsAdapter(requireContext())
        products_in_stock_recycler_view.adapter = productsAdapter

        productInStockViewModel = ViewModelProviders.of(this).get(ProductViewModel::class.java)

        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        //productInStockViewModel.getInStockProducts()
        productInStockViewModel.inStockProducts.observe(this, Observer { inStockProducts ->
            // Update the cached copy of the words in the adapter.
            inStockProducts?.let {
                productsAdapter.setProducts(it)
                //quantity > 0
            }
        })
    }
}
