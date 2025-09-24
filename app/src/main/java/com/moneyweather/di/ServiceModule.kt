package com.moneyweather.di

import com.moneyweather.data.remote.service.ApiAnicService
import com.moneyweather.data.remote.service.ApiMobonService
import com.moneyweather.data.remote.service.ApiMobwithService
import com.moneyweather.data.remote.service.ApiPoMissionService
import com.moneyweather.data.remote.service.ApiUserService
import com.moneyweather.data.remote.service.AuthApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {
    @Provides
    @Singleton
    fun provideApiUserService(
        @Named("DefaultUrlRetrofit") retrofit: Retrofit
    ): ApiUserService = retrofit.create(
        ApiUserService::class.java
    )

    @Provides
    @Singleton
    fun provideAuthApiService(
        @Named("DefaultUrlRetrofitWithoutInterceptor") retrofit: Retrofit
    ): AuthApiService = retrofit.create(
        AuthApiService::class.java
    )

    @Provides
    @Singleton
    fun provideApiPoMissionService(
        @Named("PoMissionRetrofit") retrofit: Retrofit
    ): ApiPoMissionService = retrofit.create(
        ApiPoMissionService::class.java
    )

    @Provides
    @Singleton
    fun provideApiAnicService(
        @Named("AnicRetrofit") retrofit: Retrofit
    ): ApiAnicService = retrofit.create(
        ApiAnicService::class.java
    )

    @Provides
    @Singleton
    fun provideApiMobonService(
        @Named("MobonRetrofit") retrofit: Retrofit
    ): ApiMobonService = retrofit.create(
        ApiMobonService::class.java
    )

    @Provides
    @Singleton
    fun provideApiMobwithService(
        @Named("MobwithRetrofit") retrofit: Retrofit
    ): ApiMobwithService = retrofit.create(
        ApiMobwithService::class.java
    )
}