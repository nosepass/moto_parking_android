package com.github.nosepass.motoparking.di

import com.github.nosepass.motoparking.data.MotoApi
import com.github.nosepass.motoparking.data.SettingsRepo
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Named

/**
 * Standalone ApiModule (bypasses SettingsRepo)
 */
@EnvScope
@Module
internal class TestApiModule {

    @EnvScope
    @Provides
    fun provideApi(@Named("baseUrl") baseUrl: String): MotoApi {
        val appModule = AppModule()
        return ApiModule().provideApi(baseUrl, appModule.provideGson(), OkHttpClient())
    }
}
