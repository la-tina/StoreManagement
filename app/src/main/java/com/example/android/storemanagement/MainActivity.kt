package com.example.android.storemanagement

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.android.storemanagement.create_order.CreateOrderFragment
import com.example.android.storemanagement.create_product.BarcodeScanningActivity
import com.example.android.storemanagement.create_product.CreateProductFragment
import com.example.android.storemanagement.create_product.EditProductFragment
import com.example.android.storemanagement.create_product.InfoProductFragment.Companion.CAMERA_PERMISSION_CODE
import com.example.android.storemanagement.edit_order.EditOrderFragment
import com.example.android.storemanagement.edit_order.ViewOrderFragment
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_database.OrderViewModel
import com.example.android.storemanagement.orders_tab.OrdersFragment
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsFragment
import com.example.android.storemanagement.store_tab.StoreFragment
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_orders.*
import okhttp3.OkHttpClient
import java.io.File

class MainActivity : AppCompatActivity(), OnNavigationChangedListener , Observer<List<Order>>, NavigationView.OnNavigationItemSelectedListener {

    private var currentFragment: Fragment? = null
    private var ordersFragment: OrdersFragment? = null

    private val orderViewModel : OrderViewModel by lazy {
        ViewModelProviders.of(this).get(OrderViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        deleteDatabase("Product_database")

        Stetho.initializeWithDefaults(applicationContext)

        OkHttpClient.Builder()
            .addNetworkInterceptor(StethoInterceptor())
            .build()

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
        orderViewModel.allOrders.observe(this, this)

    }

    override fun onChanged(allOrders: List<Order>?) {
        
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val firstFragment = supportFragmentManager?.fragments?.first()
        if (firstFragment != null) {
            supportFragmentManager.putFragment(outState, "FragmentName", firstFragment)
        }
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

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        ordersFragment?.toolbarTop?.setNavigationOnClickListener{
            openCloseNavigationDrawer()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_about -> {
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
            }
            R.id.action_export -> {
                Toast.makeText(this, "Export", Toast.LENGTH_SHORT).show()
                exportDatabaseToCSVFile()
            }
            R.id.action_log_out -> {
                Toast.makeText(this, "Log out", Toast.LENGTH_SHORT).show()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun openCloseNavigationDrawer() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    private fun getCSVFileName() : String = "StoreManagementDB.csv"

    private fun exportDatabaseToCSVFile() {
        val csvFile = generateFile(this, getCSVFileName())
        if (csvFile != null) {
            exportMoviesWithDirectorsToCSVFile(csvFile)

            Toast.makeText(this, getString(R.string.csv_file_generated_text), Toast.LENGTH_LONG).show()
            val intent = goToFileIntent(this, csvFile)
            startActivity(intent)
        } else {
            Toast.makeText(this, getString(R.string.csv_file_not_generated_text), Toast.LENGTH_LONG).show()
        }
    }

    private fun exportMoviesWithDirectorsToCSVFile(csvFile: File) {
        csvWriter().open(csvFile, append = false) {
            // Header
            writeRow(listOf("Orders"))
            writeRow(listOf("", "Order ID", "Order status", "Date", "Product in order", "Quantity", "Product final price"))
            ordersFragment?.addRowOrders(::writeRow)
            writeRow(emptyList())
            writeRow(emptyList())
            writeRow(listOf("Products"))
            writeRow(listOf("Barcode", "Product", "Price", "Overcharge", "Quantity"))
            ordersFragment?.addRowProducts(::writeRow)
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

    override fun onNavigationChanged(tabNumber: Int, product: Product?, order: Order?) {
        when (tabNumber) {
            CREATE_ORDER_TAB -> openCreateOrderTab()
            CREATE_PRODUCT_TAB -> openCreateProductTab()
            EDIT_PRODUCT_TAB -> openEditProductTab(product)
            EDIT_ORDER_TAB -> openEditOrderTab(order)
            VIEW_ORDER_TAB -> openViewOrderTab()
        }
    }

    private fun setDefaultCurrentFragment() {
        val fragment = OrdersFragment()
        currentFragment = fragment
        ordersFragment = fragment
        fragment.onNavigationChangedListener = this
    }

    private fun restoreCurrentFragmentState() {
        if (currentFragment is ProductsFragment)
            (currentFragment as ProductsFragment).onNavigationChangedListener = this

        if (currentFragment is OrdersFragment){
            (currentFragment as OrdersFragment).onNavigationChangedListener = this
            ordersFragment = currentFragment as OrdersFragment
        }
    }

    private fun getFragmentTag(fragment: Fragment): String =
        when (fragment) {
            is ProductsFragment -> productTag
            is OrdersFragment -> titleTag
            is StoreFragment -> storeTag
            is CreateOrderFragment -> createOrderTag
            is CreateProductFragment -> createProductTag
            is EditProductFragment -> editProductTag
            is EditOrderFragment -> editOrderTag
            is ViewOrderFragment -> viewOrderTag
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

    private fun openEditOrderTab(order: Order?) {
        val previouslyAddedEditOrderFragment = supportFragmentManager.findFragmentByTag(editOrderTag)
        val fragment = (previouslyAddedEditOrderFragment as? EditOrderFragment) ?: EditOrderFragment()
        val bundle = Bundle().apply { putSerializable(ORDER_KEY, order) }
        fragment.arguments = bundle

        openCreateTab(fragment, editOrderTag)
    }

    private fun openViewOrderTab() {
        val previouslyAddedViewOrderFragment = supportFragmentManager.findFragmentByTag(viewOrderTag)
        val fragment = (previouslyAddedViewOrderFragment as? ViewOrderFragment) ?: ViewOrderFragment()

        openCreateTab(fragment, viewOrderTag)
    }

    private fun openStoreTab() {
        val previouslyAddedStoreFragment = supportFragmentManager.findFragmentByTag(storeTag)
        val fragment = (previouslyAddedStoreFragment as? StoreFragment) ?: StoreFragment()

        openMainTab(fragment, storeTag)
    }

    private fun openOrdersTab() {
        val previouslyAddedTitleFragment = supportFragmentManager.findFragmentByTag(titleTag)
        val fragment = (previouslyAddedTitleFragment as? OrdersFragment) ?: OrdersFragment()
        ordersFragment = fragment
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
    fun onNavigationChanged(tabNumber: Int, product: Product? = null, order: Order? = null)
}



