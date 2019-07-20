package ge.dev.baqari.myfit

import android.app.Application

import io.realm.Realm
import io.realm.RealmConfiguration

class ApplicationImpl : Application() {

    override fun onCreate() {
        super.onCreate()
        realmInit()
        instance = this
    }

    private fun realmInit() {
        Realm.init(this)
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