package com.example.android.storemanagement


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.database.ProductViewModel
import com.example.android.storemanagement.orders_database.OrderViewModel
import kotlinx.android.synthetic.main.create_order_item.*
import kotlinx.android.synthetic.main.create_order_item.view.*
import kotlinx.android.synthetic.main.fragment_create_order.*
import kotlinx.android.synthetic.main.fragment_create_order.view.*
import kotlinx.android.synthetic.main.fragment_create_product.*
import kotlinx.android.synthetic.main.fragment_products.*
import kotlinx.android.synthetic.main.fragment_title.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CreateOrderFragment : Fragment() {

    lateinit var ordersViewModel: OrderViewModel
    lateinit var productsViewModel: ProductViewModel

    var finalPrice: Float = 0F
    //final_price!!.text.toString().toInt()

    //lateinit var productDao: ProductDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_order, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        setupRecyclerView()

        button_add_order.setOnClickListener {

            val quantities = (create_order_recycler_view.adapter as CreateOrderAdapter).quantities
            quantities.forEach { productName, quantity ->
                updateQuantity(productName, quantity)
            }

                  val current = LocalDate.now()
                  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd ")
                  val formattedDate = current.format(formatter)

                  //finalPrice = final_price!!.text.toString().toFloat()
                  val order = Order(finalPrice, formattedDate)
                  ordersViewModel.insert(order)

                fragmentManager?.popBackStackImmediate()
        }
    }

    private fun updateFinalPrice(price: Float) {
        finalPrice += price // finalPrice + string
        final_price.text = finalPrice.toString()
    }

    private fun updateQuantity(productName: String, quantity: Int) {
        productsViewModel.updateQuantity(productName, quantity)
    }

    private fun setupEmptyView() {
        val createOrder = create_order_recycler_view.adapter!!
        if (createOrder.itemCount == 0) {
            create_order_recycler_view.visibility = View.GONE
            empty_view_create_order.visibility = View.VISIBLE
        } else {
            create_order_recycler_view.visibility = View.VISIBLE
            empty_view_create_order.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        create_order_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        val createOrdersAdapter = CreateOrderAdapter(requireContext(), ::updateFinalPrice, ::setOrderButtonEnabled)
        create_order_recycler_view.adapter = createOrdersAdapter

        ordersViewModel = ViewModelProviders.of(this).get(OrderViewModel::class.java)

        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        productsViewModel.allProducts.observe(this, Observer { products ->
            // Update the cached copy of the products in the adapter.
            products?.let {
                createOrdersAdapter.setProducts(it)
                setupEmptyView()}
        })
    }

    private fun setOrderButtonEnabled(enabled: Boolean) {
        button_add_order.isEnabled = enabled
    }
}

