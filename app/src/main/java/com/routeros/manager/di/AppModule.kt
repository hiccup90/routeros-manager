package com.routeros.manager.di

import android.content.Context
import com.routeros.manager.data.api.NetworkClient
import com.routeros.manager.data.preferences.SecurePreferences
import com.routeros.manager.data.repository.RouterOSRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSecurePreferences(
        @ApplicationContext context: Context
    ): SecurePreferences {
        return SecurePreferences(context)
    }

    @Provides
    @Singleton
    fun provideNetworkClient(
        securePreferences: SecurePreferences
    ): NetworkClient {
        return NetworkClient(securePreferences)
    }

    @Provides
    @Singleton
    fun provideRouterOSRepository(
        networkClient: NetworkClient,
        securePreferences: SecurePreferences
    ): RouterOSRepository {
        return RouterOSRepository(networkClient, securePreferences)
    }
}
