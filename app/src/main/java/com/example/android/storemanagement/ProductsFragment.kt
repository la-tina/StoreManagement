package com.example.android.storemanagement

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.android.storemanagement.database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_products.*



class ProductsFragment : Fragment() {
    lateinit var onNavigationChangedListener: OnNavigationChangedListener

    private val data : List<Product> = listOf()

    lateinit var viewModel: ProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onStart() {
        super.onStart()

        products_recycler_view.layoutManager = LinearLayoutManager(requireContext())

        val productsAdapter = ProductsAdapter(requireContext())

        products_recycler_view.adapter = productsAdapter

        viewModel = ViewModelProviders.of(this).get(ProductViewModel::class.java)


        // Observer on the LiveData
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        viewModel.allProducts.observe(this, Observer{ products ->
            // Update the cached copy of the words in the adapter.
            products?.let { productsAdapter.setProducts(it) }
        })

    }

    override fun onResume() {
        super.onResume()
        activity?.products_add_button?.setOnClickListener {
            if (::onNavigationChangedListener.isInitialized)
                onNavigationChangedListener.onNavigationChanged(1)
        }
    }

}



