package com.astrolog.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionNumber: Int,
    val date: String,           // DD/MM/YYYY
    val objectName: String,
    val visibility: String,     // "Mar:✓ Abr:★ May:★ Jun:★"
    val conditions: String,     // Excelentes / Muy buenas / Buenas / Regulares / Malas
    val seeing: Int,            // 1-5

    // L-Pro
    val lproSubs: Int = 0,
    val lproExpSec: Int = 0,

    // Hα Askar C1
    val haSubs: Int = 0,
    val haExpSec: Int = 0,

    // OIII Askar C2
    val oiiiSubs: Int = 0,
    val oiiiExpSec: Int = 0,

    val notes: String = ""
) {
    // Tiempo total de cada filtro en segundos
    val lproTotalSec: Int get() = lproSubs * lproExpSec
    val haTotalSec: Int get() = haSubs * haExpSec
    val oiiiTotalSec: Int get() = oiiiSubs * oiiiExpSec
    val totalSec: Int get() = lproTotalSec + haTotalSec + oiiiTotalSec

    fun formatTime(seconds: Int): String {
        if (seconds == 0) return "—"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return "%02d:%02d".format(h, m)
    }

    val lproTime: String get() = formatTime(lproTotalSec)
    val haTime: String get() = formatTime(haTotalSec)
    val oiiiTime: String get() = formatTime(oiiiTotalSec)
    val totalTime: String get() = formatTime(totalSec)
}
