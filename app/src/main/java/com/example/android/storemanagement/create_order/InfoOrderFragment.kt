package com.example.android.storemanagement.create_order

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.R
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.OrderViewModel
import com.example.android.storemanagement.products_database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_create_order.*
import kotlinx.android.synthetic.main.fragment_create_product.*

abstract class InfoOrderFragment: Fragment()  {

    abstract val fragmentTitle: String
    abstract val buttonText: String

    abstract fun setupRecyclerView()

//    private var ordersViewModel: OrderViewModel  by lazy {
//        ViewModelProviders.of(this).get(OrderViewModel::class.java)
//    }

    protected val ordersViewModel : OrderViewModel by lazy {
        ViewModelProviders.of(this).get(OrderViewModel::class.java)
    }

    protected val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel(requireActivity().application)::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_create_order, container, false)

    override fun onStart() {
        super.onStart()
        toolbarTopOrder.title = fragmentTitle
        button_add_order.text = buttonText
    }

    protected var finalPrice: Float = 0F

    protected fun updateFinalPrice(price: Float) {
        finalPrice += price
        final_price.text = String.format("%.1f", finalPrice).toFloat().toString()
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