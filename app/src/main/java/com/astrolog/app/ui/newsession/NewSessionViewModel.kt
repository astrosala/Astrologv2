package com.astrolog.app.ui.newsession

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.astrolog.app.data.database.AstroDatabase
import com.astrolog.app.data.entity.Session
import com.astrolog.app.data.repository.AstroRepository
import kotlinx.coroutines.launch

class NewSessionViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: AstroRepository

    init {
        val db = AstroDatabase.getDatabase(app)
        repo = AstroRepository(db.sessionDao(), db.astroObjectDao())
    }

    // Campos del formulario
    val objectName = MutableLiveData("")
    val date = MutableLiveData("")
    val conditions = MutableLiveData("Buenas")
    val seeing = MutableLiveData(3)
    val notes = MutableLiveData("")

    // L-Pro
    val lproSubs = MutableLiveData(0)
    val lproExpSec = MutableLiveData(0)

    // Hα
    val haSubs = MutableLiveData(0)
    val haExpSec = MutableLiveData(0)

    // OIII
    val oiiiSubs = MutableLiveData(0)
    val oiiiExpSec = MutableLiveData(0)

    // Tiempos calculados automáticamente (HH:MM)
    val lproTime = MutableLiveData("00:00")
    val haTime = MutableLiveData("00:00")
    val oiiiTime = MutableLiveData("00:00")
    val totalTime = MutableLiveData("00:00")

    // Nombres de objetos para autocompletado (offline)
    val objectNames = MutableLiveData<List<String>>()

    val saveResult = MutableLiveData<Boolean?>()
    var editingSessionId: Long = -1L

    init {
        loadObjectNames()
    }

    private fun loadObjectNames() = viewModelScope.launch {
        objectNames.value = repo.getAllObjectNames()
    }

    // Recalcula HH:MM al cambiar subs o exposición
    fun recalcTimes() {
        val lSec = (lproSubs.value ?: 0) * (lproExpSec.value ?: 0)
        val hSec = (haSubs.value ?: 0) * (haExpSec.value ?: 0)
        val oSec = (oiiiSubs.value ?: 0) * (oiiiExpSec.value ?: 0)
        lproTime.value = formatTime(lSec)
        haTime.value = formatTime(hSec)
        oiiiTime.value = formatTime(oSec)
        totalTime.value = formatTime(lSec + hSec + oSec)
    }

    private fun formatTime(seconds: Int): String {
        if (seconds == 0) return "00:00"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return "%02d:%02d".format(h, m)
    }

    fun loadSession(id: Long) = viewModelScope.launch {
        val s = repo.getSessionById(id) ?: return@launch
        editingSessionId = s.id
        objectName.value = s.objectName
        date.value = s.date
        conditions.value = s.conditions
        seeing.value = s.seeing
        notes.value = s.notes
        lproSubs.value = s.lproSubs
        lproExpSec.value = s.lproExpSec
        haSubs.value = s.haSubs
        haExpSec.value = s.haExpSec
        oiiiSubs.value = s.oiiiSubs
        oiiiExpSec.value = s.oiiiExpSec
        recalcTimes()
    }

    fun saveSession() = viewModelScope.launch {
        val name = objectName.value?.trim() ?: ""
        if (name.isBlank() || date.value.isNullOrBlank()) {
            saveResult.value = false
            return@launch
        }

        // Buscar visibilidad del objeto en la DB
        val obj = repo.getObjectByName(name)
        val visibility = obj?.let {
            "Mar:${it.visibilityMarch} Abr:${it.visibilityApril} May:${it.visibilityMay} Jun:${it.visibilityJune}"
        } ?: ""

        val session = Session(
            id = if (editingSessionId > 0) editingSessionId else 0,
            sessionNumber = if (editingSessionId > 0) 0 else (repo.getMaxSessionNumber() + 1),
            date = date.value!!,
            objectName = name,
            visibility = visibility,
            conditions = conditions.value ?: "Buenas",
            seeing = seeing.value ?: 3,
            lproSubs = lproSubs.value ?: 0,
            lproExpSec = lproExpSec.value ?: 0,
            haSubs = haSubs.value ?: 0,
            haExpSec = haExpSec.value ?: 0,
            oiiiSubs = oiiiSubs.value ?: 0,
            oiiiExpSec = oiiiExpSec.value ?: 0,
            notes = notes.value ?: ""
        )

        if (editingSessionId > 0) repo.updateSession(session)
        else repo.insertSession(session)

        // Si el objeto no existe en la wishlist, añadirlo como "En curso"
        if (obj == null && name.isNotBlank()) {
            repo.insertObject(
                com.astrolog.app.data.entity.AstroObject(
                    name = name,
                    status = "En curso"
                )
            )
        } else if (obj != null && obj.status == "Pendiente") {
            repo.updateObject(obj.copy(status = "En curso"))
        }

        saveResult.value = true
    }
}
