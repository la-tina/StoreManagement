package com.example.android.storemanagement

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.areThereNewNotifications
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.getCurrentFirebaseUserInternal
import com.example.android.storemanagement.firebase.FirebaseOrder
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.firebase.FirebaseUserInternal
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_database.OrderViewModel
import com.example.android.storemanagement.orders_tab.OrdersFragment
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsFragment
import com.example.android.storemanagement.products_tab.ProductsTabFragment
import com.example.android.storemanagement.store_tab.StoreFragment
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import java.io.File
import java.util.*


open class MainActivity : AppCompatActivity(), OnNavigationChangedListener, Observer<List<Order>>,
    NavigationView.OnNavigationItemSelectedListener {

    private var currentFragment: Fragment? = null
    private var ordersFragment: OrdersFragment? = null
    private var productsTabFragment: ProductsTabFragment? = null
    private var user: FirebaseUser? = null
    var newOrderCreated = false
    private lateinit var menu: Menu

    private val orderViewModel: OrderViewModel by lazy {
        ViewModelProviders.of(this).get(OrderViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbarMain)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarMain.inflateMenu(R.menu.info_menu)
        toolbarMain.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_notifications)

        user = FirebaseAuth.getInstance().currentUser
        val isLoggedInInfoShown = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isLoggedInInfoShown", false)
        if (user != null && !isLoggedInInfoShown) {
            val loggedInInfoText = LOGGED_IN_INFO + user?.displayName
            //set title for alert dialog
            val dialog = AlertDialog.Builder(this, R.style.AlertDialog)
                .setTitle(R.string.yay_text)
                .setMessage(loggedInInfoText)
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }.show()

            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .edit()
                .putBoolean("isLoggedInInfoShown", true)
                .apply()
            val textView = dialog.findViewById<View>(android.R.id.message) as TextView
            textView.textSize = 17f
            textView.setTextColor(ContextCompat.getColor(this, R.color.darkBarColor))
        }

        deleteDatabase("Product_database")
        deleteDatabase("Order_database")

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

        val firstFragment = supportFragmentManager.fragments.first()
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

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard(this as Activity)
        navigation_view?.setNavigationItemSelectedListener(this)

        toolbarMain?.setNavigationOnClickListener {
            openCloseNavigationDrawer()
        }
        Log.d("TinaNotifications", "onResume")
        setNotificationIcon()

        val menu: Menu = navigation_view.menu
        if (user != null) {
            val userItem = menu.findItem(R.id.user)
            userItem.title = user?.displayName
            userItem.isCheckable = false
            userItem.isEnabled = false
            val accountType = menu.findItem(R.id.account_type_text)
            accountType.isCheckable = false
            accountType.isEnabled = false
            getCurrentFirebaseUserInternal { fbUserInternal ->
//                val firstLetter: String = fbUserInternal.accountType.substring(0, 1)
//                val accountTypeText = firstLetter + fbUserInternal.accountType.substring(1).toLowerCase(Locale.ROOT)
                accountType.title = fbUserInternal.accountType
            }
            val logInLogOutItem = menu.findItem(R.id.action_log_in)
            logInLogOutItem.title = this.getString(R.string.action_log_out)
            logInLogOutItem.setIcon(R.drawable.ic_baseline_exit_to_app)
        } else {
            menu.removeItem(R.id.user)
            val logInLogOutItem = menu.findItem(R.id.action_log_in)
            logInLogOutItem.title = this.getString(R.string.action_log_in)
        }
    }

    private fun setNotificationIcon() {
        areThereNewNotifications { areAllNotificationsSeen ->
            Log.d("TinaNotifications", "Main $areAllNotificationsSeen")
            when (areAllNotificationsSeen) {
                true -> toolbarMain.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_notifications)
                false -> toolbarMain.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_notifications_active)
            }
        }
    }

