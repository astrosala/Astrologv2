package com.astrolog.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
    version = 2,
    exportSchema = false
)
abstract class AstroDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun astroObjectDao(): AstroObjectDao

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

        fun getDatabase(context: Context): AstroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstroDatabase::class.java,
                    "astrolog_database"
                )
                    .addMigrations(MIGRATION_1_2)
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
                    if (database.astroObjectDao().count() == 0) {
                        database.astroObjectDao().insertAll(defaultObjects())
                    }
                }
            }
        }

        private fun defaultObjects(): List<AstroObject> = listOf(
            AstroObject(name = "Sh2-129 + OU4 Squid", visibilityMarch = "✓", visibilityApril = "★", visibilityMay = "★", visibilityJune = "✓", mainFilter = "Askar C2 OIII"),
            AstroObject(name = "NGC 7822 + Ced 214", visibilityMarch = "✓", visibilityApril = "★", visibilityMay = "★", visibilityJune = "✓", mainFilter = "Askar C1 Hα"),
            AstroObject(name = "Sh2-155 Cave Nebula", visibilityMarch = "✓", visibilityApril = "★", visibilityMay = "★", visibilityJune = "★", mainFilter = "Askar C1 Hα"),
            AstroObject(name = "NGC 7380 Wizard Nebula", visibilityMarch = "~", visibilityApril = "✓", visibilityMay = "★", visibilityJune = "★", mainFilter = "Askar C1 Hα"),
            AstroObject(name = "NGC 7023 Iris + IFN", visibilityMarch = "~", visibilityApril = "✓", visibilityMay = "★", visibilityJune = "★", mainFilter = "L-Pro (luna nueva)"),
            AstroObject(name = "vdB 141 Ghost Nebula", visibilityMarch = "~", visibilityApril = "✓", visibilityMay = "★", visibilityJune = "★", mainFilter = "L-Pro (luna nueva)"),
            AstroObject(name = "LBN 777 + IFN Tauro", visibilityMarch = "★", visibilityApril = "★", visibilityMay = "✓", visibilityJune = "~", mainFilter = "L-Pro broadband")
        )
    }
}
```

Pulsa **Commit changes**.

---

**PASO 3 — Pantalla de Ajustes (nuevo archivo)**

Ve a **Code** → carpeta `app/src/main/java/com/astrolog/app/ui/` → pulsa **"Add file"** → **"Create new file"**

En el nombre escribe:
```
app/src/main/java/com/astrolog/app/ui/settings/SettingsFragment.kt
