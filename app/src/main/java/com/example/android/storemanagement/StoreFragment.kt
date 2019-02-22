package com.example.android.storemanagement

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_store.*

class StoreFragment : Fragment() {

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel(requireActivity().application)::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_store, container, false)
    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView()

        button_save_quantity.setOnClickListener {

            val quantities: MutableMap<String, Int> = (store_recycler_view.adapter as StoreAdapter).quantities
            quantities.forEach { productName, quantity ->
                updateQuantity(productName, quantity)
            }
        }
    }

    private fun updateQuantity(productName: String, quantity: Int) {
        productViewModel.updateQuantity(productName, quantity)
    }

    private fun setupRecyclerView() {
        store_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        val storeAdapter = StoreAdapter(requireContext())
        store_recycler_view.adapter = storeAdapter

        productViewModel.inStockProducts.observe(this, Observer { inStockProducts ->
            // Update the cached copy of the words in the adapter.
            inStockProducts?.let {
                storeAdapter.setProducts(it)
                setupEmptyView()
            }
        })
    }

    private fun setupEmptyView() {
        val products = store_recycler_view.adapter!!
        if (products.itemCount == 0) {
            store_recycler_view.visibility = View.GONE
            empty_view_products_store.visibility = View.VISIBLE
        } else {
            store_recycler_view.visibility = View.VISIBLE
            empty_view_products_store.visibility = View.GONE
        }
    }
}
