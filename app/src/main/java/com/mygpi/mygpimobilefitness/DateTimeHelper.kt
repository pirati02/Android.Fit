package com.mygpi.mygpimobilefitness

import java.util.Calendar
import java.util.Date

fun Date.today(): Date? = Date(this.year, this.month, this.date)

fun Date.getBeforeSeventhDate(): Date? {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DAY_OF_MONTH, -7)
    return calendar.time
}