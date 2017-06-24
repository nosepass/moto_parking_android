package com.github.nosepass.motoparking.di

import com.github.nosepass.motoparking.MyApplication
import dagger.Subcomponent

@EnvScope
@Subcomponent(modules = arrayOf(ApiModule::class))
interface EnvComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): EnvComponent
    }

    fun inject(app: MyApplication)
}