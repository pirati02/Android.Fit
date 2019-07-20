package ge.dev.baqari.myfit.utils

import android.os.Build

object Device {
    fun isXiaomi(): Boolean {
        return Build.MANUFACTURER?.toLowerCase()?.equals("xiaomi") ?: false
    }
}