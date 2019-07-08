package com.mygpi.mygpimobilefitness.model

import android.util.Log

import java.util.Date

import io.realm.Realm

class StepTransaction(private val startDate: Date?, private val endDate: Date?, private val num: Long, private val update: Boolean = false) : Realm.Transaction {

    override fun execute(realm: Realm?) {
        val stepModel: StepModel?

        if (!update)
            stepModel = realm?.createObject(StepModel::class.java)
        else
            stepModel = realm?.where(StepModel::class.java)?.equalTo("startDate", startDate)?.findFirst()

        stepModel?.let {
            it.startDate = startDate
            it.endDate = endDate
            it.numSteps += num
        }
    }
}
