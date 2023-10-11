package com.tpay.sdk.internal.model

internal enum class PaymentMethod(val groupId: String) {
    CREDIT_CARD("103"),
    ALIOR("113"),
    PEKAO("102"),
    PKO("108"),
    INTELIGO("110"),
    BLIK("150"),
    MBANK("160"),
    ING("111"),
    MILLENIUM("114"),
    SANTANDER("115"),
    CITIBANK("132"),
    AGRICOLE("116"),
    GETIN("119"),
    POCZTOWY("124"),
    SPOLDZIELCZE("135"),
    PARIBAS("133"),
    NEO("159"),
    NEST("130"),
    PLUS("145"),
    GOOGLE_PAY("166"),
    PAYPAL("106");

    companion object {
        fun transfers(): List<PaymentMethod> {
            values()
                .toMutableList()
                .apply {
                    removeAll { paymentMethod ->
                        paymentMethod in listOf(CREDIT_CARD, PAYPAL, GOOGLE_PAY, BLIK)
                    }
                    return this
                }
        }
    }
}