//    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
//        areThereNewNotifications { areAllNotificationsSeen ->
//            Log.d("TinaNotifications", "Main $areAllNotificationsSeen")
//            when (areAllNotificationsSeen) {
////                true -> menu?.getItem(0).icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_notifications)
//                false -> menu?.getItem(0)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_notifications_active)
//            }
//        }
//        return super.onPrepareOptionsMenu(menu)
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu
        menuInflater.inflate(R.menu.info_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId
        if (id == R.id.action_notifications) {
            Toast.makeText(this, "Notifications", Toast.LENGTH_LONG).show()
            openNotificationsTab()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_about -> {
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
//                onNavigationChanged(ABOUT_TAB, null, null)
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }
            R.id.action_export -> {
                Toast.makeText(this, "Export", Toast.LENGTH_SHORT).show()
                exportDatabaseToCSVFile()
            }
            R.id.action_log_in -> {

                if (user != null) {
                    Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show()
                    Firebase.auth.signOut()
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                        .edit()
                        .putBoolean("isLoggedInInfoShown", false)
                        .apply()
                } else {
                    Toast.makeText(this, "Log in", Toast.LENGTH_SHORT).show()
                }
                val intent = Intent(this, ActivityLogin::class.java)
                startActivity(intent)
            }
        }
        openCloseNavigationDrawer()
        return true
    }

    private fun openCloseNavigationDrawer() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    private fun getCSVFileName(): String = "StoreManagementDB.csv"

    private fun exportDatabaseToCSVFile() {
        val csvFile = generateFile(this, getCSVFileName())
        if (csvFile != null) {
            exportOrdersToCSVFile(csvFile)

            Toast.makeText(this, getString(R.string.csv_file_generated_text), Toast.LENGTH_LONG).show()
            val intent = goToFileIntent(this, csvFile)
            startActivity(intent)
        } else {
            Toast.makeText(this, getString(R.string.csv_file_not_generated_text), Toast.LENGTH_LONG).show()
        }
    }

    private fun exportOrdersToCSVFile(csvFile: File) {
        csvWriter().open(csvFile, append = false) {
            // Header
            writeRow(listOf("Orders"))
            writeRow(
                listOf(
                    "",
                    "Order ID",
                    "Order status",
                    "Date",
                    "Product in order",
                    "Quantity",
                    "Product final price"
                )
            )
            ordersFragment?.addRowOrders(::writeRow)
            writeRow(emptyList())
            writeRow(emptyList())
            writeRow(listOf("Products"))
            writeRow(listOf("Barcode", "Product", "Price", "Overcharge", "Quantity"))
            ordersFragment?.addRowProducts(::writeRow)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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

    override fun onNavigationChanged(
        tabNumber: Int,
        product: Product?,
        firebaseProduct: FirebaseProduct?,
        order: Order?,
        firebaseOrder: FirebaseOrder?,
        firebaseUser: FirebaseUserInternal?
    ) {
        when (tabNumber) {
            CREATE_ORDER_TAB -> openCreateOrderTab(firebaseUser!!)
            CREATE_PRODUCT_TAB -> openCreateProductTab()
            EDIT_PRODUCT_TAB -> openEditProductTab(product, firebaseProduct)
            EDIT_ORDER_TAB -> openEditOrderTab(firebaseUser!!, order, firebaseOrder)
            VIEW_ORDER_TAB -> openViewOrderTab(order, firebaseOrder)
            USERS_TAB -> openUsersTab()
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

        if (currentFragment is OrdersFragment) {
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
            is UserSelectionFragment -> usersTag
            is NotificationsFragment -> notificationsTag
            else -> throw IllegalStateException()
        }

    private fun openUsersTab() {
        val previouslyAddedUsersFragment = supportFragmentManager.findFragmentByTag(usersTag)
        val fragment = (previouslyAddedUsersFragment as? UserSelectionFragment) ?: UserSelectionFragment()

        fragment.onNavigationChangedListener = this
        openCreateTab(fragment, usersTag)
    }

    private fun openNotificationsTab() {
        val previouslyAddedNotificationsFragment = supportFragmentManager.findFragmentByTag(notificationsTag)
        val fragment = (previouslyAddedNotificationsFragment as? NotificationsFragment) ?: NotificationsFragment()

        fragment.onNavigationChangedListener = this
        openCreateTab(fragment, notificationsTag)
    }

    private fun openCreateOrderTab(firebaseUser: FirebaseUserInternal) {
        val previouslyAddedCreateOrderFragment = supportFragmentManager.findFragmentByTag(
            createOrderTag
        )
        val fragment = (previouslyAddedCreateOrderFragment as? CreateOrderFragment) ?: CreateOrderFragment()
        val bundle = Bundle().apply {
            putSerializable(USER_KEY, firebaseUser)
        }
        fragment.arguments = bundle
        openCreateTab(fragment, createOrderTag)
    }

    private fun openCreateProductTab() {
        val previouslyAddedCreateProductFragment = supportFragmentManager.findFragmentByTag(
            createProductTag
        )
        val fragment = (previouslyAddedCreateProductFragment as? CreateProductFragment) ?: CreateProductFragment()

        openCreateTab(fragment, createProductTag)
    }

    private fun openEditProductTab(product: Product?, firebaseProduct: FirebaseProduct?) {
        val previouslyAddedEditProductFragment = supportFragmentManager.findFragmentByTag(
            editProductTag
        )
        val fragment = (previouslyAddedEditProductFragment as? EditProductFragment) ?: EditProductFragment()

        if (product == null) {
            val bundle = Bundle().apply { putSerializable(PRODUCT_KEY, firebaseProduct) }
            fragment.arguments = bundle
        } else {
            val bundle = Bundle().apply { putSerializable(PRODUCT_KEY, product) }
            fragment.arguments = bundle
        }

        openCreateTab(fragment, editProductTag)
    }

    private fun openEditOrderTab(firebaseUser: FirebaseUserInternal, order: Order?, firebaseOrder: FirebaseOrder?) {
        val previouslyAddedEditOrderFragment = supportFragmentManager.findFragmentByTag(editOrderTag)
        val fragment = (previouslyAddedEditOrderFragment as? EditOrderFragment) ?: EditOrderFragment()
        if (order == null) {
            val bundle = Bundle().apply {
                putSerializable(ORDER_KEY, firebaseOrder)
                putSerializable(USER_KEY, firebaseUser)
            }
            fragment.arguments = bundle
        }

        openCreateTab(fragment, editOrderTag)
    }

    private fun openViewOrderTab(order: Order?, firebaseOrder: FirebaseOrder?) {
        val previouslyAddedViewOrderFragment = supportFragmentManager.findFragmentByTag(viewOrderTag)
        val fragment = (previouslyAddedViewOrderFragment as? ViewOrderFragment) ?: ViewOrderFragment()
        if (order == null) {
            val bundle = Bundle().apply { putSerializable(VIEW_ORDER_KEY, firebaseOrder) }
            fragment.arguments = bundle
        } else {
            val bundle = Bundle().apply { putSerializable(VIEW_ORDER_KEY, order) }
            fragment.arguments = bundle
        }

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

    companion object {
        private const val LOGGED_IN_INFO = "You have successfully logged in as "
    }
}

interface OnNavigationChangedListener {
    fun onNavigationChanged(
        tabNumber: Int,
        product: Product? = null,
        firebaseProduct: FirebaseProduct? = null,
        order: Order? = null,
        firebaseOrder: FirebaseOrder? = null,
        firebaseUser: FirebaseUserInternal? = null
    )
}
