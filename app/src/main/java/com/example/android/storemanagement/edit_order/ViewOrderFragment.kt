package com.example.android.storemanagement.edit_order

import android.arch.lifecycle.Observer
import android.view.View
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import kotlinx.android.synthetic.main.fragment_create_order.*

class ViewOrderFragment: EditOrderFragment()  {

    override val fragmentTitle: String = "View Order"
    override val buttonText: String = "Ok"

    private lateinit var orderContentViewModel: OrderContentViewModel

    override fun onStart() {
        super.onStart()

        button_add_order.visibility = View.GONE

        orderContentViewModel.allOrderContents.observe(this, Observer { viewState ->
            currentOrderContents = viewState?.filter { it.orderId == currentOrder.id }
            setupRecyclerView()
        })
    }
}