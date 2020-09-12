package com.example.android.storemanagement.store_tab

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.android.storemanagement.R
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_store.*
import kotlinx.android.synthetic.main.store_item.*

class StoreFragment : Fragment() {
    companion object {
        const val KEY_QUANTITY_VALUE = "productQuantityValue"
    }

    private val allOrderContents = mutableListOf<OrderContent>()

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel(requireActivity().application)::class.java)
    }
    private val orderContentViewModel: OrderContentViewModel by lazy {
        ViewModelProviders.of(this).get(OrderContentViewModel(requireActivity().application)::class.java)
    }

    private var savedProductQuantity: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_store, container, false)

        if (savedInstanceState != null) {
            savedProductQuantity = savedInstanceState.getCharSequence(KEY_QUANTITY_VALUE)!!.toString()
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView()

        if (store_item_quantity != null && !savedProductQuantity.isBlank())
            store_item_quantity.setText(savedProductQuantity)

        button_save_quantity.setOnClickListener {
            val quantities: MutableMap<String, Int> = (store_recycler_view.adapter as StoreAdapter).quantities
            quantities.forEach { (productName, quantity) ->
                updateQuantity(productName, quantity)
                Toast.makeText(requireContext(), "Quantity updated.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeOrderContentViewModel() {
        orderContentViewModel.allOrderContents.observe(this, Observer { allOrderContents ->
            this.allOrderContents.clear()
            allOrderContents?.let {
                this.allOrderContents.addAll(it)
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(KEY_QUANTITY_VALUE, store_item_quantity.text)
    }

    private fun updateQuantity(productName: String, quantity: Int) {
        productViewModel.updateQuantity(productName, quantity)
    }

    private fun setupRecyclerView() {
        store_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        val storeAdapter = StoreAdapter(requireContext(), ::setOrderButtonEnabled, ::getProductQuantity)
        store_recycler_view.adapter = storeAdapter

        productViewModel.allProducts.observe(this, Observer { products ->
            // Update the cached copy of the words in the adapter.
            products?.let {
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

    private fun getProductQuantity(product: Product): Int =
        allOrderContents.filter { it.productBarcode == product.barcode }.map { it.quantity }.sum()

    private fun setOrderButtonEnabled(enabled: Boolean) {
        button_save_quantity.isEnabled = enabled
    }
}
