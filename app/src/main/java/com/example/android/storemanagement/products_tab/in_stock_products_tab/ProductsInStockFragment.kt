package com.example.android.storemanagement.products_tab.in_stock_products_tab

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.deleteFirebaseProductData
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_tab.ProductsTabFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_products_container.*

class ProductsInStockFragment : ProductsTabFragment() {

    private lateinit var viewModel: ProductsInStockViewModel

    override fun setupViewModel() {
        viewModel = ViewModelProviders.of(this)
            .get(ProductsInStockViewModel(requireActivity().application)::class.java)
    }

    override fun setOnNavigationChangedListener(onNavigationChangedListener: OnNavigationChangedListener) {
        listener = onNavigationChangedListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_products_container, container, false)

    override fun onStart() {
        super.onStart()
        info_text.text = context?.getString(R.string.in_stock_products_info)
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
            setupRecyclerView()
        }
    }

    override fun deleteProduct(product: Product?, firebaseProduct: FirebaseProduct?) {
        if (product != null) {
            viewModel.deleteProduct(product)
        } else if (firebaseProduct != null) {
            deleteFirebaseProductData(firebaseProduct)
        }
    }

    override fun setupRecyclerView() {
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
                viewModel.inStockProducts.observe(this, Observer { inStockProducts ->
                    // Update the cached copy of the words in the adapter.
                    inStockProducts?.let {
                        productsInStockAdapter.setProducts(it, null)
                        setupEmptyView(empty_view_products, products_recycler_view)
                        //quantity > 0
                    }
                })
            } else {
                val inStockProducts = firebaseProductsList.filter {it.quantity.toInt() > 5}
                productsInStockAdapter.setProducts(null, inStockProducts)
                setupEmptyView(empty_view_products, products_recycler_view)
            }
        }
    }
}
