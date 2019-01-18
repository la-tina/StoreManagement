package com.example.android.storemanagement

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.example.android.storemanagement.database.ProductViewModel
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity(), OnNavigationChangedListener{

    companion object {
        const val titleTag = "titleFragment"
        const val productTag = "productFragment"
        const val storeTag = "storeFragment"
        const val createOrderTag = "createOrderFragment"
        const val createProductTag = "createProductFragment"
    }

    private val viewModel: ProductViewModel
        get() = ViewModelProviders.of(this).get(ProductViewModel(application)::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragment = TitleFragment()
        fragment.onNavigationChangedListener = this

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, fragment, titleTag)
            .commit()
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

        fragment.viewModel = this.viewModel

        openTab(fragment, createProductTag)
    }

    private fun openStoreTab() {
        val previouslyAddedStoreFragment = supportFragmentManager.findFragmentByTag(storeTag)
        val fragment = (previouslyAddedStoreFragment as? StoreFragment) ?: StoreFragment()

        openTab(fragment, storeTag)
    }

    private fun openOrdersTab() {
        val previouslyAddedTitleFragment = supportFragmentManager.findFragmentByTag(titleTag)
        val fragment = (previouslyAddedTitleFragment as? TitleFragment) ?: TitleFragment()

        fragment.onNavigationChangedListener = this

        openTab(fragment, titleTag)
    }

    private fun openProductsTab() {
        val previouslyAddedProductFragment = supportFragmentManager.findFragmentByTag(productTag)
        val fragment = (previouslyAddedProductFragment as? ProductsFragment) ?: ProductsFragment()

        fragment.viewModel = this.viewModel

        fragment.onNavigationChangedListener = this

        openTab(fragment, productTag)
    }

    private fun openTab(fragment: Fragment, tag: String) {

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack("a")
            .commit()
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        Timber.i("onRestoreInstanceState Called")
    }

}

interface OnNavigationChangedListener {
    fun onNavigationChanged(tabNumber: Int)
}