package com.tpay.sdk.api.models

interface Presentable {
    /**
     * Function responsible for opening Tpay UI module
     * @return [SheetOpenResult]
     */
    fun present(): SheetOpenResult

    /**
     * Function responsible for handling back press inside Tpay UI module
     */
    fun onBackPressed()
}
