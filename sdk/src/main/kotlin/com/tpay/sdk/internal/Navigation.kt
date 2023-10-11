package com.tpay.sdk.internal

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.tpay.sdk.R
import com.tpay.sdk.extensions.animate
import com.tpay.sdk.extensions.backStack
import javax.inject.Singleton

@Singleton
internal class Navigation {
    private lateinit var fragmentManager: FragmentManager

    internal fun init(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    fun changeFragment(
        fragment: Fragment,
        withAnim: Boolean = true,
        addToBackStack: Boolean = true
    ) {
        if(!fragmentManager.isDestroyed){
            fragmentManager.let {
                fragment.run {
                    it.beginTransaction()
                        .animate(withAnim)
                        .replace(R.id.bottomSheetContainer, this)
                        .backStack(addToBackStack)
                        .commit()
                }
            }
        }
    }

    fun getCurrentFragment(): Fragment? {
        return fragmentManager.fragments.lastOrNull()
    }

    fun fragmentListeners(onAttach: (Fragment) -> Unit, onBackStackChange: () -> Unit){
        fragmentManager.run {
            addOnBackStackChangedListener(onBackStackChange)
            addFragmentOnAttachListener { _, fragment ->
                onAttach.invoke(fragment)
            }
        }
    }

    fun onBackPressed(): Boolean {
        val fragment = fragmentManager.fragments.lastOrNull()
        val result = fragmentManager.popBackStackImmediate()
        fragment?.let {
            fragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
        return result
    }
}