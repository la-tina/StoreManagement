package com.example.android.storemanagement.products_tab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.android.storemanagement.CREATE_PRODUCT_TAB
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.R
import com.example.android.storemanagement.products_tab.all_products_tab.AllProductsFragment
import com.example.android.storemanagement.products_tab.in_stock_products_tab.ProductsInStockFragment
import com.example.android.storemanagement.products_tab.low_stock_products_tab.ProductsLowStockFragment
import com.example.android.storemanagement.products_tab.pending_products_tab.PendingProductsFragment
import kotlinx.android.synthetic.main.fragment_products.*
import kotlinx.android.synthetic.main.fragment_products.view.*


class ProductsFragment : Fragment() {

    lateinit var onNavigationChangedListener: OnNavigationChangedListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_products, container, false)
        view.toolbarTop.inflateMenu(R.menu.products_filer_menu)
        view.toolbarTop.overflowIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_baseline_filter_alt)
        return view
    }

    override fun onResume() {
        super.onResume()
        products_add_button?.setOnClickListener {
            if (::onNavigationChangedListener.isInitialized)
                onNavigationChangedListener.onNavigationChanged(CREATE_PRODUCT_TAB)
        }
        setupViewPager()
    }

    override fun onPause() {
        super.onPause()
        childFragmentManager.fragments.forEach { fragment ->
            childFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }

    private fun setupViewPager() {
        // Create an adapter that knows which fragment should be shown on each page
        val allProductsFragment =
            AllProductsFragment()
        allProductsFragment.setOnNavigationChangedListener(onNavigationChangedListener)

        val productsInStockFragment =
            ProductsInStockFragment()
        productsInStockFragment.setOnNavigationChangedListener(onNavigationChangedListener)

        val productsLowStockFragment =
            ProductsLowStockFragment()
        productsLowStockFragment.setOnNavigationChangedListener(onNavigationChangedListener)

        val pendingProductsFragment =
            PendingProductsFragment()
        pendingProductsFragment.setOnNavigationChangedListener(onNavigationChangedListener)

        val fragments = listOf(allProductsFragment, productsInStockFragment, productsLowStockFragment, pendingProductsFragment)

        val adapter = ProductsTabAdapter(childFragmentManager, fragments)
        // Set the adapter onto the view pager
        products_viewpager.adapter = adapter

        // Give the TabLayout the ViewPager
        tab_layout_products.setupWithViewPager(products_viewpager)
    }
}
