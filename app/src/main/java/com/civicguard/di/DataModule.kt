package com.civicguard.di

import android.content.Context
import androidx.room.Room
import com.civicguard.data.local.CivicGuardDatabase
import com.civicguard.data.local.ComplaintDao
import com.civicguard.data.remote.ComplaintApi
import com.civicguard.data.repository.ComplaintRepository
import com.civicguard.util.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CivicGuardDatabase {
        return Room.databaseBuilder(
            context,
            CivicGuardDatabase::class.java,
            "civicguard.db"
        ).build()
    }

    @Provides
    fun provideComplaintDao(db: CivicGuardDatabase): ComplaintDao = db.complaintDao()

    @Provides
    @Singleton
    fun provideComplaintApi(retrofit: Retrofit): ComplaintApi = retrofit.create(ComplaintApi::class.java)

    @Provides
    @Singleton
    fun provideComplaintRepository(
        api: ComplaintApi,
        dao: ComplaintDao
    ): ComplaintRepository = ComplaintRepository(api, dao)
}
