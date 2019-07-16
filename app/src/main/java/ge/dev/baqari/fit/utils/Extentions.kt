package ge.dev.baqari.fit.utils

import java.util.Calendar
import java.util.Date
import kotlin.math.pow
import kotlin.math.roundToInt

fun Date.today(): Date? = Calendar.getInstance().time

fun Date.dayOnly(): Date? = Date(this.year, this.month, this.date)

fun Double.round(places: Int): Double {
    var value = this
    if (places < 0) throw IllegalArgumentException()

    val factor = 10.0.pow(places.toDouble()).toLong()
    value *= factor
    val tmp = value.roundToInt()
    return tmp.toDouble() / factor
}