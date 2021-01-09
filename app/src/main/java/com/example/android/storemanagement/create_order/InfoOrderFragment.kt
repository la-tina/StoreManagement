package com.example.android.storemanagement.create_order

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.OrderViewModel
import com.example.android.storemanagement.products_database.ProductViewModel
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_create_order.*
import kotlinx.android.synthetic.main.fragment_create_product.*

abstract class InfoOrderFragment: Fragment()  {

    abstract var fragmentTitle: String
    abstract var buttonText: String
    protected var firebaseProductsList = mutableListOf<FirebaseProduct>()

    abstract fun setupRecyclerView()

//    private var ordersViewModel: OrderViewModel  by lazy {
//        ViewModelProviders.of(this).get(OrderViewModel::class.java)
//    }

    protected fun getFirebaseProducts(userId: String) {
//        val uniqueId: String = user?.uid!!
        val database = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("Products").child(userId)

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
        setupRecyclerView()
    }

    protected val ordersViewModel : OrderViewModel by lazy {
        ViewModelProviders.of(this).get(OrderViewModel::class.java)
    }

    protected val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel(requireActivity().application)::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflatedView = inflater.inflate(R.layout.fragment_create_order, container, false)
        val toolbar: Toolbar = inflatedView.findViewById(R.id.toolbarTopOrder)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back)
        toolbar.setNavigationOnClickListener{
            parentFragmentManager.popBackStackImmediate()
        }
        return inflatedView
    }


    override fun onStart() {
        super.onStart()
        toolbarTopOrder.title = fragmentTitle
        button_add_order.text = buttonText
    }

    protected var finalPrice: Float = 0F

    protected fun updateFinalPrice(price: Float) {
        Log.d("Tina", "final price before $finalPrice")
        finalPrice = price
        final_price.text = String.format("%.1f", finalPrice).toFloat().toString()
        Log.d("Tina", "final price after $finalPrice")
    }

    protected fun setupEmptyView(emptyView: View, recyclerView: RecyclerView) {
        val products = recyclerView.adapter!!
        if (products.itemCount == 0) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }

    protected fun setOrderButtonEnabled(enabled: Boolean) {
        button_add_order.isEnabled = enabled
    }
}