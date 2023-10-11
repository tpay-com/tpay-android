@file:Suppress("unused")

package com.tpay.sdk.internal.nfcScanner.models


internal enum class EMVTag(val tagBytes: ByteArray){
    EMV_PROPRIETARY_TEMPLATE(byteArrayOf(0x70)),
    FILE_CONTROL_INFORMATION_TEMPLATE(byteArrayOf(0x6F)),
    DEDICATED_FILE_NAME(byteArrayOf(0x84.toByte())),
    FILE_CONTROL_INFORMATION_PROPRIETARY_TEMPLATE(byteArrayOf(0xA5.toByte())),
    FILE_CONTROL_INFORMATION_ISSUER_DISCRETIONARY_DATA(byteArrayOf(0xBF.toByte(), 0x0C)),
    APPLICATION_TEMPLATE(byteArrayOf(0x61)),
    APPLICATION_IDENTIFIER(byteArrayOf(0x4F)),
    APPLICATION_LABEL(byteArrayOf(0x50)),
    APPLICATION_PRIMARY_ACCOUNT_NUMBER(byteArrayOf(0x5A)),
    SERVICE_CODE(byteArrayOf(0x5F, 0x30)),
    APPLICATION_EXPIRATION_DATE(byteArrayOf(0x5F, 0x24)),
    ISSUER_COUNTRY_CODE(byteArrayOf(0x5F, 0x28)),
    TRACK_2_EQUIVALENT_DATA(byteArrayOf(0x57)),
    APPLICATION_PRIMARY_ACCOUNT_NUMBER_SEQUENCE_NUMBER(byteArrayOf(0x5F, 0x34)),
    CARD_RISK_MANAGEMENT_DATA_OBJECT_LIST_1(byteArrayOf(0x8C.toByte())),
    CARD_RISK_MANAGEMENT_DATA_OBJECT_LIST_2(byteArrayOf(0x8D.toByte())),
    CARD_HOLDER_VERIFICATION_METHOD_LIST(byteArrayOf(0x8E.toByte())),
    APPLICATION_USAGE_CONTROL(byteArrayOf(0x9F.toByte(), 0x07)),
    APPLICATION_VERSION_NUMBER(byteArrayOf(0x9F.toByte(), 0x08)),
    ISSUER_ACTION_CODE_DEFAULT(byteArrayOf(0x9F.toByte(), 0x0D)),
    ISSUER_ACTION_CODE_DENIAL(byteArrayOf(0x9F.toByte(), 0x0E)),
    ISSUER_ACTION_CODE_ONLINE(byteArrayOf(0x9F.toByte(), 0x0F)),
    APPLICATION_INTERCHANGE_PROFILE(byteArrayOf(0x82.toByte())),
    APPLICATION_CURRENCY_CODE(byteArrayOf(0x9F.toByte(), 0x42)),
    STATIC_DATA_AUTHENTICATION_TAG_LIST(byteArrayOf(0x9F.toByte(),0x4A)),
    APPLICATION_FILE_LOCATOR(byteArrayOf(0x94.toByte())),
    RESPONSE_MESSAGE_TEMPLATE_FORMAT_2(byteArrayOf(0x77)),
    APPLICATION_CURRENCY_EXPONENT(byteArrayOf(0x9F.toByte(), 0x44)),
    CERTIFICATION_AUTHORITY_PUBLIC_KEY_INDEX(byteArrayOf(0x8f.toByte())),
    ISSUER_PUBLIC_KEY_EXPONENT(byteArrayOf(0x9f.toByte(), 0x32)),
    ISSUER_PUBLIC_KEY_REMAINDER(byteArrayOf(0x92.toByte())),
    ISSUER_PUBLIC_KEY_CERTIFICATE(byteArrayOf(0x90.toByte())),
    INTEGRATED_CIRCUIT_CARD_PUBLIC_KEY_EXPONENT(byteArrayOf(0x9F.toByte(), 0x47)),
    DYNAMIC_DATA_AUTHENTICATION_DATA_OBJECT_LIST(byteArrayOf(0x9F.toByte(), 0x49)),
    INTEGRATED_CIRCUIT_CARD_PUBLIC_KEY_CERTIFICATE(byteArrayOf(0x9F.toByte(), 0x46)),
    PROCESSING_OPTIONS_DATA_OBJECT_LIST(byteArrayOf(0x9F.toByte(), 0x38)),
    TERMINAL_TRANSACTION_QUALIFIERS(byteArrayOf(0x9F.toByte(), 0x66)),
    AMOUNT_AUTHORISED(byteArrayOf(0x9F.toByte(), 0x02)),
    AMOUNT_OTHER(byteArrayOf(0x9F.toByte(), 0x03)),
    TERMINAL_COUNTRY_CODE(byteArrayOf(0x9F.toByte(), 0x1A)),
    TRANSACTION_VERIFICATION_RESULTS(byteArrayOf(0x95.toByte())),
    TRANSACTION_DATE(byteArrayOf(0x9A.toByte())),
    TRANSACTION_TYPE(byteArrayOf(0x9C.toByte())),
    TRANSACTION_TIME(byteArrayOf(0x9F.toByte(), 0x21)),
    UNPREDICTABLE_NUMBER(byteArrayOf(0x9F.toByte(), 0x37)),
    TRANSACTION_CURRENCY_CODE(byteArrayOf(0x5F, 0x2A)),
    TERMINAL_CAPABILITIES(byteArrayOf(0x9F.toByte(), 0x33)),
    ADDITIONAL_TERMINAL_CAPABILITIES(byteArrayOf(0x9F.toByte(), 0x40)),
    TERMINAL_TYPE(byteArrayOf(0x9F.toByte(), 0x35));

    companion object {
        val pdolTags = listOf(
            TERMINAL_TRANSACTION_QUALIFIERS,
            AMOUNT_AUTHORISED,
            AMOUNT_OTHER,
            TERMINAL_COUNTRY_CODE,
            TRANSACTION_VERIFICATION_RESULTS,
            TRANSACTION_DATE,
            TRANSACTION_TYPE,
            TRANSACTION_TIME,
            UNPREDICTABLE_NUMBER,
            TRANSACTION_CURRENCY_CODE,
            TERMINAL_CAPABILITIES,
            ADDITIONAL_TERMINAL_CAPABILITIES,
            TERMINAL_TYPE
        ).distinct()
    }
}