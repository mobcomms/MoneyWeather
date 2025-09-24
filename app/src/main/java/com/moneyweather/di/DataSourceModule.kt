package com.moneyweather.di

import com.moneyweather.data.remote.impl.ApiAnicImpl
import com.moneyweather.data.remote.impl.ApiMobonImpl
import com.moneyweather.data.remote.impl.ApiMobwithImpl
import com.moneyweather.data.remote.impl.ApiPoMissionImpl
import com.moneyweather.data.remote.impl.ApiUserImpl
import com.moneyweather.data.remote.model.ApiAnicModel
import com.moneyweather.data.remote.model.ApiMobonModel
import com.moneyweather.data.remote.model.ApiMobwithModel
import com.moneyweather.data.remote.model.ApiPoMissionModel
import com.moneyweather.data.remote.model.ApiUserModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindApiUserModel(apiUserImpl: ApiUserImpl): ApiUserModel

    @Binds
    abstract fun bindApiPoMissionModel(apipoMissionImpl: ApiPoMissionImpl): ApiPoMissionModel

    @Binds
    abstract fun bindApiAnicModel(apiAnicImpl: ApiAnicImpl): ApiAnicModel

    @Binds
    abstract fun bindApiMobonModel(apiMobonImpl: ApiMobonImpl): ApiMobonModel

    @Binds
    abstract fun bindApiMobwithModel(apiMobwithImpl: ApiMobwithImpl): ApiMobwithModel
}