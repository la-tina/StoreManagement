package com.example.android.storemanagement.products_tab

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.EDIT_PRODUCT_TAB
import com.example.android.storemanagement.OnNavigationChangedListener
import com.example.android.storemanagement.R
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.OrderViewModel
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_products_container.*

abstract class ProductsTabFragment : Fragment() {

    protected var listener: OnNavigationChangedListener? = null

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

    abstract fun deleteProduct(product: Product)

    abstract fun setupViewModel()

    abstract fun setupRecyclerView()

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()
        setupViewModel()
        setupRecyclerView()
        observeOrderContentViewModel()
    }

    private fun observeOrderContentViewModel() {
        orderContentViewModel.allOrderContents.observe(this, Observer { allOrderContents ->
            this.allOrderContents.clear()
            allOrderContents?.let {
                this.allOrderContents.addAll(it)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_products_container, container, false)

    protected fun openEditProductTab(product: Product) {
        listener?.onNavigationChanged(tabNumber = EDIT_PRODUCT_TAB, product = product)
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

    protected fun getProductQuantity(product: Product): Int {
        val productQuantity = allOrderContents.filter { it.productBarcode == product.barcode }.map { it.quantity }.sum()
        productViewModel.updateQuantity(product.name, productQuantity)
        return productQuantity
    }
}