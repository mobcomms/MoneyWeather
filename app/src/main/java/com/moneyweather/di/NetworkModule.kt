package com.moneyweather.di

import android.content.Context
import com.moneyweather.data.remote.UrlHelper
import com.moneyweather.data.remote.jsonconverter.JSONConverterFactory
import com.moneyweather.util.interceptor.HeaderInterceptor
import com.moneyweather.util.interceptor.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    @Named("PoMissionUrl")
    fun providePoMissionUrl(): String = UrlHelper.POMISSION_DOMAIN

    @Provides
    @Singleton
    @Named("AnicUrl")
    fun provideAnicUrl(): String = UrlHelper.ANIC_DOMAIN

    @Provides
    @Singleton
    @Named("MobonUrl")
    fun provideMobonUrl(): String = UrlHelper.MOBON_DOMAIN

    @Provides
    @Singleton
    @Named("MobwithUrl")
    fun provideMobwithUrl(): String = UrlHelper.MOBWITH_DOMAIN

    @Provides
    fun provideCache(@ApplicationContext context: Context): Cache {
        return Cache(File(context.cacheDir, CACHE_FILE_NAME), CACHE_FILE_SIZE)
    }

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    @Named("DefaultOkHttpClient")
    fun provideDefaultOkHttpClient(
        cache: Cache,
        headerInterceptor: HeaderInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .applySystemDefaultTrustManager()
            .connectTimeout(CONNECT_TIMEOUT_SECS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECS, TimeUnit.SECONDS)
            .cache(cache)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(headerInterceptor)
            .build()

    @Provides
    @Singleton
    @Named("DynamicUrlOkHttpClient")
    fun provideDynamicUrlOkHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .applySystemDefaultTrustManager()
            .connectTimeout(CONNECT_TIMEOUT_SECS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECS, TimeUnit.SECONDS)
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    @Named("InterceptorOkHttpClient")
    fun provideInterceptorOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    @Named("DefaultUrlRetrofit")
    fun provideDefaultRetrofit(
        @Named("DefaultOkHttpClient") okHttpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(UrlHelper.DOMAIN)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(JSONConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("DefaultUrlRetrofitWithoutInterceptor")
    fun provideRetrofitWithoutInterceptor(
        @Named("InterceptorOkHttpClient") okHttpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(UrlHelper.DOMAIN)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("DynamicUrlRetrofitBuilder")
    fun provideDynamicUrlRetrofitBuilder(
        @Named("DynamicUrlOkHttpClient") okHttpClient: OkHttpClient,
    ): Retrofit.Builder = Retrofit.Builder()
        .client(okHttpClient)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(JSONConverterFactory.create())

    @Provides
    @Singleton
    @Named("PoMissionRetrofit")
    fun providePoMissionRetrofit(
        @Named("DynamicUrlRetrofitBuilder") builder: Retrofit.Builder,
        @Named("PoMissionUrl") url: String
    ): Retrofit = builder
        .baseUrl(url)
        .build()

    @Provides
    @Singleton
    @Named("AnicRetrofit")
    fun provideAnicRetrofit(
        @Named("DynamicUrlRetrofitBuilder") builder: Retrofit.Builder,
        @Named("AnicUrl") url: String
    ): Retrofit = builder
        .baseUrl(url)
        .build()

    @Provides
    @Singleton
    @Named("MobonRetrofit")
    fun provideMobonRetrofit(
        @Named("DynamicUrlRetrofitBuilder") builder: Retrofit.Builder,
        @Named("MobonUrl") url: String
    ): Retrofit = builder
        .baseUrl(url)
        .build()

    @Provides
    @Singleton
    @Named("MobwithRetrofit")
    fun provideMobwithRetrofit(
        @Named("DynamicUrlRetrofitBuilder") builder: Retrofit.Builder,
        @Named("MobwithUrl") url: String
    ): Retrofit = builder
        .baseUrl(url)
        .build()

    companion object {
        // timeout
        const val CONNECT_TIMEOUT_SECS = 10 * 60L
        const val WRITE_TIMEOUT_SECS = 10 * 60L
        const val READ_TIMEOUT_SECS = 30 * 60L

        // cache
        const val CACHE_FILE_NAME = "donseeNetworkCache"
        const val CACHE_FILE_SIZE = 10 * 1024 * 1024L
    }
}

fun OkHttpClient.Builder.applySystemDefaultTrustManager(): OkHttpClient.Builder = apply {
    try {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)

        val trustManagers = trustManagerFactory.trustManagers
        check(trustManagers.size == 1 && trustManagers[0] is X509TrustManager) {
            "Unexpected default trust managers: ${trustManagers.contentToString()}"
        }

        val trustManager = trustManagers[0] as X509TrustManager

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(trustManager), null)

        sslSocketFactory(sslContext.socketFactory, trustManager)

    } catch (e: Exception) {
        e.printStackTrace()
    }
}