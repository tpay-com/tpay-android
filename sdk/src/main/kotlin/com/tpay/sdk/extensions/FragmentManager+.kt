package com.tpay.sdk.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

internal inline fun <reified T : Fragment> FragmentManager.getFragmentOrNull(): T? {
    return fragments.firstOrNull { fragment -> fragment is T } as? T
}