//package com.example.android.storemanagement
//
//import android.arch.lifecycle.LiveData
//import android.content.Context
//import android.support.annotation.WorkerThread
//import android.support.v7.widget.RecyclerView
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import com.example.android.storemanagement.database.ProductViewModel
//import kotlinx.android.synthetic.main.product_item.view.*
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import android.support.v4.app.FragmentActivity
//import android.arch.lifecycle.ViewModelProviders
//import android.R.attr.name
//
//
//
//class ProductsInStockAdapter(private val context: Context) :
//    RecyclerView.Adapter<ProductsHolder>() {
//
////    var inStockProducts: LiveData<List<Product>>
////    var lowStockProducts: LiveData<List<Product>>
//    private var productViewModel: ProductViewModel? = null
//
//    private var products = emptyList<Product>() // Cached copy of products
//    private var inStockProducts = emptyList<Product>()
//
//
//    // Gets the number of items in the list
//    override fun getItemCount(): Int {
//        return inStockProducts.size
//    }
//
//    // Inflates the item views
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsHolder {
//        return ProductsHolder(LayoutInflater.from(context).inflate(R.layout.product_item, parent, false))
//    }
//
//    // Binds each product in the list to a view
//    override fun onBindViewHolder(holder: ProductsHolder, position: Int) {
//        val current = inStockProducts[position]
//        holder.productType.text = current.name
//        holder.productPrice.text = current.price.toString()
//    }
//
//    internal fun setProducts(products: List<Product>) {
//        this.products = products
//        notifyDataSetChanged()
//    }
//
//    fun getProductAtPosition(position: Int): Product {
//        return products[position]
//    }
//
//    fun getInStockProducts(){
//        productViewModel = ViewModelProviders.of(context as FragmentActivity).get(ProductViewModel::class.java)
//        inStockProducts = productViewModel!!.getInStockProducts() as List<Product>
//    }
//}

//class ProductsInStockHolder(textView: View) : RecyclerView.ViewHolder(textView) {
//    // Holds the ProductTextView that will add each product to
//    val productType = textView.product_item_text!!
//    val productPrice = textView.product_item_price!!
//}
