package com.bhanu.ironlog.di

import com.bhanu.ironlog.data.repository.AccountRepository
import com.bhanu.ironlog.data.repository.impl.AccountRepositoryImpl
import com.bhanu.ironlog.data.service.CloudStorageService
import com.bhanu.ironlog.data.service.impl.GoogleDriveServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CloudModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        accountRepositoryImpl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton
    abstract fun bindCloudStorageService(
        googleDriveServiceImpl: GoogleDriveServiceImpl
    ): CloudStorageService
}
