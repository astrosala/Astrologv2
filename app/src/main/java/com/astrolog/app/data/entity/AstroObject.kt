package com.astrolog.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "astro_objects")
data class AstroObject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val visibilityMarch: String = "—",   // ★ / ✓ / ~ / —
    val visibilityApril: String = "—",
    val visibilityMay: String = "—",
    val visibilityJune: String = "—",
    val mainFilter: String = "",          // L-Pro / Askar C1 Hα / Askar C2 OIII
    val status: String = "Pendiente",    // Pendiente / En curso / Completado
    val notes: String = "",
    val alertEnabled: Boolean = false,
    val alertMonths: String = ""         // meses separados por coma: "Abril,Mayo"
)
