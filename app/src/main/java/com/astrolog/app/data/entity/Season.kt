package com.astrolog.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seasons")
data class Season(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,           // "Marzo-Junio 2026"
    val month1: String = "",    // "Marzo"
    val month2: String = "",    // "Abril"
    val month3: String = "",    // "Mayo"
    val month4: String = "",    // "Junio"
    val isActive: Boolean = false
)
