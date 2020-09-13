package com.example.android.storemanagement

import LoginViewModel
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.example.android.storemanagement.orders_database.Order
import com.example.android.storemanagement.products_database.Product
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient

class LoginActivity : AppCompatActivity(), OnNavigationChangedListener {

    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Stetho.initializeWithDefaults(applicationContext)

        OkHttpClient.Builder()
            .addNetworkInterceptor(StethoInterceptor())
            .build()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val firstFragment = supportFragmentManager?.fragments?.first()
        if (firstFragment != null) {
            supportFragmentManager.putFragment(outState, "FragmentName", firstFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BARCODE_ACTIVITY_REQUEST_CODE) {
            currentFragment?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onNavigationChanged(tabNumber: Int, product: Product?, order: Order?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


