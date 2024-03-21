package com.tpay.sdk.extensions

import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.tpay.sdk.R
import com.tpay.sdk.internal.ScreenMetrics
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class FragmentViewBindingDelegate<T : ViewBinding>(
    val fragment: Fragment,
    val viewBindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            binding = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val binding = binding
        if (binding != null) {
            return binding
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        return viewBindingFactory(thisRef.requireView()).also { this.binding = it }
    }
}

internal fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(
        this,
        viewBindingFactory
    )

// Resources
internal fun Fragment.getColor(@ColorRes resId: Int): Int = ContextCompat.getColor(requireContext(), resId)

// Keyboard
internal fun Fragment.hideKeyboard() = requireActivity().hideKeyboard()

internal fun Fragment.getScreenMetrics(): ScreenMetrics {
    return Rect().run {
        requireActivity().window.decorView.getWindowVisibleDisplayFrame(this)
        ScreenMetrics(
            statusBarHeight = top,
            screenHeightWithoutBottomBar = bottom
        )
    }
}

internal fun FragmentTransaction.animate(withAnim: Boolean): FragmentTransaction {
    if (withAnim) {
        this.setCustomAnimations(
            R.anim.alpha_in,
            R.anim.alpha_out,
            R.anim.alpha_in,
            R.anim.alpha_out
        )
    }
    return this
}

internal fun FragmentTransaction.backStack(addToBackStack: Boolean): FragmentTransaction {
    if (addToBackStack) {
        this.addToBackStack(null)
    }
    return this
}

internal inline fun <reified T : Fragment> List<Fragment>.containsInstanceOf(): Boolean {
    return firstOrNull { fragment -> fragment is T } != null
}