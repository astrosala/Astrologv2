package com.astrolog.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.astrolog.app.data.dao.AstroObjectDao
import com.astrolog.app.data.dao.SeasonDao
import com.astrolog.app.data.dao.SessionDao
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.data.entity.Season
import com.astrolog.app.data.entity.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Session::class, AstroObject::class, Season::class],
    version = 3,
    exportSchema = false
)
abstract class AstroDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun astroObjectDao(): AstroObjectDao
    abstract fun seasonDao(): SeasonDao

    companion object {
        @Volatile private var INSTANCE: AstroDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sessions ADD COLUMN siiSubs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sessions ADD COLUMN siiExpSec INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sessions ADD COLUMN lextSubs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sessions ADD COLUMN lextExpSec INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sessions ADD COLUMN custom1Name TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sessions ADD COLUMN custom1Subs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sessions ADD COLUMN custom1ExpSec INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sessions ADD COLUMN custom2Name TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sessions ADD COLUMN custom2Subs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sessions ADD COLUMN custom2ExpSec INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS seasons (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        month1 TEXT NOT NULL DEFAULT '',
                        month2 TEXT NOT NULL DEFAULT '',
                        month3 TEXT NOT NULL DEFAULT '',
                        month4 TEXT NOT NULL DEFAULT '',
                        isActive INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN seasonId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN visibilityMonth1 TEXT NOT NULL DEFAULT '—'")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN visibilityMonth2 TEXT NOT NULL DEFAULT '—'")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN visibilityMonth3 TEXT NOT NULL DEFAULT '—'")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN visibilityMonth4 TEXT NOT NULL DEFAULT '—'")
                db.execSQL("""
                    INSERT INTO seasons (name, month1, month2, month3, month4, isActive)
                    VALUES ('Marzo-Junio 2026', 'Marzo', 'Abril', 'Mayo', 'Junio', 1)
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AstroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstroDatabase::class.java,
                    "astrolog_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PrepopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val seasonId = database.seasonDao().insert(
                        Season(
                            name = "Marzo-Junio 2026",
                            month1 = "Marzo", month2 = "Abril",
                            month3 = "Mayo", month4 = "Junio",
                            isActive = true
                        )
                    )
                    if (database.astroObjectDao().count() == 0) {
                        database.astroObjectDao().insertAll(defaultObjects(seasonId))
                    }
                }
            }
        }

        private fun defaultObjects(seasonId: Long): List<AstroObject> = listOf(
            AstroObject(name = "Sh2-129 + OU4 Squid", seasonId = seasonId,
                visibilityMarch = "✓", visibilityApril = "★", visibilityMay = "★", visibilityJune = "✓",
                visibilityMonth1 = "✓", visibilityMonth2 = "★", visibilityMonth3 = "★", visibilityMonth4 = "✓",
                mainFilter = "Askar C2 OIII"),
            AstroObject(name = "NGC 7822 + Ced 214", seasonId = seasonId,
                visibilityMarch = "✓", visibilityApril = "★", visibilityMay = "★", visibilityJune = "✓",
                visibilityMonth1 = "✓", visibilityMonth2 = "★", visibilityMonth3 = "★", visibilityMonth4 = "✓",
                mainFilter = "Askar C1 Hα"),
            AstroObject(name = "Sh2-155 Cave Nebula", seasonId = seasonId,
                visibilityMarch = "✓", visibilityApril = "★", visibilityMay = "★", visibilityJune = "★",
                visibilityMonth1 = "✓", visibilityMonth2 = "★", visibilityMonth3 = "★", visibilityMonth4 = "★",
                mainFilter = "Askar C1 Hα"),
            AstroObject(name = "NGC 7380 Wizard Nebula", seasonId = seasonId,
                visibilityMarch = "~", visibilityApril = "✓", visibilityMay = "★", visibilityJune = "★",
                visibilityMonth1 = "~", visibilityMonth2 = "✓", visibilityMonth3 = "★", visibilityMonth4 = "★",
                mainFilter = "Askar C1 Hα"),
            AstroObject(name = "NGC 7023 Iris + IFN", seasonId = seasonId,
                visibilityMarch = "~", visibilityApril = "✓", visibilityMay = "★", visibilityJune = "★",
                visibilityMonth1 = "~", visibilityMonth2 = "✓", visibilityMonth3 = "★", visibilityMonth4 = "★",
                mainFilter = "L-Pro (luna nueva)"),
            AstroObject(name = "vdB 141 Ghost Nebula", seasonId = seasonId,
                visibilityMarch = "~", visibilityApril = "✓", visibilityMay = "★", visibilityJune = "★",
                visibilityMonth1 = "~", visibilityMonth2 = "✓", visibilityMonth3 = "★", visibilityMonth4 = "★",
                mainFilter = "L-Pro (luna nueva)"),
            AstroObject(name = "LBN 777 + IFN Tauro", seasonId = seasonId,
                visibilityMarch = "★", visibilityApril = "★", visibilityMay = "✓", visibilityJune = "~",
                visibilityMonth1 = "★", visibilityMonth2 = "★", visibilityMonth3 = "✓", visibilityMonth4 = "~",
                mainFilter = "L-Pro broadband")
        )
    }
}
