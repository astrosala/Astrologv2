package com.astrolog.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionNumber: Int,
    val date: String,
    val objectName: String,
    val visibility: String = "",
    val conditions: String = "",
    val seeing: Int = 3,

    // Filtros fijos
    val lproSubs: Int = 0,
    val lproExpSec: Int = 0,
    val haSubs: Int = 0,
    val haExpSec: Int = 0,
    val oiiiSubs: Int = 0,
    val oiiiExpSec: Int = 0,
    val siiSubs: Int = 0,
    val siiExpSec: Int = 0,
    val lextSubs: Int = 0,
    val lextExpSec: Int = 0,

    // Filtros personalizables
    val custom1Name: String = "",
    val custom1Subs: Int = 0,
    val custom1ExpSec: Int = 0,
    val custom2Name: String = "",
    val custom2Subs: Int = 0,
    val custom2ExpSec: Int = 0,

    val notes: String = ""
) {
    private fun formatTime(seconds: Int): String {
        if (seconds == 0) return "—"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return "%02d:%02d".format(h, m)
    }

    val lproTotalSec get() = lproSubs * lproExpSec
    val haTotalSec get() = haSubs * haExpSec
    val oiiiTotalSec get() = oiiiSubs * oiiiExpSec
    val siiTotalSec get() = siiSubs * siiExpSec
    val lextTotalSec get() = lextSubs * lextExpSec
    val custom1TotalSec get() = custom1Subs * custom1ExpSec
    val custom2TotalSec get() = custom2Subs * custom2ExpSec

    val totalSec get() = lproTotalSec + haTotalSec + oiiiTotalSec +
            siiTotalSec + lextTotalSec + custom1TotalSec + custom2TotalSec

    val lproTime get() = formatTime(lproTotalSec)
    val haTime get() = formatTime(haTotalSec)
    val oiiiTime get() = formatTime(oiiiTotalSec)
    val siiTime get() = formatTime(siiTotalSec)
    val lextTime get() = formatTime(lextTotalSec)
    val custom1Time get() = formatTime(custom1TotalSec)
    val custom2Time get() = formatTime(custom2TotalSec)
    val totalTime get() = formatTime(totalSec)
}
