package com.astrolog.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.astrolog.app.data.dao.AstroObjectDao
import com.astrolog.app.data.dao.SessionDao
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.data.entity.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Session::class, AstroObject::class],
    version = 1,
    exportSchema = false
)
abstract class AstroDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun astroObjectDao(): AstroObjectDao

    companion object {
        @Volatile
        private var INSTANCE: AstroDatabase? = null

        fun getDatabase(context: Context): AstroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstroDatabase::class.java,
                    "astrolog_database"
                )
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Pre-carga los objetos del Excel de la temporada Marzo-Junio 2026
    private class PrepopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    if (database.astroObjectDao().count() == 0) {
                        database.astroObjectDao().insertAll(defaultObjects())
                    }
                }
            }
        }

        private fun defaultObjects(): List<AstroObject> = listOf(
            AstroObject(
                name = "Sh2-129 + OU4 Squid",
                visibilityMarch = "✓", visibilityApril = "★",
                visibilityMay = "★", visibilityJune = "✓",
                mainFilter = "Askar C2 OIII",
                status = "Pendiente"
            ),
            AstroObject(
                name = "NGC 7822 + Ced 214",
                visibilityMarch = "✓", visibilityApril = "★",
                visibilityMay = "★", visibilityJune = "✓",
                mainFilter = "Askar C1 Hα",
                status = "En curso"
            ),
            AstroObject(
                name = "Sh2-155 Cave Nebula",
                visibilityMarch = "✓", visibilityApril = "★",
                visibilityMay = "★", visibilityJune = "★",
                mainFilter = "Askar C1 Hα",
                status = "En curso"
            ),
            AstroObject(
                name = "NGC 7380 Wizard Nebula",
                visibilityMarch = "~", visibilityApril = "✓",
                visibilityMay = "★", visibilityJune = "★",
                mainFilter = "Askar C1 Hα",
                status = "Pendiente"
            ),
            AstroObject(
                name = "NGC 7023 Iris + IFN",
                visibilityMarch = "~", visibilityApril = "✓",
                visibilityMay = "★", visibilityJune = "★",
                mainFilter = "L-Pro (luna nueva)",
                status = "Pendiente"
            ),
            AstroObject(
                name = "vdB 141 Ghost Nebula",
                visibilityMarch = "~", visibilityApril = "✓",
                visibilityMay = "★", visibilityJune = "★",
                mainFilter = "L-Pro (luna nueva)",
                status = "Pendiente"
            ),
            AstroObject(
                name = "LBN 777 + IFN Tauro",
                visibilityMarch = "★", visibilityApril = "★",
                visibilityMay = "✓", visibilityJune = "~",
                mainFilter = "L-Pro broadband",
                status = "Pendiente"
            )
        )
    }
}
