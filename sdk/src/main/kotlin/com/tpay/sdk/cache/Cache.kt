package com.tpay.sdk.cache

import com.tpay.sdk.di.injectFields
import com.tpay.sdk.extensions.Completable
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class Cache {
    private val executor = Executors.newCachedThreadPool()

    @Inject
    lateinit var directoryManager: DirectoryManager

    init {
        injectFields()
    }

    fun saveBankLogo(cachedNetworkImage: CachedNetworkImage): Completable<String> {
        return saveImage(cachedNetworkImage, directoryManager.assetsDirectoryPath)
    }

    fun getBankLogo(fromUrl: String): Completable<CachedNetworkImage> {
        return getImage(CachedNetworkImage.hashUrl(fromUrl) ?: "", directoryManager.assetsDirectoryPath)
    }

    private fun getImage(fileName: String, dirPath: String): Completable<CachedNetworkImage> {
        return Completable.create { completable ->
            executor.execute {
                val imageFile = File(dirPath).listFiles()?.firstOrNull { it.name == fileName }
                if(imageFile != null){
                    completable.onSuccess(CachedNetworkImage(fileName, imageFile.readBytes()))
                } else {
                    completable.onError(ImageNotFoundException())
                }
            }
        }
    }

    private fun saveImage(cachedNetworkImage: CachedNetworkImage, dirPath: String): Completable<String> {
        return Completable.create { completable ->
            executor.execute {
                val imagePath = dirPath + File.separator + cachedNetworkImage.urlHash
                try {
                    File(imagePath).run {
                        createNewFile()
                        writeBytes(cachedNetworkImage.bytes)
                    }
                    completable.onSuccess(imagePath)
                } catch (exception: Exception){
                    completable.onError(exception)
                }
            }
        }
    }
}