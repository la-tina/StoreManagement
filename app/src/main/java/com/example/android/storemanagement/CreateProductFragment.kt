package com.example.android.storemanagement


import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.android.storemanagement.database.ProductViewModel
import kotlinx.android.synthetic.main.fragment_create_product.*


class CreateProductFragment : Fragment() {

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProviders.of(this).get(ProductViewModel(requireActivity().application)::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_product, container, false)
    }

    override fun onStart() {
        super.onStart()

        onEditTextChanged(product_name, product_price, product_overcharge, product_barcode)

        button_add_product.setOnClickListener {

            val productName = product_name!!.text.toString()
            val productPrice = product_price!!.text.toString()
            val productOvercharge = product_overcharge!!.text.toString()
            val productBarcode = product_barcode!!.text.toString()

            val quantity = 0
            val product = Product(
                productName,
                productPrice.toFloat(),
                productOvercharge.toFloat(),
                productBarcode.toLong(),
                quantity
            )
            productViewModel.insert(product)

            fragmentManager?.popBackStackImmediate()
        }
    }

    private fun onEditTextChanged(
        productName: EditText,
        productPrice: EditText,
        productOvercharge: EditText,
        productBarcode: EditText
    ) {
        product_name.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                button_add_product.isEnabled = !(productName.toString().isEmpty() &&
                        productPrice.toString().isEmpty() &&
                        productOvercharge.toString().isEmpty() &&
                        productBarcode.toString().isEmpty())
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
                button_add_product.isEnabled = false
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    override fun onResume() {
        super.onResume()
        name_text.isHintAnimationEnabled = true
    }

}


