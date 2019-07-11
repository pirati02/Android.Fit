package ge.dev.baqari.api.step

import ge.dev.baqari.model.StepTransaction
import ge.dev.baqari.model.SuccessTransaction
import ge.dev.baqari.today
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmAsyncTask
import java.util.*
import java.util.concurrent.TimeUnit


object SessionManager {
    private var startTime: Date? = null
    private var realmAsyncTask: RealmAsyncTask? = null
    private var counter: Long = 0
    private var interval: Disposable? = null

    fun startSession(num: Long, restarted: Boolean = false) {
        if (restarted) {
            counter = 0
            save(startTime, Date().today(), num, false)
            startTime = null
            if (interval != null && interval?.isDisposed == false) {
                interval?.dispose()
                interval = null
            }
        }

        if (startTime == null) {
            startTime = Date().today()
            interval = Observable.interval(1000, TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .subscribe {
                        counter += 1
                    }
        }
    }

    fun sessionExpired(): Boolean {
        return if (startTime == null) true
        else return counter > 20
    }

    fun update(num: Long) {
        counter = 0
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