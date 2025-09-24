package com.moneyweather.util.exoplayer

import android.content.Context
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import timber.log.Timber
import java.io.File

object ExoPlayerDataSourceFactory {
    private const val MAX_CACHE_SIZE = 50L * 1024 * 1024 // 50MB
    private const val CACHE_FILE_NAME = "media_cache"

    @Volatile
    private var simpleCache: SimpleCache? = null

    private fun getCache(context: Context): SimpleCache {
        return simpleCache ?: synchronized(this) {
            simpleCache ?: SimpleCache(
                File(context.cacheDir, CACHE_FILE_NAME),
                LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE),
                StandaloneDatabaseProvider(context)
            ).also { simpleCache = it }
        }
    }

    fun getDataSourceFactory(context: Context): CacheDataSource.Factory {
        val cache = getCache(context)

        val httpDataSourceFactory = DefaultHttpDataSource.Factory().apply {
            setUserAgent(context.packageName)
        }
        val upstreamDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val cacheFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setCacheWriteDataSinkFactory(CacheDataSink.Factory().setCache(cache))
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setUpstreamDataSourceFactory(upstreamDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            .setEventListener(object : CacheDataSource.EventListener {
                override fun onCachedBytesRead(cacheSizeBytes: Long, cachedBytesRead: Long) {
                    Timber.tag("exoPlayer").d("onCachedBytesRead. cacheSizeBytes:$cacheSizeBytes, cachedBytesRead:$cachedBytesRead")
                }

                override fun onCacheIgnored(reason: Int) {
                    Timber.tag("exoPlayer").d("onCacheIgnored. reason:$reason")
                }
            })

        return cacheFactory
    }

    // TODO : 영상 버전 관련 API 구현 후 처리 예정
    fun clearCache(context: Context) {
        getCache(context).release()
        File(context.cacheDir, CACHE_FILE_NAME).deleteRecursively()
    }
}