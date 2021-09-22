package com.app.workmanagerapp

import android.app.Application


class AppClass: Application() {


    override fun onCreate() {
        super.onCreate()
       /* initLogger()
        initDI()*/

    }



    /*private fun initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initDI() {
        Toothpick.openScope(Scopes.APP).installModules(
            AppModule(
                applicationContext
            )
        )
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.BODY)
    }*/


}