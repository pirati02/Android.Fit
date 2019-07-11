package ge.dev.baqari.fit.model

import io.realm.Realm
import io.realm.RealmAsyncTask

class SuccessTransaction(internal var asyncTransaction: RealmAsyncTask?) : Realm.Transaction.OnSuccess {

    override fun onSuccess() {
        if (asyncTransaction != null && asyncTransaction?.isCancelled == false) {
            asyncTransaction?.cancel()
            asyncTransaction = null
        }
    }
}
