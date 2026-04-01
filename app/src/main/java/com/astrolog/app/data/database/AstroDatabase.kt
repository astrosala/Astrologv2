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
    version = 4,
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Campos de referencia de subs en astro_objects
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refLproSubs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refLproExpSec INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refHaSubs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refHaExpSec INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refOiiiSubs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refOiiiExpSec INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refSiiSubs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refSiiExpSec INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refLextSubs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refLextExpSec INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refCustom1Subs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refCustom1ExpSec INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refCustom2Subs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE astro_objects ADD COLUMN refCustom2ExpSec INTEGER NOT NULL DEFAULT 0")
                // Eliminar objetos precargados — app arranca vacía
                db.execSQL("DELETE FROM astro_objects")
            }
        }

        fun getDatabase(context: Context): AstroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstroDatabase::class.java,
                    "astrolog_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
