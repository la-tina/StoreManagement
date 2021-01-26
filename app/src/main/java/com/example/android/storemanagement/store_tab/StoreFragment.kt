package com.example.android.storemanagement.store_tab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseDatabaseProductsOperations.updateFirebaseProductQuantity
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_database.ProductViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.create_order_item.*
import kotlinx.android.synthetic.main.fragment_store.*
import kotlinx.android.synthetic.main.store_item.*

class StoreFragment : Fragment() {
    companion object {
        const val KEY_QUANTITY_VALUE = "productQuantityValue"
    }

    protected var firebaseProductsList = mutableListOf<FirebaseProduct>()
    protected var user: FirebaseUser? = null
    lateinit var topToolbar: Toolbar
    private val allOrderContents = mutableListOf<OrderContent>()
    private lateinit var productViewModel: ProductViewModel

    private fun setupViewModel() {
        productViewModel =
            ViewModelProvider(this)
                .get(ProductViewModel(requireActivity().application)::class.java)
    }

    private var savedProductQuantity: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_store, container, false)

        if (savedInstanceState != null) {
            savedProductQuantity =
                savedInstanceState.getCharSequence(KEY_QUANTITY_VALUE)!!.toString()
        }
        return view
    }

    override fun onStart() {
        super.onStart()

        if (store_item_quantity != null && savedProductQuantity.isNotBlank())
            store_item_quantity.setText(savedProductQuantity)

        store_info_text?.text = context?.getString(R.string.store_info)
        topToolbar = view!!.findViewById(R.id.toolbarTop)
        topToolbar.inflateMenu(R.menu.products_filer_menu)
        topToolbar.overflowIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_baseline_filter_alt)

        button_save_quantity.setOnClickListener {
            val quantities: MutableMap<String, Int> =
                (store_recycler_view.adapter as StoreAdapter).quantities
            if (user == null) {
                quantities.forEach { (barcode, quantity) ->
                    updateQuantity(barcode, quantity)
                    Toast.makeText(requireContext(), "Quantity updated.", Toast.LENGTH_SHORT).show()
                }
            } else {
                quantities.forEach { (barcode, quantity) ->
                    updateFirebaseQuantity(barcode, quantity)
                    Toast.makeText(requireContext(), "Quantity updated.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        user = Firebase.auth.currentUser
        if (user != null) {
            getFirebaseProducts()
        } else {
            setupViewModel()
            productViewModel.allProducts.observe(this, Observer { products ->
                // Update the cached copy of the words in the adapter.
                Log.d("TinaAllProducts", "store products $products")
                products?.let {
                    setupRecyclerView(products = products)
                }
            })
        }

        topToolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
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

    private fun getFirebaseProducts() {
        // Get a reference to our posts
        val uniqueId: String = user?.uid!!
        val database = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("Products").child(uniqueId)

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                firebaseProductsList.clear()
                val firebaseProducts = dataSnapshot.children

                for (item in firebaseProducts) {
                    val firebaseProduct: FirebaseProduct? =
                        item.getValue(FirebaseProduct::class.java)

                    if (firebaseProduct != null) {
                        val product = FirebaseProduct(
                            firebaseProduct.name,
                            firebaseProduct.price,
                            firebaseProduct.overcharge,
                            firebaseProduct.barcode,
                            firebaseProduct.quantity,
                            item.key!!
                        )
                        Log.d("TinaFirebase", "firebaseProduct onDataChange $product")
                        if (!firebaseProductsList.contains(product)) {
                            firebaseProductsList.add(product)
                            activity?.runOnUiThread {
                                setupRecyclerView(firebaseProducts = firebaseProductsList)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val firebaseNewProduct: FirebaseProduct? =
                    dataSnapshot.getValue(FirebaseProduct::class.java)
                if (firebaseNewProduct != null) {
                    val product = FirebaseProduct(
                        firebaseNewProduct.name,
                        firebaseNewProduct.price,
                        firebaseNewProduct.overcharge,
                        firebaseNewProduct.barcode,
                        firebaseNewProduct.quantity,
                        dataSnapshot.key!!
                    )
                    Log.d("TinaFirebase", "firebaseProduct onChildAdded $product")
                    if (firebaseProductsList.none { it.barcode == product.barcode }) {
                        firebaseProductsList.add(product)
                        activity?.runOnUiThread {
                            setupRecyclerView(firebaseProducts = firebaseProductsList)
                        }
                    }
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val changedFirebaseProduct: FirebaseProduct? =
                    dataSnapshot.getValue(FirebaseProduct::class.java)
                if (changedFirebaseProduct != null) {
                    val product = FirebaseProduct(
                        changedFirebaseProduct.name,
                        changedFirebaseProduct.price,
                        changedFirebaseProduct.overcharge,
                        changedFirebaseProduct.barcode,
                        changedFirebaseProduct.quantity,
                        dataSnapshot.key!!
                    )
                    Log.d("TinaFirebase", "firebaseProduct onChildAdded $product")
                    val changedProduct =
                        firebaseProductsList.first { t -> t.id == dataSnapshot.key!! }
                    firebaseProductsList.remove(changedProduct)
                    firebaseProductsList.add(product)
                    activity?.runOnUiThread {
                        setupRecyclerView(firebaseProducts = firebaseProductsList)
                    }
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val firebaseRemovedProduct: FirebaseProduct? =
                    dataSnapshot.getValue(FirebaseProduct::class.java)
                if (firebaseRemovedProduct != null) {
                    val product = FirebaseProduct(
                        firebaseRemovedProduct.name,
                        firebaseRemovedProduct.price,
                        firebaseRemovedProduct.overcharge,
                        firebaseRemovedProduct.barcode,
                        firebaseRemovedProduct.quantity,
                        dataSnapshot.key!!
                    )
                    Log.d("TinaFirebase", "firebaseProduct onChildRemoved $product")
                    if (!firebaseProductsList.none { it.barcode == product.barcode }) {
                        firebaseProductsList.remove(product)
                        activity?.runOnUiThread {
                            setupRecyclerView(firebaseProducts = firebaseProductsList)
                        }
                    }
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        setupRecyclerView(firebaseProducts = firebaseProductsList)
    }

    private fun filterByAscendingQuantity() {
        if (user != null) {
            val ascendingQuantityComparator = compareBy<FirebaseProduct> { it.quantity.toInt() }
            val sortedOrderContentsList =
                firebaseProductsList.sortedWith(ascendingQuantityComparator)
            setupRecyclerView(firebaseProducts = sortedOrderContentsList)
        } else {
            productViewModel.allProducts.observe(this, Observer { products ->
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
            productViewModel.allProducts.observe(this, Observer { products ->
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
            productViewModel.allProducts.observe(this, Observer { products ->
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
            productViewModel.allProducts.observe(this, Observer { products ->
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
            productViewModel.allProducts.observe(this, Observer { products ->
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
            productViewModel.allProducts.observe(this, Observer { products ->
                // Update the cached copy of the words in the adapter.
                products?.let {
                    val descendingPriceComparator = compareByDescending<Product> { it.price }
                    val sortedProductList = products.sortedWith(descendingPriceComparator)
                    setupRecyclerView(products = sortedProductList)
                }
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(KEY_QUANTITY_VALUE, product_item_quantity?.text)
    }

    private fun updateQuantity(barcode: String, quantity: Int) {
        productViewModel.updateQuantity(barcode, quantity)
    }

    private fun updateFirebaseQuantity(barcode: String, quantity: Int) {
        updateFirebaseProductQuantity(barcode, quantity.toString())
    }

    private fun setupRecyclerView(
        products: List<Product>? = null,
        firebaseProducts: List<FirebaseProduct>? = null,
    ) {
        store_recycler_view.layoutManager =
            LinearLayoutManager(requireContext())
        val storeAdapter = StoreAdapter(requireContext(), ::setOrderButtonEnabled)
        store_recycler_view.adapter = storeAdapter

        if (user == null) {
            storeAdapter.setProducts(products, null)
            setupEmptyView(products?.size!!)
        } else {
            storeAdapter.setProducts(null, firebaseProducts)
            setupEmptyView(firebaseProducts?.size!!)
        }
    }

    private fun setupEmptyView(productsSize: Int) {
        if (productsSize == 0) {
            store_recycler_view.visibility = View.GONE
            empty_view_products_store.visibility = View.VISIBLE
        } else {
            store_recycler_view.visibility = View.VISIBLE
            empty_view_products_store.visibility = View.GONE
        }
    }

    private fun setOrderButtonEnabled(enabled: Boolean) {
        button_save_quantity.isEnabled = enabled
    }
}
