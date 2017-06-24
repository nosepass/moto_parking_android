package com.github.nosepass.motoparking.di

import android.app.Application
import com.github.nosepass.motoparking.MyApplication

import javax.inject.Singleton

import dagger.BindsInstance
import dagger.Component

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): AppComponent
    }

    fun envComponentBuilder(): EnvComponent.Builder

    fun inject(app: MyApplication)
}