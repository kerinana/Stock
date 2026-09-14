package com.example.securities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 1. Hilt 看到這條：知道如何建立 LoggingInterceptor
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 印出完整 Request/Response 標頭與 Body
        }
    }

    // 2. Hilt 發現建立 OkHttpClient 需要 LoggingInterceptor
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // 👈 將 Interceptor 加進 OkHttpClient
            .build()
    }

    // 3. Hilt 發現建立 Retrofit 需要 OkHttpClient
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://openapi.twse.com.tw/")
            .client(okHttpClient) // 👈 將設定好的 OkHttpClient 帶入 Retrofit
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 4. 提供 TwseApiService 實作
    @Provides
    @Singleton
    fun provideTwseApiService(retrofit: Retrofit): TwseApiService {
        return retrofit.create(TwseApiService::class.java)
    }
}