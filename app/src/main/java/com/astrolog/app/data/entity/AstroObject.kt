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
    val alertMonths: String = ""
)
