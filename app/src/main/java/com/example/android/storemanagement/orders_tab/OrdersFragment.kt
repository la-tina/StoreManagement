package com.example.android.storemanagement.orders_tab


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.*
import com.example.android.storemanagement.order_content_database.OrderContent
import com.example.android.storemanagement.order_content_database.OrderContentViewModel
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.orders_database.OrderViewModel
import com.example.android.storemanagement.products_database.Product
import com.example.android.storemanagement.products_database.ProductViewModel
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_orders.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


open class OrdersFragment : Fragment() {

    var listener: OnNavigationChangedListener? = null
    private lateinit var topToolbar: Toolbar

    lateinit var ordersList: List<Order>
    lateinit var productsInOrderList: List<OrderContent>
    lateinit var productsList: List<Product>

    private val orderViewModel: OrderViewModel by lazy {
        ViewModelProviders.of(this).get(OrderViewModel::class.java)
    }

    private val orderContentViewModel: OrderContentViewModel by lazy {
        ViewModelProviders.of(this).get(OrderContentViewModel::class.java)
    }

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel::class.java)
    }

    lateinit var onNavigationChangedListener: OnNavigationChangedListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(
            R.layout.fragment_orders,
            container,
            false
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    private fun initData() {
        orderViewModel.allOrders.toSingleEvent().observe(this,
            Observer<List<Order>> { orders ->
                ordersList = orders
            }
        )
        orderContentViewModel.allOrderContents.toSingleEvent().observe(this,
            Observer<List<OrderContent>> { productsInOrder ->
                productsInOrderList = productsInOrder
            }
        )
        productViewModel.allProducts.toSingleEvent().observe(this,
            Observer<List<Product>> { products ->
                productsList = products
            }
        )
    }

    open fun addRowOrders(writeRow: (List<Any?>) -> Unit) {
        ordersList.forEach { order ->
            val orderContents = productsInOrderList.filter { it.orderId == order.id }
            orderContents.forEach { orderContent ->
                val products = productsList.filter { it.barcode == orderContent.productBarcode }
                products.forEach { product ->
                    writeRow(
                        listOf(
                            "",
                            order.id,
                            order.isOrdered,
                            order.date,
                            product.name,
                            orderContent.quantity,
                            product.price * orderContent.quantity
                        )
                    )
                }
            }
            writeRow(listOf("Final price"," - "," - "," - "," - "," - ", order.finalPrice))
            writeRow(emptyList())
        }
    }

    open fun addRowProducts(writeRow: (List<Any?>) -> Unit) {
        productsList.forEach { product ->
            writeRow(
                listOf(
                    product.barcode,
                    product.name,
                    product.price,
                    product.overcharge,
                    product.quantity
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        orders_add_button?.setOnClickListener {
            if (::onNavigationChangedListener.isInitialized)
                onNavigationChangedListener.onNavigationChanged(CREATE_ORDER_TAB)
        }
    }

    private fun updateDate(id: Long){
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDate = current.format(formatter)

        orderViewModel.updateDate(formattedDate, id)
    }

    private fun updateOrderStatus(id: Long, isOrdered: Boolean){
        orderViewModel.updateOrderStatus(id, isOrdered)
        updateDate(id)
    }

    private fun deleteOrder(order: Order) {
//        orderContentViewModel.allOrderContents.toSingleEvent()
//            .observe(this, Observer<List<OrderContent>> { allContents ->
//                productViewModel.allProducts.toSingleEvent()
//                    .observe(this, Observer<List<Product>> { allProducts ->
//
//                        allContents?.filter { it.orderId == order.id }?.forEach { currentProduct ->
//
//                            val previousQuantity =
//                                allProducts?.firstOrNull { it.barcode == currentProduct.productBarcode }?.quantity
//
//                            val newQuantity =
//                                if (previousQuantity != null)
//                                    previousQuantity - currentProduct.quantity
//                                else
//                                    currentProduct.quantity
//
//                            val productQuantity = if (newQuantity <= 0) 0 else newQuantity
//
//                            productViewModel.updateProductQuantity(
//                                currentProduct.productBarcode,
//                                productQuantity
//                            )
//                        }
//
//                    })
                orderViewModel.deleteOrder(order)
//            })
    }

//    private fun updateProductQuantities(orderContent: OrderContent) {
//        productViewModel.allProducts.observe(this, Observer { allProducts ->
//            allProducts?.forEach { product ->
//
//                if (orderContent.productBarcode == product.barcode) {
//                    productViewModel.updateProductQuantity(
//                        product.barcode, product.quantity - orderContent.quantity
//                    )
//                }
//            }
//        })
//    }

    private fun openEditOrderTab(order: Order) {
        if (::onNavigationChangedListener.isInitialized) {
            //onNavigationChangedListener.onNavigationChanged(EDIT_ORDER_TAB)
            listener = onNavigationChangedListener
        }

        listener?.onNavigationChanged(tabNumber = EDIT_ORDER_TAB, order = order)
    }

    private fun setupEmptyView() {
        val orders = orders_recycler_view.adapter!!
        if (orders.itemCount == 0) {
            orders_recycler_view.visibility = View.GONE
            empty_view_orders.visibility = View.VISIBLE
        } else {
            orders_recycler_view.visibility = View.VISIBLE
            empty_view_orders.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        orders_recycler_view.layoutManager =
            LinearLayoutManager(requireContext())
        val ordersAdapter = OrdersAdapter(requireContext(), ::deleteOrder, ::openEditOrderTab, ::updateOrderStatus)
        orders_recycler_view.adapter = ordersAdapter

        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        orderViewModel.allOrders.observe(this, Observer { orders ->
            // Update the cached copy of the words in the adapter.
            orders?.let {
                ordersAdapter.setOrders(it)
                setupEmptyView()
            }
        })
    }
}



