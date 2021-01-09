package com.example.android.storemanagement.create_product

import android.text.Editable
import android.util.Log
import androidx.lifecycle.Observer
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations
import com.example.android.storemanagement.firebase.FirebaseProduct
import com.example.android.storemanagement.products_database.Product
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase


open class CreateProductFragment : InfoProductFragment() {

    override val fragmentTitle: String = "Create Product"
    override val buttonText: String = "Add Product"

    private val barcodes: MutableList<String> = mutableListOf()
    private val names: MutableList<String> = mutableListOf()
    val firebaseProductsList = mutableListOf<FirebaseProduct>()

    override fun onResume() {
        super.onResume()
        user = Firebase.auth.currentUser
        if (user == null) {
            productViewModel.allProducts.observe(this, Observer { products ->
                // Update the cached copy of the products in the adapter.
                products?.let {
                    barcodes.clear()
                    names.clear()
                    val productsBarcodes = it.map { product -> product.barcode }
                    barcodes.addAll(productsBarcodes)
                    val productsNames = it.map { product -> product.name }
                    names.addAll(productsNames)
                }
            })
        } else {
            getAllFirebaseProducts()
        }
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
                            barcodes.clear()
                            names.clear()
                            val productsBarcodes = it.map { product -> product.barcode }
                            barcodes.addAll(productsBarcodes)
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
                                barcodes.add(product.barcode)
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
                            barcodes.add(product.barcode)
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
                                barcodes.remove(product.barcode)
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

    override fun onButtonClicked(
        name: Editable,
        price: Editable,
        overcharge: Editable,
        barcode: Editable
    ) {
        val quantity = 0
        if (user == null) {
            val product = Product(
                name.toString(),
                price.toString().toFloat(),
                overcharge.toString().toFloat(),
                barcode.toString(),
                quantity
            )
            productViewModel.insert(product)
        } else {
            val firebaseProduct = FirebaseProduct(
                name.toString(),
                price.toString(),
                overcharge.toString(),
                barcode.toString(),
                quantity.toString(),
                ""
            )
            FirebaseDatabaseOperations.addFirebaseProduct(firebaseProduct)
        }
        parentFragmentManager.popBackStackImmediate()
    }

    override fun isBarcodeDuplicated(barcode: String) =
        barcodes.any { currentBarcode -> currentBarcode == barcode }

    override fun isNameDuplicated(name: String) =
        names.any { currentName -> currentName == name }
}