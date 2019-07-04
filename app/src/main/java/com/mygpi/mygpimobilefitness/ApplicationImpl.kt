package com.mygpi.mygpimobilefitness

import android.app.Application
import android.util.Log

import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File

class ApplicationImpl : Application() {

    var serviceRun: Boolean = false
    var isShowToast = false

    override fun onCreate() {
        super.onCreate()
        serviceRun = false
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .name("step_db")
                .schemaVersion(3)
                .deleteRealmIfMigrationNeeded()
                .build()
        Realm.setDefaultConfiguration(realmConfig)
        instance = this
    }

    override fun onTerminate() {
        super.onTerminate()
        instance = null
    }

    companion object {
        var instance: ApplicationImpl? = null
    }
}