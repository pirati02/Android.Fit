package ge.dev.baqari.api.step

import ge.dev.baqari.dayOnly
import ge.dev.baqari.model.StepModel
import ge.dev.baqari.today
import io.realm.Realm
import java.util.*
import java.util.concurrent.TimeUnit

object BaseCalculator {
    fun calculateKilometers(numSteps: Long): Double {
        return (numSteps / 1100.0)
    }

    fun currentSteps(realm: Realm): Double {
        val stepResult = realm.where(StepModel::class.java)
                .equalTo("startDate", Date().today()?.dayOnly())
                .findAll()

        val result = (stepResult?.sum("numSteps") ?: 0.0).toDouble()
        realm.close()
        return result
    }

    fun calculateTimes(text: String): String {
        val realm = Realm.getDefaultInstance()
        val stepResult = realm.where(StepModel::class.java)
                .equalTo("startDate", Date().today()?.dayOnly())
                .findAll()

        val stepTime = stepResult.map { it.endDateTime?.time!! - it.startDateTime?.time!! }.sum()
        val days = TimeUnit.MILLISECONDS.toDays(stepTime)
        val hours = TimeUnit.MILLISECONDS.toHours(stepTime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(stepTime)
        val formattedString = StringBuffer()
        formattedString.append(text)
        if (days > 1) {
            formattedString.append("0$days")
            formattedString.append(":")
        }
        formattedString.append(if (hours > 9) hours.toString() else "0$hours")
        formattedString.append(":")
        formattedString.append(if (minutes > 9) minutes.toString() else "0$minutes")

        return formattedString.toString()
    }
}