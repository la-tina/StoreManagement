package com.example.android.storemanagement.edit_order

import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.ORDER_KEY
import com.example.android.storemanagement.R
import com.example.android.storemanagement.create_order.InfoOrderFragment
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_tab.OrderStatus
import kotlinx.android.synthetic.main.fragment_create_order.*

open class EditOrderFragment : InfoOrderFragment() {

    override var fragmentTitle: String = ""
    override var buttonText: String = "Save"
    var currentOrderContents: List<OrderContent>? = null

    lateinit var currentOrder: Order

    private val orderContentViewModel: OrderContentViewModel by lazy {
        ViewModelProviders.of(this)
            .get(OrderContentViewModel(requireActivity().application)::class.java)
    }

    override fun onStart() {
        super.onStart()
//        imageView.setImageDrawable("lemon.png")
        orderContentViewModel.allOrderContents.observe(this, Observer { viewState ->
            onOrderContentsChanged(viewState)
        })
    }

    private fun onOrderContentsChanged(viewState: List<OrderContent>?) {
        val order: Order = arguments?.getSerializable(ORDER_KEY) as Order
        setupRecyclerView()

        currentOrder = order
        toolbarTopOrder.title = if (!canOrderBeEdited()) "Order Content" else "Edit Order"
        info_text?.text =
            if (!canOrderBeEdited()) context?.getString(R.string.view_order_info) else context?.getString(R.string.edit_order_info)
//        if (finalPrice == 0F){
//            finalPrice = currentOrder.finalPrice
//        }
        Log.d("Tina", "final price edit order " + currentOrder.finalPrice)
        final_price.text = currentOrder.finalPrice.toString()

        currentOrderContents = viewState?.filter { it.orderId == order.id }

        setupRecyclerView()

        if (!canOrderBeEdited()) {
            button_add_order.visibility = View.GONE
            constraintLayout.maxHeight = 160
        }
        button_add_order.setOnClickListener { onEditButtonClicked(order) }
    }

    private fun canOrderBeEdited(): Boolean =
        currentOrder.orderStatus == OrderStatus.PENDING.toString()

    private fun onEditButtonClicked(order: Order) {
        val quantities = (create_order_recycler_view.adapter as EditOrderAdapter).quantities

        quantities.forEach { (productName, quantity) ->
            updateQuantity(productName, quantity)
        }
//        orderContentViewModel.updateOrderFinalPrice(order.id, finalPrice)

//        updateFinalPrice()
        ordersViewModel.updateFinalPrice(finalPrice, order.id)
        parentFragmentManager.popBackStackImmediate()
    }

    private fun deleteOrderContent() {

//        currentOrderContents?.filter { it.quantity != 0 }?.forEach {newOrderContent ->
//            newOrderContents?.add(newOrderContent)
//        }
        currentOrderContents?.filter { it.quantity == 0 }?.forEach { orderContent ->
            orderContentViewModel.deleteOrderContent(orderContent)
        }
    }


    private fun updateQuantity(productName: String, editedQuantity: Int) {
        val currentProduct = productViewModel.allProducts.value
            ?.first { product -> product.name == productName }

        val currentQuantity = currentProduct!!.quantity

        val orderedQuantity = orderContentViewModel.allOrderContents.value
            ?.first { product -> product.productBarcode == currentProduct.barcode }!!.quantity

        val currentBarcode = orderContentViewModel.allOrderContents.value
            ?.first { product -> product.productBarcode == currentProduct.barcode }!!.productBarcode

        val finalQuantity: Int = if (orderedQuantity > editedQuantity)
            currentQuantity - (orderedQuantity - editedQuantity)
        else currentQuantity + (editedQuantity - orderedQuantity)

//        productViewModel.updateQuantity(productName, finalQuantity)
        if (editedQuantity == 0) {
            deleteOrderContent()
        } else {
            orderContentViewModel.updateQuantity(currentBarcode, editedQuantity)
        }
    }

    override fun setupRecyclerView() {
        create_order_recycler_view.layoutManager =
            LinearLayoutManager(requireContext())

        val editOrdersAdapter = EditOrderAdapter(
            requireContext(),
            ::updateFinalPrice,
            ::setOrderButtonEnabled,
            ::canOrderBeEdited
        )
        create_order_recycler_view.adapter = editOrdersAdapter

        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        editOrdersAdapter.setProductsInOrder(currentOrderContents)
        setupEmptyView(empty_view_create_order, info_text, create_order_recycler_view)

        productViewModel.allProducts.observe(this, Observer { products ->
            products?.let {
                editOrdersAdapter.setProducts(it)
            }
        })
    }
}