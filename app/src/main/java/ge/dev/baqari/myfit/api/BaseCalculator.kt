package ge.dev.baqari.myfit.api

import ge.dev.baqari.myfit.utils.dayOnly
import ge.dev.baqari.myfit.model.StepModel
import ge.dev.baqari.myfit.utils.today
import io.realm.Realm
import java.util.*
import java.util.concurrent.TimeUnit

object BaseCalculator {
    fun calculateKilometers(numSteps: Long): Double {
        return (numSteps / 1300.0)
    }

    fun currentSteps(realm: Realm): Double {
        val stepResult = realm.where(StepModel::class.java)
                .equalTo("startDate", Date().today()?.dayOnly())
                .findAll()

        val result = (stepResult?.sum("numSteps") ?: 0.0).toDouble()
        realm.close()
        return result
    }
}