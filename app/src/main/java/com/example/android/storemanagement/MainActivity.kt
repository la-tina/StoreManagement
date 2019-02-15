package com.example.android.storemanagement

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity(), OnNavigationChangedListener {

    companion object {
        const val titleTag = "titleFragment"
        const val productTag = "productFragment"
        const val storeTag = "storeFragment"
        const val createOrderTag = "createOrderFragment"
        const val createProductTag = "createProductFragment"
    }

    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            currentFragment = supportFragmentManager.getFragment(savedInstanceState, "FragmentName")
            if (currentFragment is ProductsFragment)
                (currentFragment as ProductsFragment).onNavigationChangedListener = this

            if (currentFragment is OrdersFragment)
                (currentFragment as OrdersFragment).onNavigationChangedListener = this
        } else {
            val fragment = OrdersFragment()
            currentFragment = fragment
            fragment.onNavigationChangedListener = this
        }

        if (currentFragment?.isAdded == false) {
            val getFragmentTag = getFragmentTag(currentFragment!!)

            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, currentFragment!!, getFragmentTag)
                .commit()
        }
    }

    private fun getFragmentTag(fragment: Fragment): String =
        when (fragment) {
            is ProductsFragment -> productTag
            is OrdersFragment -> titleTag
            is StoreFragment -> storeTag
            is CreateOrderFragment -> createOrderTag
            is CreateProductFragment -> createProductTag
            else -> throw IllegalStateException()
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        //Save the fragment's instance
        if (currentFragment!!.isAdded)
            supportFragmentManager.putFragment(outState, "FragmentName", currentFragment as Fragment)
    }


    override fun onStart() {
        super.onStart()

        bottom_navigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_orders -> {
                    openOrdersTab()
                    true
                }
                R.id.action_products -> {
                    openProductsTab()
                    true
                }
                R.id.action_store -> {
                    openStoreTab()
                    true
                }
                else -> true
            }
        }
    }

    override fun onNavigationChanged(tabNumber: Int) {
        when (tabNumber) {
            0 -> openCreateOrderTab()
            1 -> openCreateProductTab()
        }
    }

    private fun openCreateOrderTab() {
        val previouslyAddedCreateOrderFragment = supportFragmentManager.findFragmentByTag(createOrderTag)
        val fragment = (previouslyAddedCreateOrderFragment as? CreateOrderFragment) ?: CreateOrderFragment()

        openTab(fragment, createOrderTag)
    }

    private fun openCreateProductTab() {
        val previouslyAddedCreateProductFragment = supportFragmentManager.findFragmentByTag(createProductTag)
        val fragment = (previouslyAddedCreateProductFragment as? CreateProductFragment) ?: CreateProductFragment()

        openTab(fragment, createProductTag)
    }

    private fun openStoreTab() {
        val previouslyAddedStoreFragment = supportFragmentManager.findFragmentByTag(storeTag)
        val fragment = (previouslyAddedStoreFragment as? StoreFragment) ?: StoreFragment()

        openTab(fragment, storeTag)
    }

    private fun openOrdersTab() {
        val previouslyAddedTitleFragment = supportFragmentManager.findFragmentByTag(titleTag)
        val fragment = (previouslyAddedTitleFragment as? OrdersFragment) ?: OrdersFragment()

        fragment.onNavigationChangedListener = this

        openTab(fragment, titleTag)
    }

    private fun openProductsTab() {
        val previouslyAddedProductFragment = supportFragmentManager.findFragmentByTag(productTag)
        val fragment = (previouslyAddedProductFragment as? ProductsFragment) ?: ProductsFragment()

        fragment.onNavigationChangedListener = this

        openTab(fragment, productTag)
    }

    private fun openTab(fragment: Fragment, tag: String) {
        currentFragment = fragment

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack("a")
            .commit()
    }
}

interface OnNavigationChangedListener {
    fun onNavigationChanged(tabNumber: Int)
}