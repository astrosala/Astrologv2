package com.astrolog.app.data.repository

import androidx.lifecycle.LiveData
import com.astrolog.app.data.dao.AstroObjectDao
import com.astrolog.app.data.dao.ObjectSummary
import com.astrolog.app.data.dao.SessionDao
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.data.entity.Session

class AstroRepository(
    private val sessionDao: SessionDao,
    private val objectDao: AstroObjectDao
) {
    // --- Sessions ---
    val allSessions: LiveData<List<Session>> = sessionDao.getAllSessions()

    suspend fun getAllSessionsOnce() = sessionDao.getAllSessionsOnce()
    suspend fun getSessionById(id: Long) = sessionDao.getSessionById(id)
    suspend fun insertSession(session: Session): Long = sessionDao.insert(session)
    suspend fun insertAllSessions(sessions: List<Session>) = sessionDao.insertAll(sessions)
    suspend fun updateSession(session: Session) = sessionDao.update(session)
    suspend fun deleteSession(session: Session) = sessionDao.delete(session)
    suspend fun getMaxSessionNumber() = sessionDao.maxSessionNumber() ?: 0
    suspend fun countSessions() = sessionDao.count()

    // Stats
    suspend fun totalLproSubs() = sessionDao.totalLproSubs() ?: 0
    suspend fun totalLproSec() = sessionDao.totalLproSec() ?: 0
    suspend fun totalHaSubs() = sessionDao.totalHaSubs() ?: 0
    suspend fun totalHaSec() = sessionDao.totalHaSec() ?: 0
    suspend fun totalOiiiSubs() = sessionDao.totalOiiiSubs() ?: 0
    suspend fun totalOiiiSec() = sessionDao.totalOiiiSec() ?: 0
    suspend fun avgSeeing() = sessionDao.avgSeeing() ?: 0f
    suspend fun getSummaryByObject(): List<ObjectSummary> = sessionDao.getSummaryByObject()

    // --- AstroObjects ---
    val allObjects: LiveData<List<AstroObject>> = objectDao.getAllObjects()

    suspend fun getAllObjectsOnce() = objectDao.getAllObjectsOnce()
    suspend fun getAllObjectNames() = objectDao.getAllObjectNames()
    suspend fun getObjectByName(name: String) = objectDao.getByName(name)
    suspend fun insertObject(obj: AstroObject): Long = objectDao.insert(obj)
    suspend fun insertAllObjects(objects: List<AstroObject>) = objectDao.insertAll(objects)
    suspend fun updateObject(obj: AstroObject) = objectDao.update(obj)
    suspend fun deleteObject(obj: AstroObject) = objectDao.delete(obj)
    suspend fun getObjectsWithAlerts() = objectDao.getObjectsWithAlerts()
    suspend fun countObjects() = objectDao.count()
}
