package com.github.nosepass.motoparking.di

import android.app.Application
import android.content.Context
import com.github.nosepass.motoparking.data.MotoApi
import com.github.nosepass.motoparking.data.SettingsRepo
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.DateTypeAdapter
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

/**
 * Create stuff for the singleton dagger graph
 */
@Module//(subcomponents = arrayOf(EnvComponent::class))
internal class AppModule {

    @Singleton
    @Provides
    fun provideContext(app: Application): Context {
        return app
    }

    @Singleton
    @Provides
    fun provideOkhttp(cookieManager: CookieManager): OkHttpClient {
        CookieManager.setDefault(cookieManager)
        return OkHttpClient()
    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date::class.java, DateTypeAdapter())
                .create()
    }

    @Singleton
    @Provides
    fun provideCookieManager(): CookieManager {
        return CookieManager(null, CookiePolicy.ACCEPT_ALL)
    }

    @Singleton
    @Provides
    fun provideSettings(context: Context): SettingsRepo {
        return SettingsRepo(context)
    }
}
