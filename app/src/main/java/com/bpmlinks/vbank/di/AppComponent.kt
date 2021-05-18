package com.bpmlinks.vbank.di

import android.app.Application
import com.bpmlinks.vbank.VBankApp
import com.bpmlinks.vbank.di.modules.ApiModule
import com.bpmlinks.vbank.di.modules.AppModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

/* The "modules" attribute in the @Component annotation tells Dagger what Modules
 to include when building the graph*/
@Singleton
@Component(
    modules = [AndroidInjectionModule::class,
        ActivityBuilder::class,
        ApiModule::class,
        AppModule::class]
)
interface AppComponent {

    /* We will call this builder interface from our custom Application class.
 * This will set our application object to the AppComponent.
 * So inside the AppComponent the application instance is available.
 * So this application instance can be accessed by our modules
 * such as ApiModule when needed
 * */
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent

    }

    /*  This is our custom Application class*/
    fun inject(application: VBankApp)
}