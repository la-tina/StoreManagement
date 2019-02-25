package com.example.android.storemanagement.products_tab

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.R
import com.example.android.storemanagement.products_database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_products_in_stock.*

class ProductsInStockFragment : Fragment() {

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel(requireActivity().application)::class.java)
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
    }

    private val helper by lazy {
        ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val adapter = products_in_stock_recycler_view.adapter as ProductsAdapter
                    val myProduct = adapter.getProductAtPosition(position)

                    // Delete the product by calling deleteProduct() on the ProductViewModel:
                    productViewModel.deleteProduct(myProduct)
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_products_in_stock, container, false)
    }

    private fun setupRecyclerView() {
        products_in_stock_recycler_view?.let { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            val productsInStockAdapter = ProductsInStockAdapter(requireContext())
            recyclerView.adapter = productsInStockAdapter

            // Observer on the LiveData
            // The onChanged() method fires when the observed data changes and the activity is
            // in the foreground.

            productViewModel.inStockProducts.observe(this, Observer { inStockProducts ->
                // Update the cached copy of the words in the adapter.
                inStockProducts?.let {
                    productsInStockAdapter.setProducts(it)
                    setupEmptyView()
                    //quantity > 0
                }
            })
            helper.attachToRecyclerView(recyclerView)
        }
    }

    private fun setupEmptyView() {
        val products = products_in_stock_recycler_view.adapter!!
        if (products.itemCount == 0) {
            products_in_stock_recycler_view.visibility = View.GONE
            empty_view_products_in_stock.visibility = View.VISIBLE
        } else {
            products_in_stock_recycler_view.visibility = View.VISIBLE
            empty_view_products_in_stock.visibility = View.GONE
        }
    }
}
