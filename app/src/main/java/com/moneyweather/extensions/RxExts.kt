package com.moneyweather.extensions

import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

fun <T> Single<T>.retryWithBackoff(
    maxRetries: Int = 3,
    delayMillis: Long = 300,
    shouldRetry: (Throwable) -> Boolean = { it is IOException || (it is HttpException && it.code() in 500..599) }
): Single<T> {
    return this
        .toFlowable()
        .retryWhen { errors ->
            errors.zipWith(Flowable.range(1, maxRetries)) { error, retryCount ->
                if (shouldRetry(error) && retryCount <= maxRetries) {
                    retryCount
                } else {
                    throw error
                }
            }.flatMap { retryCount ->
                // delayMillis * 1 > delayMillis * 2 > delayMillis * 3
                val delay = delayMillis * retryCount
                Flowable.timer(delay, TimeUnit.MILLISECONDS)
            }
        }
        .singleOrError()
}

fun <T> Single<T>.retryWithExponentialBackoff(
    maxRetries: Int = 3,
    delayMillis: Long = 300,
    shouldRetry: (Throwable) -> Boolean = { it is IOException || (it is HttpException && it.code() in 500..599) }
): Single<T> {
    return this
        .toFlowable()
        .retryWhen { errors ->
            errors.zipWith(Flowable.range(1, maxRetries)) { error, retryCount ->
                if (shouldRetry(error) && retryCount <= maxRetries) {
                    retryCount
                } else {
                    throw error
                }
            }.flatMap { retryCount ->
                // delayMillis * 1 > delayMillis * 2 > delayMillis * 4
                val delay = delayMillis * (1L shl (retryCount - 1))
                Flowable.timer(delay, TimeUnit.MILLISECONDS)
            }
        }
        .singleOrError()
}