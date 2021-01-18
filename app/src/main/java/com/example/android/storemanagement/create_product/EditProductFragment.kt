package com.example.android.storemanagement.create_product

import android.text.Editable
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.updateFirebaseProduct
import com.example.android.storemanagement.PRODUCT_KEY
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.getFirebaseUserOrderContents
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.products_database.Product
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_create_product.*


class EditProductFragment : InfoProductFragment() {

    override val fragmentTitle: String = "Edit product"
    override val buttonText: String = "Save product"
    private val names: MutableList<String> = mutableListOf()
    val firebaseProductsList = mutableListOf<FirebaseProduct>()

    override fun onStart() {
        super.onStart()

        button_scan_barcode.visibility = View.INVISIBLE

        val product = arguments?.getSerializable(PRODUCT_KEY)
        if (product is Product) {
            product_name.setText(product.name)
            product_price.setText(product.price.toString())
            product_overcharge.setText(product.overcharge.toString())
            product_barcode.setText(product.barcode)
        } else if (product is FirebaseProduct) {
            user = Firebase.auth.currentUser
            getFirebaseUserOrderContents(user!!.uid, product.barcode, true) { _, _ ->
                disableEditFields()
            }
            product_name.setText(product.name)
            product_price.setText(product.price)
            product_overcharge.setText(product.overcharge)
            product_barcode.setText(product.barcode)
        }
        if (product_price.text.isNotEmpty() && product_overcharge.text.isNotEmpty()
        ) {
            //calculate percentage for percentage field
            val percentage: Float = (product_overcharge.text.toString()
                .toFloat() / product_price.text.toString().toFloat()) * 100
            product_overcharge_percentage.setText(
                percentage.toInt().toString()
            )
            lastPercentage = product_overcharge_percentage.text.toString().toInt()
        }
    }

    override fun onResume() {
        super.onResume()
        user = Firebase.auth.currentUser
        if (user == null) {
            productViewModel.allProducts.observe(this, Observer { products ->
                // Update the cached copy of the products in the adapter.
                products?.let {
                    names.clear()
                    val productsNames = it.map { product -> product.name }
                    names.addAll(productsNames)
                }
            })
        } else {
            getAllFirebaseProducts()
        }
    }

    private fun disableEditFields() {
        product_name.isEnabled = false
        product_price.isEnabled = false
        product_overcharge.isEnabled = false
        product_overcharge_percentage.isEnabled = false
        product_barcode.isEnabled = false
        button_add_product.isEnabled = false
    }

    private fun getAllFirebaseProducts() {
        super.onResume()
        if (user != null) {
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
                            }
                        }
                    }
                    activity?.runOnUiThread {
                        firebaseProductsList.let {
                            names.clear()
                            val productsNames = it.map { product -> product.name }
                            names.addAll(productsNames)
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
                        if (!firebaseProductsList.contains(product)) {
                            firebaseProductsList.add(product)
                            activity?.runOnUiThread {
                                names.add(product.name)
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
                        if (names.contains(changedProduct.name))
                            activity?.runOnUiThread {
                                names.remove(changedProduct.name)
                            }
                        firebaseProductsList.add(product)
                        activity?.runOnUiThread {
                            names.add(product.name)
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
                        if (firebaseProductsList.contains(product)) {
                            firebaseProductsList.remove(product)
                            activity?.runOnUiThread {
                                names.remove(product.name)
                            }
                        }
                    }
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    override fun isBarcodeDuplicated(barcode: String): Boolean = false
    override fun isNameDuplicated(name: String): Boolean = names.any { currentName -> currentName == name }

    override fun onButtonClicked(name: Editable, price: Editable, overcharge: Editable, barcode: Editable) {
        if (user == null) {
            productViewModel.updateName(barcode.toString(), name.toString())
            productViewModel.updatePrice(name.toString(), price.toString().toFloat())
            productViewModel.updateOvercharge(name.toString(), overcharge.toString().toFloat())
        } else {
            updateFirebaseProduct(barcode.toString(), name.toString(), overcharge.toString(), price.toString())
        }
        parentFragmentManager.popBackStackImmediate()
    }
}