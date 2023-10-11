package com.tpay.sdk.cache

import android.content.Context
import java.io.File
import javax.inject.Singleton

@Singleton
internal class DirectoryManager {
    private lateinit var cacheDirPath: String
    private lateinit var tpayDirectoryPath: String
    lateinit var assetsDirectoryPath: String

    fun init(context: Context){
        cacheDirPath = context.cacheDir.absolutePath

        tpayDirectoryPath =
            cacheDirPath + File.separator + TPAY_CACHE_DIRECTORY_NAME

        assetsDirectoryPath =
            tpayDirectoryPath + File.separator + ASSETS_DIRECTORY_NAME

        File(tpayDirectoryPath).mkdir()
        File(assetsDirectoryPath).mkdir()
    }

    companion object {
        const val TPAY_CACHE_DIRECTORY_NAME = "tpay"
        const val ASSETS_DIRECTORY_NAME = "assets"
    }
}