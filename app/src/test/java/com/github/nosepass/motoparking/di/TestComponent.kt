package com.github.nosepass.motoparking.di

import com.github.nosepass.motoparking.data.TestMotoApi
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named

@EnvScope
@Component(modules = arrayOf(TestApiModule::class))
interface TestComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun baseUrl(@Named("baseUrl") baseUrl: String): Builder
        fun build(): TestComponent
    }

    fun inject(test: TestMotoApi)
}
