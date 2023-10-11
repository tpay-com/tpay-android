package com.tpay.sdk.internal.addCard

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.tpay.sdk.R
import com.tpay.sdk.databinding.FragmentAddCardBinding
import com.tpay.sdk.extensions.isVisible
import com.tpay.sdk.extensions.onClick
import com.tpay.sdk.internal.paymentMethod.Composition


internal class EnableNFCComposition(
    private val binding: FragmentAddCardBinding,
    private val context: Context
) : Composition(context) {
    override fun onCreate() {
        isLayoutVisible = true
        binding.run {
            isBottomLayoutVisible = false

            enableNfc.run {
                enableNfcInSettingsTextView.text =
                    context.getString(R.string.enable_nfc_in_settings)
                enableNfcTextView.text = context.getString(R.string.enable_nfc)
                enableNfcSettingsButton.text = context.getString(R.string.settings)
                enableNfcBackButton.text = context.getString(R.string.go_back)
            }
        }

        setOnClicks()
    }

    override fun onDestroy() {
        isLayoutVisible = false
    }

    private fun setOnClicks() {
        binding.enableNfc.run {
            enableNfcSettingsButton.onClick {
                context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }
    }

    private var isLayoutVisible: Boolean
        get() = binding.enableNfc.root.isVisible
        set(value) {
            binding.enableNfc.root.isVisible = value
        }
}