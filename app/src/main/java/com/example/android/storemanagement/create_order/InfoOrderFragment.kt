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
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.OrderViewModel
import com.example.android.storemanagement.products_database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_create_order.*
import kotlinx.android.synthetic.main.fragment_create_product.*

abstract class InfoOrderFragment: Fragment()  {

    abstract var fragmentTitle: String
    abstract var buttonText: String

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