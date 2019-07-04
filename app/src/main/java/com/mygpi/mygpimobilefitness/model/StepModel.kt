package com.mygpi.mygpimobilefitness.model

import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.Index

open class StepModel : RealmObject() {
    @Index
    var date: Date? = null
        internal set
    var numSteps: Long = 0
        internal set
}