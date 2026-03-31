package com.civicguard.di

import com.civicguard.data.remote.AuthApi
import com.civicguard.data.repository.AuthRepository
import com.civicguard.util.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi, sessionManager: SessionManager): AuthRepository {
        return AuthRepository(api, sessionManager)
    }
}
