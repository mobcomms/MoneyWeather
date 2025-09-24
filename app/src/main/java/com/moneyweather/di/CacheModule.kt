package com.moneyweather.di

import com.moneyweather.util.token.TokenProvider
import com.moneyweather.util.token.TokenProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CacheModule {
    @Binds
    abstract fun bindTokenProvider(tokenProviderImpl: TokenProviderImpl): TokenProvider
}