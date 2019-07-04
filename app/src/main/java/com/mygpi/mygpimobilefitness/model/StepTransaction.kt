package com.mygpi.mygpimobilefitness.model

import android.util.Log

import java.util.Date

import io.realm.Realm

class StepTransaction(private val date: Date?, private val num: Long) : Realm.Transaction {

    override fun execute(realm: Realm?) {
        var stepModel = realm?.where(StepModel::class.java)?.equalTo("date", date)?.findFirst()

        if (stepModel == null)
            stepModel = realm?.createObject(StepModel::class.java)
        stepModel?.let {
            it.date = date
            it.numSteps = num
        }
    }
}
