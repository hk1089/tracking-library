package com.app.workmanagerapp.modules

import android.content.Context
import com.app.workmanagerapp.storages.PrefStorage
import toothpick.config.Module

class AppModule(context: Context): Module() {

    init {
        bind(Context::class.java).toInstance(context)
        bind(PrefStorage::class.java).singletonInScope()
    }
}