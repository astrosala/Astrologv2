package com.astrolog.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "astro_objects")
data class AstroObject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val seasonId: Long = 0,
    val visibilityMonth1: String = "—",
    val visibilityMonth2: String = "—",
    val visibilityMonth3: String = "—",
    val visibilityMonth4: String = "—",
    val visibilityMarch: String = "—",
    val visibilityApril: String = "—",
    val visibilityMay: String = "—",
    val visibilityJune: String = "—",
    val mainFilter: String = "",
    val status: String = "Pendiente",
    val notes: String = "",
    val alertEnabled: Boolean = false,
    val alertMonths: String = "",
    // Referencia de subs objetivo por filtro
    val refLproSubs: Int = 0,
    val refLproExpSec: Int = 0,
    val refHaSubs: Int = 0,
    val refHaExpSec: Int = 0,
    val refOiiiSubs: Int = 0,
    val refOiiiExpSec: Int = 0,
    val refSiiSubs: Int = 0,
    val refSiiExpSec: Int = 0,
    val refLextSubs: Int = 0,
    val refLextExpSec: Int = 0,
    val refCustom1Subs: Int = 0,
    val refCustom1ExpSec: Int = 0,
    val refCustom2Subs: Int = 0,
    val refCustom2ExpSec: Int = 0
) {
    // Tiempo objetivo total en segundos
    val refTotalSec: Int get() =
        refLproSubs * refLproExpSec +
        refHaSubs * refHaExpSec +
        refOiiiSubs * refOiiiExpSec +
        refSiiSubs * refSiiExpSec +
        refLextSubs * refLextExpSec +
        refCustom1Subs * refCustom1ExpSec +
        refCustom2Subs * refCustom2ExpSec

    fun formatTime(seconds: Int): String {
        if (seconds == 0) return "—"
        return "%02d:%02d".format(seconds / 3600, (seconds % 3600) / 60)
    }

    val refTotalTime: String get() = formatTime(refTotalSec)
}
