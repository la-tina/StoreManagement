package com.example.android.storemanagement

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.android.storemanagement.create_order.CreateOrderFragment
import com.example.android.storemanagement.create_product.BarcodeScanningActivity
import com.example.android.storemanagement.create_product.CreateProductFragment
import com.example.android.storemanagement.create_product.EditProductFragment
import com.example.android.storemanagement.create_product.InfoProductFragment.Companion.CAMERA_PERMISSION_CODE
import com.example.android.storemanagement.orders_tab.OrdersFragment
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsFragment
import com.example.android.storemanagement.store_tab.StoreFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnNavigationChangedListener {

    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        deleteDatabase("Product_database")
//        deleteDatabase("Order_database")

        if (savedInstanceState != null) {
            currentFragment = supportFragmentManager.getFragment(savedInstanceState, "FragmentName")
            restoreCurrentFragmentState()
        } else {
            setDefaultCurrentFragment()
        }

        if (currentFragment?.isAdded == false) {
            val getFragmentTag = getFragmentTag(currentFragment!!)

            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, currentFragment!!, getFragmentTag)
                .commit()
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(this, BarcodeScanningActivity()::class.java)
            startActivityForResult(intent, BARCODE_ACTIVITY_REQUEST_CODE)
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        restoreCurrentFragmentState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BARCODE_ACTIVITY_REQUEST_CODE) {
            currentFragment?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onNavigationChanged(tabNumber: Int, product: Product?) {
        when (tabNumber) {
            CREATE_ORDER_TAB -> openCreateOrderTab()
            CREATE_PRODUCT_TAB -> openCreateProductTab()
            EDIT_PRODUCT_TAB -> openEditProductTab(product)
        }
    }

    private fun setDefaultCurrentFragment() {
        val fragment = OrdersFragment()
        currentFragment = fragment
        fragment.onNavigationChangedListener = this
    }

    private fun restoreCurrentFragmentState() {
        if (currentFragment is ProductsFragment)
            (currentFragment as ProductsFragment).onNavigationChangedListener = this

        if (currentFragment is OrdersFragment)
            (currentFragment as OrdersFragment).onNavigationChangedListener = this
    }

    private fun getFragmentTag(fragment: Fragment): String =
        when (fragment) {
            is ProductsFragment -> productTag
            is OrdersFragment -> titleTag
            is StoreFragment -> storeTag
            is CreateOrderFragment -> createOrderTag
            is CreateProductFragment -> createProductTag
            is EditProductFragment -> editProductTag
            else -> throw IllegalStateException()
        }

    private fun openCreateOrderTab() {
        val previouslyAddedCreateOrderFragment = supportFragmentManager.findFragmentByTag(createOrderTag)
        val fragment = (previouslyAddedCreateOrderFragment as? CreateOrderFragment) ?: CreateOrderFragment()

        openCreateTab(fragment, createOrderTag)
    }

    private fun openCreateProductTab() {
        val previouslyAddedCreateProductFragment = supportFragmentManager.findFragmentByTag(createProductTag)
        val fragment = (previouslyAddedCreateProductFragment as? CreateProductFragment) ?: CreateProductFragment()

        openCreateTab(fragment, createProductTag)
    }

    private fun openEditProductTab(product: Product?) {
        val previouslyAddedEditProductFragment = supportFragmentManager.findFragmentByTag(editProductTag)
        val fragment = (previouslyAddedEditProductFragment as? EditProductFragment) ?: EditProductFragment()
        val bundle = Bundle().apply { putSerializable(PRODUCT_KEY, product) }
        fragment.arguments = bundle
        openCreateTab(fragment, editProductTag)
    }

    private fun openStoreTab() {
        val previouslyAddedStoreFragment = supportFragmentManager.findFragmentByTag(storeTag)
        val fragment = (previouslyAddedStoreFragment as? StoreFragment) ?: StoreFragment()

        openMainTab(fragment, storeTag)
    }

    private fun openOrdersTab() {
        val previouslyAddedTitleFragment = supportFragmentManager.findFragmentByTag(titleTag)
        val fragment = (previouslyAddedTitleFragment as? OrdersFragment) ?: OrdersFragment()

        fragment.onNavigationChangedListener = this

        openMainTab(fragment, titleTag)
    }

    private fun openProductsTab() {
        val previouslyAddedProductFragment = supportFragmentManager.findFragmentByTag(productTag)
        val fragment = (previouslyAddedProductFragment as? ProductsFragment) ?: ProductsFragment()

        fragment.onNavigationChangedListener = this

        openMainTab(fragment, productTag)
    }

    private fun openCreateTab(fragment: Fragment, tag: String) {
        currentFragment = fragment

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack("a")
            .commit()
    }

    private fun openMainTab(fragment: Fragment, tag: String) {
        currentFragment = fragment

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
    }
}

interface OnNavigationChangedListener {
    fun onNavigationChanged(tabNumber: Int, product: Product? = null)
}

