package com.tpay.sdk.di

import com.tpay.sdk.api.tpayModule.TpayModule
import com.tpay.sdk.internal.Navigation
import com.tpay.sdk.server.ServerService
import com.tpay.sdk.internal.Repository
import com.tpay.sdk.internal.config.Configuration
import com.tpay.sdk.internal.webViewModule.WebViewCoordinator
import javax.inject.Singleton

internal class DIModule {
    @Provides
    @Singleton
    fun repository(): Repository {
        return Repository(serverService())
    }

    @Provides
    @Singleton
    fun configuration(): Configuration {
        return TpayModule.configuration
    }

    @Provides
    @Singleton
    fun navigation(): Navigation {
        return Navigation()
    }

    @Provides
    @Singleton
    fun webViewCoordinator(): WebViewCoordinator {
        return WebViewCoordinator()
    }

    @Provides
    @Singleton
    fun serverService(): ServerService {
        return ServerService()
    }
}