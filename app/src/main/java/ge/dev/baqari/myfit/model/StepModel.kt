package ge.dev.baqari.myfit.model

import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.Index

open class StepModel : RealmObject() {
    @Index
    var startDate: Date? = null
        internal set
    @Index
    var endDate: Date? = null
        internal set

    @Index
    var startDateTime: Date? = null
        internal set
    @Index
    var endDateTime: Date? = null
        internal set

    var numSteps: Long = 0
        internal set
}