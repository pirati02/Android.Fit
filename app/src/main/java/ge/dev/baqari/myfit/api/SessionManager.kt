package ge.dev.baqari.myfit.api

import ge.dev.baqari.myfit.model.StepTransaction
import ge.dev.baqari.myfit.model.SuccessTransaction
import ge.dev.baqari.myfit.utils.today
import io.realm.Realm
import io.realm.RealmAsyncTask
import java.util.*


object SessionManager {
    private var startTime: Date? = null
    private var realmAsyncTask: RealmAsyncTask? = null
    private var counterInMillis: Long = 0

    fun startSession(num: Long, restarted: Boolean = false) {
        if (restarted) {
            counterInMillis = 0
            save(startTime, Date().today(), num, false)
            startTime = null
        }

        if (startTime == null) {
            startTime = Date().today()
            counterInMillis = System.currentTimeMillis()
        }
    }

    fun sessionExpired(): Boolean {
        return if (startTime == null) true
        else return (System.currentTimeMillis() - counterInMillis) > 10000
    }

    fun update(num: Long) {
        counterInMillis = 0
        save(startTime!!, Date().today(), num)
    }

    private fun save(startDate: Date?, endDate: Date?, num: Long, update: Boolean = true) {
        val realm = Realm.getDefaultInstance()
        realmAsyncTask = realm.executeTransactionAsync(
                StepTransaction(startDate, endDate, num, update),
                SuccessTransaction(realmAsyncTask),
                Realm.Transaction.OnError { error -> error.printStackTrace() }
        )
        realm.close()
    }
}