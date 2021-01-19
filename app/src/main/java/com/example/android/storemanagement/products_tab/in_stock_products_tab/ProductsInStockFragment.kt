package com.example.android.storemanagement.products_tab.in_stock_products_tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseDatabaseProductsOperations.deleteFirebaseProductData
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsFragment
import com.example.android.storemanagement.products_tab.ProductsTabFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_products.*
import kotlinx.android.synthetic.main.fragment_products_container.*

class ProductsInStockFragment : ProductsTabFragment() {

    private lateinit var viewModel: ProductsInStockViewModel

    override fun setupViewModel() {
        viewModel = ViewModelProvider(this)
            .get(ProductsInStockViewModel(requireActivity().application)::class.java)
    }

    override fun setOnNavigationChangedListener(onNavigationChangedListener: OnNavigationChangedListener) {
        listener = onNavigationChangedListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_products_container, container, false)

    override fun onStart() {
        super.onStart()
        info_text.text = context?.getString(R.string.in_stock_products_info)
    }

    private fun filterByAscendingQuantity() {
        if (user != null) {
            val ascendingQuantityComparator = compareBy<FirebaseProduct> { it.quantity.toInt() }
            val sortedOrderContentsList =
                firebaseProductsList.sortedWith(ascendingQuantityComparator)
            setupRecyclerView(firebaseProducts = sortedOrderContentsList)
        } else {
            viewModel.inStockProducts.observe(this, Observer { products ->
                // Update the cached copy of the words in the adapter.
                products?.let {
                    val ascendingQuantityComparator = compareBy<Product> { it.quantity }
                    val sortedProductList = products.sortedWith(ascendingQuantityComparator)
                    setupRecyclerView(products = sortedProductList)
                }
            })
        }
    }

    private fun filterByDescendingQuantity() {
        if (user != null) {
            val descendingQuantityComparator = compareByDescending<FirebaseProduct> { it.quantity.toInt() }
            val sortedOrderContentsList =
                firebaseProductsList.sortedWith(descendingQuantityComparator)
            setupRecyclerView(firebaseProducts = sortedOrderContentsList)
        } else {
            viewModel.inStockProducts.observe(this, Observer { products ->
                // Update the cached copy of the words in the adapter.
                products?.let {
                    val descendingQuantityComparator = compareByDescending<Product> { it.quantity }
                    val sortedProductList = products.sortedWith(descendingQuantityComparator)
                    setupRecyclerView(products = sortedProductList)
                }
            })
        }
    }

    private fun filterByAscendingName() {
        if (user != null) {
            val ascendingNameComparator = compareBy<FirebaseProduct> { it.name }
            val sortedOrderContentsList = firebaseProductsList.sortedWith(ascendingNameComparator)
            setupRecyclerView(firebaseProducts = sortedOrderContentsList)
        } else {
            viewModel.inStockProducts.observe(this, Observer { products ->
                // Update the cached copy of the words in the adapter.
                products?.let {
                    val ascendingNameComparator = compareBy<Product> { it.name }
                    val sortedProductList = products.sortedWith(ascendingNameComparator)
                    setupRecyclerView(products = sortedProductList)
                }
            })
        }
    }

    private fun filterByDescendingName() {
        if (user != null) {
            val descendingNameComparator = compareByDescending<FirebaseProduct> { it.name }
            val sortedOrderContentsList = firebaseProductsList.sortedWith(descendingNameComparator)
            setupRecyclerView(firebaseProducts = sortedOrderContentsList)
        } else {
            viewModel.inStockProducts.observe(this, Observer { products ->
                // Update the cached copy of the words in the adapter.
                products?.let {
                    val descendingNameComparator = compareByDescending<Product> { it.name }
                    val sortedProductList = products.sortedWith(descendingNameComparator)
                    setupRecyclerView(products = sortedProductList)
                }
            })
        }
    }

    private fun filterByAscendingPrice() {
        if (user != null) {
            val ascendingPriceComparator = compareBy<FirebaseProduct> { it.price.toFloat() }
            val sortedOrderContentsList = firebaseProductsList.sortedWith(ascendingPriceComparator)
            setupRecyclerView(firebaseProducts = sortedOrderContentsList)
        } else {
            viewModel.inStockProducts.observe(this, Observer { products ->
                // Update the cached copy of the words in the adapter.
                products?.let {
                    val ascendingPriceComparator = compareByDescending<Product> { it.price }
                    val sortedProductList = products.sortedWith(ascendingPriceComparator)
                    setupRecyclerView(products = sortedProductList)
                }
            })
        }
    }

    private fun filterByDescendingPrice() {
        if (user != null) {
            val descendingPriceComparator = compareByDescending<FirebaseProduct> { it.price.toFloat() }
            val sortedOrderContentsList = firebaseProductsList.sortedWith(descendingPriceComparator)
            setupRecyclerView(firebaseProducts = sortedOrderContentsList)
        } else {
            viewModel.inStockProducts.observe(this, Observer { products ->
                // Update the cached copy of the words in the adapter.
                products?.let {
                    val descendingPriceComparator = compareByDescending<Product> { it.price }
                    val sortedProductList = products.sortedWith(descendingPriceComparator)
                    setupRecyclerView(products = sortedProductList)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()

        user = Firebase.auth.currentUser
        if (user != null) {
            // Get a reference to our posts
            val uniqueId: String = user?.uid!!
            val database = FirebaseDatabase.getInstance()
            getFirebaseProducts(database, uniqueId)
        } else {
            setupViewModel()
            viewModel.inStockProducts.observe(this, Observer { products ->
                // Update the cached copy of the words in the adapter.
                products?.let {
                    setupViewModel()
                    setupRecyclerView(products, null)
                }
            })
        }
        setStatusMenuItemsVisibility(false)
        (parentFragment as ProductsFragment).toolbarTop.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
            when (item.itemId) {
                R.id.quantity_ascending ->
                    filterByAscendingQuantity()
                R.id.quantity_descending ->
                    filterByDescendingQuantity()
                R.id.name_ascending ->
                    filterByAscendingName()
                R.id.name_descending ->
                    filterByDescendingName()
                R.id.final_price_ascending ->
                    filterByAscendingPrice()
                R.id.final_price_descending ->
                    filterByDescendingPrice()
            }
            true
        })

    }

    override fun deleteProduct(product: Product?, firebaseProduct: FirebaseProduct?) {
        if (product != null) {
            viewModel.deleteProduct(product)
        } else if (firebaseProduct != null) {
            deleteFirebaseProductData(firebaseProduct)
        }
    }

    override fun setupRecyclerView(
        products: List<Product>?,
        firebaseProducts: List<FirebaseProduct>?,
        firebaseOrderContents: List<FirebaseOrderContent>?
    ) {
        products_recycler_view?.let { recyclerView ->
            recyclerView.layoutManager =
                LinearLayoutManager(requireContext())
            val productsInStockAdapter =
                ProductsInStockAdapter(
                    requireContext(),
                    ::deleteProduct,
                    ::openEditProductTab
                )
            recyclerView.adapter = productsInStockAdapter

            // Observer on the LiveData
            // The onChanged() method fires when the observed data changes and the activity is
            // in the foreground.

            if (user == null) {
                productsInStockAdapter.setProducts(products, null)
                setupEmptyView(empty_view_products, products_recycler_view)
                //quantity > 5
            } else {
                val inStockProducts = firebaseProducts?.filter { it.quantity.toInt() > 5 }
                productsInStockAdapter.setProducts(null, inStockProducts)
                setupEmptyView(empty_view_products, products_recycler_view)
            }
        }
    }
}
