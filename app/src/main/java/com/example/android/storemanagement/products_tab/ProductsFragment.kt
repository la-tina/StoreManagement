package com.example.android.storemanagement.products_tab

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.R
import kotlinx.android.synthetic.main.fragment_products.*


class ProductsFragment : Fragment() {

    lateinit var onNavigationChangedListener: OnNavigationChangedListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onResume() {
        super.onResume()

        activity?.products_add_button?.setOnClickListener {
            if (::onNavigationChangedListener.isInitialized)
                onNavigationChangedListener.onNavigationChanged(1)
        }
        setupViewPager()
    }

    private fun setupViewPager() {
        // Create an adapter that knows which fragment should be shown on each page
        val allProductsFragment: Fragment = AllProductsFragment()
        val productsInStockFragment: Fragment =
            ProductsInStockFragment()
        val productsLowStockFragment: Fragment =
            ProductsLowStockFragment()

        val fragments = listOf(allProductsFragment, productsInStockFragment, productsLowStockFragment)

        val adapter =
            ProductsTabAdapter(childFragmentManager, fragments)
        // Set the adapter onto the view pager
        products_viewpager.adapter = adapter

        // Give the TabLayout the ViewPager
        tab_layout_products.setupWithViewPager(products_viewpager)
    }
}









