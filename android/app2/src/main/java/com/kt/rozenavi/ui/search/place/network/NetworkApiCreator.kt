/*
 *  Copyright (c) 2017 kt corp. All rights reserved.
 *
 *  This is a proprietary software of kt corp, and you may not use this file
 *  except in compliance with license agreement with kt corp. Any redistribution
 *  or use of this software, with or without modification shall be strictly
 *  prohibited without prior written approval of kt corp, and the copyright
 *   notice above does not evidence any actual or intended publication of such
 *  software.
 *
 */
package com.kt.rozenavi.ui.search.place.network

import android.util.Log
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkApiCreator {
    private val TAG = NetworkApiCreator::class.java.simpleName
    private const val CONNECT_TIMEOUT = 5 * 1000
    private const val WRITE_TIMEOUT = 10 * 1000
    private const val READ_TIMEOUT = 10 * 1000
    private const val KEY_PARAMETER = "Authorization"

    fun createRetrofitWithCoroutine(baseUrl: String, key: String): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(createHttpClient(key))
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create()))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
    }

    private fun createHttpClient(key: String): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
        builder.addInterceptor(getApiKeyInterceptor(key))
        val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message -> Log.d(TAG, message) })
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(httpLoggingInterceptor) //http 로그 확인
        return builder.connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS) //연결 타임아웃 시간 설정
                .writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS) //쓰기 타임아웃 시간 설정
                .readTimeout(READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS) //읽기 타임아웃 시간 설정
                .build()
    }

    /**
     * api key를 추가하는 interceptor 객체 반환
     *
     * @return interceptor 객체 반환
     */
    private fun getApiKeyInterceptor(key: String): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                    .addHeader(KEY_PARAMETER,
                            "Bearer $key")
                    .build()
            chain.proceed(request)
        }
    }
}