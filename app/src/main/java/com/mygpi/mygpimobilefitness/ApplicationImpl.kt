package com.mygpi.mygpimobilefitness

import android.app.Application
import android.os.Environment
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
        realmInit()
        instance = this
    }

    private fun realmInit() {
        Realm.init(this)

        //val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/cache_shared_lib/";
        //val publicDirectory = File(filePath)
        //if (!publicDirectory.exists())
        //    publicDirectory.mkdir()

        val realmConfig = RealmConfiguration.Builder()
                .name("data_shared_movement")
                .schemaVersion(3)
                .deleteRealmIfMigrationNeeded()
                .build()
        Realm.setDefaultConfiguration(realmConfig)
    }

    override fun onTerminate() {
        super.onTerminate()
        instance = null
    }

    companion object {
        var instance: ApplicationImpl? = null
    }
}