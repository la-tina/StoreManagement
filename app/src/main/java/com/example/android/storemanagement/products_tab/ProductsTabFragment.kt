package com.example.android.storemanagement.products_tab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.android.storemanagement.EDIT_PRODUCT_TAB
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseOrderContent
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.OrderViewModel
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_database.ProductViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_products_container.*

abstract class ProductsTabFragment : Fragment() {

    protected var listener: OnNavigationChangedListener? = null

    protected var firebaseProductsList = mutableListOf<FirebaseProduct>()
    protected var firebaseOrderContentsList = mutableListOf<FirebaseOrderContent>()
    protected var user: FirebaseUser? = null

    protected val orderContentViewModel: OrderContentViewModel by lazy {
        ViewModelProviders.of(this).get(OrderContentViewModel::class.java)
    }

    protected val orderViewModel: OrderViewModel by lazy {
        ViewModelProviders.of(this).get(OrderViewModel::class.java)
    }

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel::class.java)
    }

    private val allOrderContents = mutableListOf<OrderContent>()

    abstract fun setOnNavigationChangedListener(onNavigationChangedListener: OnNavigationChangedListener)

    abstract fun deleteProduct(product: Product?, firebaseProduct: FirebaseProduct?)

    abstract fun setupViewModel()

    abstract fun setupRecyclerView()

    protected fun getFirebaseProducts(
        database: FirebaseDatabase,
        uniqueId: String
    ) {
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
                        if (firebaseProductsList.none { it.barcode == product.barcode }) {
                            firebaseProductsList.add(product)
                            activity?.runOnUiThread {
                                setupRecyclerView()
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
                            setupRecyclerView()
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
                        setupRecyclerView()
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
                            setupRecyclerView()
                        }
                    }
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_products_container, container, false)

    protected fun openEditProductTab(product: Product?, firebaseProduct: FirebaseProduct?) {
        listener?.onNavigationChanged(
            tabNumber = EDIT_PRODUCT_TAB,
            product = product,
            firebaseProduct = firebaseProduct
        )
    }

    protected fun setupEmptyView(emptyView: View, recyclerView: RecyclerView) {
        val products = recyclerView.adapter!!
        if (products.itemCount == 0) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            info_text.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }
}