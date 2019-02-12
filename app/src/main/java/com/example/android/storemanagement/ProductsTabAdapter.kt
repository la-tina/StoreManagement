package com.example.android.storemanagement

import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import java.nio.file.Files.size
import android.support.v4.app.FragmentStatePagerAdapter


class ProductsTabAdapter internal constructor(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> ProductsFragment()
            1 -> ProductsInStock()
            else -> {
                return ProductsLowStock()
            }
        }
    }

    override fun getCount(): Int {
        return 3
    }

//    override fun getPageTitle(position: Int): CharSequence {
//        return when (position) {
//            0 -> "First Tab"
//            1 -> "Second Tab"
//            else -> {
//                return "Third Tab"
//            }
//        }
//    }
}