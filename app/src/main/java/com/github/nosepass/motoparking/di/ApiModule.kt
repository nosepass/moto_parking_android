package com.github.nosepass.motoparking.di

import com.github.nosepass.motoparking.data.MotoApi
import com.github.nosepass.motoparking.data.SettingsRepo
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

/**
 * Create stuff for the singleton dagger graph
 */
@EnvScope
@Module
internal open class ApiModule {

    @EnvScope
    @Provides
    @Named("baseUrl")
    fun provideApiUrl(settings: SettingsRepo): String {
        return settings.baseUrl
    }

    @EnvScope
    @Provides
    fun provideApi(@Named("baseUrl") baseUrl: String, gson: Gson, okhttp: OkHttpClient): MotoApi {
        val retrofit = Retrofit.Builder()
                .client(okhttp)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        return retrofit.build().create(MotoApi::class.java)
    }
}
