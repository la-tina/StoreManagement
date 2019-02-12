package com.example.android.storemanagement

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v4.view.ViewPager
import com.example.android.storemanagement.database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_products.*
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders


class ProductsFragment : Fragment(){


    private var myContext: FragmentActivity? = null

    lateinit var onNavigationChangedListener: OnNavigationChangedListener

    lateinit var productViewModel: ProductViewModel

    //private var mListener: ProductsLowStock.OnFragmentInteractionListener? = null

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
                    val adapter = products_recycler_view.adapter as ProductsAdapter
                    val myProduct = adapter.getProductAtPosition(position)

                    // Delete the product by calling deleteProduct() on the ProductViewModel:
                    productViewModel.deleteProduct(myProduct)
                }
            })
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_products, container, false)



    override fun onStart() {
        super.onStart()
        setupRecyclerView()

        // Create an adapter that knows which fragment should be shown on each page
        val adapter = ProductsTabAdapter(activity!!.supportFragmentManager)

        // Set the adapter onto the view pager
        products_viewpager.adapter = adapter

        // Give the TabLayout the ViewPager
        tab_layout_products.setupWithViewPager(products_viewpager)

    }

    override fun onResume() {
        super.onResume()
        activity?.products_add_button?.setOnClickListener {
            if (::onNavigationChangedListener.isInitialized)
                onNavigationChangedListener.onNavigationChanged(1)
        }
    }


    private fun setupEmptyView() {
        val products = products_recycler_view.adapter!!
        if (products.itemCount == 0) {
            products_recycler_view.visibility = View.GONE
            empty_view_products.visibility = View.VISIBLE
        } else {
            products_recycler_view.visibility = View.VISIBLE
            empty_view_products.visibility = View.GONE
        }
    }


    private fun setupRecyclerView() {
        products_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        val productsAdapter = ProductsAdapter(requireContext())
        products_recycler_view.adapter = productsAdapter

        productViewModel = ViewModelProviders.of(this).get(ProductViewModel::class.java)

        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        productViewModel.allProducts.observe(this, Observer { products ->
            // Update the cached copy of the words in the adapter.
            products?.let {
                productsAdapter.setProducts(it)
                setupEmptyView()
            }
        })

        helper.attachToRecyclerView(products_recycler_view)
    }
}






