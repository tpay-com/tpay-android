package com.tpay.sdk.internal.nfcScanner

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import com.tpay.sdk.internal.nfcScanner.commands.CardType
import com.tpay.sdk.internal.nfcScanner.extensions.getRecordData
import com.tpay.sdk.internal.nfcScanner.util.AFLUtil
import com.tpay.sdk.internal.nfcScanner.util.GetProcessingOptionsUtil
import com.tpay.sdk.internal.nfcScanner.util.PSEUtil
import com.tpay.sdk.internal.nfcScanner.util.SelectApplicationUtil


internal class PayCardScanner {
    private var nfcAdapter: NfcAdapter? = null

    internal fun startScan(
        activity: Activity,
        onScan: (PayCardScanResult?) -> Unit
    ){
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        nfcAdapter?.enableReaderMode(
            activity,
            { tag -> onScan(scan(tag)) },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B,
            Bundle().apply { putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250) }
        )
    }

    internal fun stopScan(activity: Activity){
        if (!activity.isDestroyed) {
            nfcAdapter?.disableReaderMode(activity)
        }
        nfcAdapter = null
    }

    private fun scan(tag: Tag): PayCardScanResult? {
        val isoDep = IsoDep.get(tag)
        isoDep.connect()

        return try {
            // Get application id from pay card
            val pseResponseBytes = isoDep.transceive(PSEUtil.getPSECommand(CardType.CONTACTLESS))
            val pseResponse = PSEUtil.getPSEResponse(pseResponseBytes)

            // Select application on pay card
            val selectApplicationResponseBytes =
                isoDep.transceive(SelectApplicationUtil.getSelectApplicationCommand(pseResponse.applicationId))
            val selectApplicationResponse = SelectApplicationUtil.getSelectApplicationResponse(selectApplicationResponseBytes)

            if(selectApplicationResponse == null){
                // Application response not containing PDOL
                val getProcessingOptionsResponseBytes =
                    isoDep.transceive(GetProcessingOptionsUtil.getProcessingOptionsCommand())
                val getProcessingOptionsResponse =
                    GetProcessingOptionsUtil.getProcessingOptionsResponse(getProcessingOptionsResponseBytes)

                // Commands to read actual pay card data
                val recordCommands = AFLUtil.getRecordCommands(getProcessingOptionsResponse.aflRecords)
                val recordData = isoDep.getRecordData(recordCommands)

                PayCardScanResult(recordData)
            } else {
                // Application response containing PDOL
                val getProcessingOptionsResponseBytes =
                    isoDep.transceive(GetProcessingOptionsUtil.getProcessingOptionsWithPDOLCommand(selectApplicationResponse.pdolTags))
                val getProcessingOptionsResponse =
                    GetProcessingOptionsUtil.getProcessingOptionsResponse(getProcessingOptionsResponseBytes)

                // Commands to read actual pay card data
                val recordCommands = AFLUtil.getRecordCommands(getProcessingOptionsResponse.aflRecords)
                val recordData = isoDep.getRecordData(recordCommands)

                PayCardScanResult(recordData)
            }
        } catch (exception: Exception){
            null
        } finally {
            isoDep.close()
        }
    }
}