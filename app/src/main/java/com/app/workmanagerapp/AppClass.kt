package com.app.workmanagerapp

import android.app.Application
import androidx.work.WorkManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.app.workmanagerapp.modules.AppModule
import com.app.workmanagerapp.modules.Scopes
import com.app.workmanagerapp.services.PeriodicWork
import com.app.workmanagerapp.storages.PrefStorage
import com.app.workmanagerapp.utils.PeriodicHelper
import timber.log.Timber
import toothpick.Toothpick


class AppClass: Application() {


    override fun onCreate() {
        super.onCreate()
        initLogger()
        initDI()
        checkService()
    }

    private fun checkService() {
        val prefStorage: PrefStorage = Toothpick.openScope(Scopes.APP).getInstance(PrefStorage::class.java)
        if (prefStorage.isWorkStart){
            WorkManager.getInstance().cancelAllWork()
            PeriodicHelper(applicationContext).startLog()
        }
    }

    private fun initLogger() {
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
    }


}