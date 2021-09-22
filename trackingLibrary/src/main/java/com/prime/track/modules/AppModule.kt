package com.prime.track.modules

import android.content.Context
import com.prime.track.MainClass
import com.prime.track.storages.PrefStorage
import toothpick.config.Module

class AppModule(context: Context): Module() {

    init {
        bind(Context::class.java).toInstance(context)
        bind(PrefStorage::class.java).toInstance(PrefStorage(context))
        bind(MainClass::class.java).singleton()
    }
}