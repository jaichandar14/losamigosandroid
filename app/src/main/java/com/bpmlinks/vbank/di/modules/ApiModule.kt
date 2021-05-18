package com.bpmlinks.vbank.di.modules

import com.bpmlinks.vbank.BuildConfig
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.webservices.ApiStories
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/* @Module informs Dagger that this class is a Dagger Module
*  @Provides tell Dagger how to create instances of the type that this function returns dependency.
*  @Singleton makes your class a single instance in your dependencies graph
*  Function parameters are the dependencies of this type.*/
@Module
class ApiModule {

    @Singleton
    @Provides
    fun getGson() : Gson {
        return GsonBuilder().create()
    }
    @Singleton
    @Provides
    fun provideOKHttpClient(httpLoggingInterceptor : HttpLoggingInterceptor): OkHttpClient {

        val okHttpClient = OkHttpClient.Builder()

        okHttpClient
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        okHttpClient.addInterceptor(httpLoggingInterceptor)

        return okHttpClient.build()
    }

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    @Singleton
    @Provides
    fun getRetrofit(gson: Gson, okHttpClient: OkHttpClient) : Retrofit {
        return Retrofit.Builder().
            addConverterFactory(GsonConverterFactory.create(gson)).
            baseUrl(BuildConfig.base_url).client(okHttpClient).build()
    }

    @Singleton
    @Provides
    fun provideApiStories(retrofit: Retrofit) : ApiStories {
        return retrofit.create(ApiStories::class.java)
    }

    @Singleton
    @Provides
    fun dispatcherProvider() : DefaultDispatcherProvider {
        return DefaultDispatcherProvider()
    }
}