package com.example.android.storemanagement.create_order


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.storemanagement.R
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_database.OrderViewModel
import com.example.android.storemanagement.products_database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_create_order.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateOrderFragment : Fragment() {

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel(requireActivity().application)::class.java)
    }

    lateinit var ordersViewModel: OrderViewModel

    var finalPrice: Float = 0F

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_create_order, container, false)

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

            val order = Order(finalPrice, formattedDate)
            ordersViewModel.insert(order)

            fragmentManager?.popBackStackImmediate()
        }
    }

    private fun updateFinalPrice(price: Float) {
        finalPrice += price // finalPrice + string
        final_price.text = String.format("%.1f", finalPrice).toFloat().toString()
    }

    private fun updateQuantity(productName: String, quantity: Int) {
        val currentQuantity = productViewModel.allProducts.value
            ?.first { product -> product.name == productName }?.quantity

        val finalQuantity = if (currentQuantity != null) quantity + currentQuantity else quantity

        productViewModel.updateQuantity(productName, finalQuantity)
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

        val createOrdersAdapter = CreateOrderAdapter(
            requireContext(),
            ::updateFinalPrice,
            ::setOrderButtonEnabled
        )
        create_order_recycler_view.adapter = createOrdersAdapter

        ordersViewModel = ViewModelProviders.of(this).get(OrderViewModel::class.java)

        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        productViewModel.allProducts.observe(this, Observer { products ->
            // Update the cached copy of the products in the adapter.
            products?.let {
                createOrdersAdapter.setProducts(it)
                setupEmptyView()
            }
        })
    }

    private fun setOrderButtonEnabled(enabled: Boolean) {
        button_add_order.isEnabled = enabled
    }
}