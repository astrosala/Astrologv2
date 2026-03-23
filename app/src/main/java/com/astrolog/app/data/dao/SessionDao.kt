package com.astrolog.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.astrolog.app.data.entity.Session

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY id DESC")
    fun getAllSessions(): LiveData<List<Session>>

    @Query("SELECT * FROM sessions ORDER BY id DESC")
    suspend fun getAllSessionsOnce(): List<Session>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): Session?

    @Query("SELECT * FROM sessions WHERE objectName = :name ORDER BY id DESC")
    fun getSessionsByObject(name: String): LiveData<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: Session): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<Session>)

    @Update
    suspend fun update(session: Session)

    @Delete
    suspend fun delete(session: Session)

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun count(): Int

    @Query("SELECT MAX(sessionNumber) FROM sessions")
    suspend fun maxSessionNumber(): Int?

    // Stats: total subs y segundos por filtro
    @Query("SELECT SUM(lproSubs) FROM sessions")
    suspend fun totalLproSubs(): Int?

    @Query("SELECT SUM(lproSubs * lproExpSec) FROM sessions")
    suspend fun totalLproSec(): Int?

    @Query("SELECT SUM(haSubs) FROM sessions")
    suspend fun totalHaSubs(): Int?

    @Query("SELECT SUM(haSubs * haExpSec) FROM sessions")
    suspend fun totalHaSec(): Int?

    @Query("SELECT SUM(oiiiSubs) FROM sessions")
    suspend fun totalOiiiSubs(): Int?

    @Query("SELECT SUM(oiiiSubs * oiiiExpSec) FROM sessions")
    suspend fun totalOiiiSec(): Int?

    @Query("SELECT AVG(seeing) FROM sessions")
    suspend fun avgSeeing(): Float?

    // Resumen acumulado por objeto
    @Query("""
        SELECT objectName,
               SUM(lproSubs) as lproSubs,
               SUM(lproSubs * lproExpSec) as lproSec,
               SUM(haSubs) as haSubs,
               SUM(haSubs * haExpSec) as haSec,
               SUM(oiiiSubs) as oiiiSubs,
               SUM(oiiiSubs * oiiiExpSec) as oiiiSec,
               SUM(lproSubs*lproExpSec + haSubs*haExpSec + oiiiSubs*oiiiExpSec) as totalSec
        FROM sessions
        GROUP BY objectName
        ORDER BY totalSec DESC
    """)
    suspend fun getSummaryByObject(): List<ObjectSummary>
}

data class ObjectSummary(
    val objectName: String,
    val lproSubs: Int,
    val lproSec: Int,
    val haSubs: Int,
    val haSec: Int,
    val oiiiSubs: Int,
    val oiiiSec: Int,
    val totalSec: Int
) {
    fun formatTime(seconds: Int): String {
        if (seconds == 0) return "—"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return "%02d:%02d".format(h, m)
    }
    val lproTime get() = formatTime(lproSec)
    val haTime get() = formatTime(haSec)
    val oiiiTime get() = formatTime(oiiiSec)
    val totalTime get() = formatTime(totalSec)
}
