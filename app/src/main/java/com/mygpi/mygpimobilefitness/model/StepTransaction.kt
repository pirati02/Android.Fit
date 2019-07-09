package com.mygpi.mygpimobilefitness.model

import android.util.Log
import com.mygpi.mygpimobilefitness.dayOnly

import java.util.Date

import io.realm.Realm

class StepTransaction(private val startDate: Date?,
                      private val endDate: Date?,
                      private val num: Long,
                      private val update: Boolean = false) : Realm.Transaction {

    override fun execute(realm: Realm?) {
        val stepModel: StepModel? = if (!update)
            realm?.createObject(StepModel::class.java)
        else
            realm?.where(StepModel::class.java)?.equalTo("startDate", startDate?.dayOnly())?.findAll()?.lastOrNull()

        if (update)
            stepModel?.let {
                it.endDate = endDate?.dayOnly()
                it.endDateTime = endDate
                it.numSteps += num
            }
        else
            stepModel?.let {
                it.startDateTime = startDate
                it.startDate = startDate?.dayOnly()
                it.endDateTime = endDate
                it.endDate = endDate?.dayOnly()
                it.numSteps += num
            }
    }
}
