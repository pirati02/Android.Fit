package com.mygpi.mygpimobilefitness

import java.util.Calendar
import java.util.Date
import kotlin.math.pow
import kotlin.math.roundToInt

fun Date.today(): Date? = Date(this.year, this.month, this.date)

fun Date.getBeforeSeventhDate(): Date? {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DAY_OF_MONTH, -7)
    return calendar.time
}

fun Double.round(places: Int): Double {
    var value = this
    if (places < 0) throw IllegalArgumentException()

    val factor = 10.0.pow(places.toDouble()).toLong()
    value *= factor
    val tmp = value.roundToInt()
    return tmp.toDouble() / factor
}