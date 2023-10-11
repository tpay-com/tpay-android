package com.tpay.sdk.internal

import android.content.Context
import com.tpay.sdk.R
import com.tpay.sdk.extensions.TextToSpan
import com.tpay.sdk.extensions.TypefaceStyle

internal class LegalNotesToSpan {
    companion object {
        private const val TPAY_PL_REGULATIONS_URL = "https://tpay.com/user/assets/files_for_download/regulamin.pdf"
        private const val TPAY_PL_RODO_URL = "https://tpay.com/user/assets/files_for_download/klauzula-informacyjna-platnik.pdf"
        private const val TPAY_EN_REGULATIONS_URL = "https://tpay.com/user/assets/files_for_download/terms-and-conditions-of-payments.pdf"
        private const val TPAY_EN_RODO_URL = "https://tpay.com/user/assets/files_for_download/information-clause-payer-2022.pdf"

        internal fun prepareRegulationTextsToSpan(
            context: Context,
            language: Language
        ): List<TextToSpan> = listOf(
            TextToSpan(context.getString(R.string.regulation_accept), TypefaceStyle.REGULAR),
            TextToSpan(
                context.getString(R.string.regulation),
                TypefaceStyle.MEDIUM,
                if (language == Language.POLISH) TPAY_PL_REGULATIONS_URL else TPAY_EN_REGULATIONS_URL
            )
        )

        internal fun prepareRODOTextsToSpan(
            context: Context,
            language: Language
        ): List<TextToSpan> = listOf(
            TextToSpan(context.getString(R.string.rodo_info), TypefaceStyle.REGULAR),
            TextToSpan(
                context.getString(R.string.rodo_link),
                TypefaceStyle.MEDIUM,
                if (language == Language.POLISH) TPAY_PL_RODO_URL else TPAY_EN_RODO_URL
            )
        )

        internal fun prepareMerchantRODOTextsToSpan(
            context: Context,
            merchantName: String,
            merchantRODOUrl: String,
            merchantCity: String?
        ): List<TextToSpan> = listOf(
            TextToSpan(
                merchantCity?.run {
                    context.getString(R.string.merchant_rodo_info_with_city, merchantName, merchantCity)
                } ?: context.getString(R.string.merchant_rodo_info, merchantName),
                TypefaceStyle.REGULAR
            ),
            TextToSpan(
                context.getString(R.string.rodo_link),
                TypefaceStyle.MEDIUM,
                merchantRODOUrl
            )
        )
    }
}