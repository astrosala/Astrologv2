package com.astrolog.app.ui.newsession

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.astrolog.app.data.database.AstroDatabase
import com.astrolog.app.data.entity.Session
import com.astrolog.app.data.repository.AstroRepository
import kotlinx.coroutines.launch

class NewSessionViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: AstroRepository
    private val prefs = app.getSharedPreferences("astrolog_prefs", Context.MODE_PRIVATE)

    init {
        val db = AstroDatabase.getDatabase(app)
        repo = AstroRepository(db.sessionDao(), db.astroObjectDao())
    }

    val objectName = MutableLiveData("")
    val date = MutableLiveData("")
    val conditions = MutableLiveData("Buenas")
    val seeing = MutableLiveData(3)
    val notes = MutableLiveData("")

    val lproSubs = MutableLiveData(0); val lproExpSec = MutableLiveData(0)
    val haSubs = MutableLiveData(0); val haExpSec = MutableLiveData(0)
    val oiiiSubs = MutableLiveData(0); val oiiiExpSec = MutableLiveData(0)
    val siiSubs = MutableLiveData(0); val siiExpSec = MutableLiveData(0)
    val lextSubs = MutableLiveData(0); val lextExpSec = MutableLiveData(0)
    val custom1Subs = MutableLiveData(0); val custom1ExpSec = MutableLiveData(0)
    val custom2Subs = MutableLiveData(0); val custom2ExpSec = MutableLiveData(0)

    val lproTime = MutableLiveData("00:00"); val haTime = MutableLiveData("00:00")
    val oiiiTime = MutableLiveData("00:00"); val siiTime = MutableLiveData("00:00")
    val lextTime = MutableLiveData("00:00"); val custom1Time = MutableLiveData("00:00")
    val custom2Time = MutableLiveData("00:00"); val totalTime = MutableLiveData("00:00")

    val objectNames = MutableLiveData<List<String>>()
    val saveResult = MutableLiveData<Boolean?>()
    var editingSessionId: Long = -1L

    // Filtros configurables
    val showLpro get() = prefs.getBoolean("show_lpro", true)
    val showHa get() = prefs.getBoolean("show_ha", true)
    val showOiii get() = prefs.getBoolean("show_oiii", true)
    val showSii get() = prefs.getBoolean("show_sii", false)
    val showLext get() = prefs.getBoolean("show_lext", false)
    val showCustom1 get() = prefs.getBoolean("show_custom1", false)
    val showCustom2 get() = prefs.getBoolean("show_custom2", false)
    val custom1Name get() = prefs.getString("custom1_name", "Filtro personalizado 1") ?: "Filtro personalizado 1"
    val custom2Name get() = prefs.getString("custom2_name", "Filtro personalizado 2") ?: "Filtro personalizado 2"

    init { loadObjectNames() }

    private fun loadObjectNames() = viewModelScope.launch {
        objectNames.value = repo.getAllObjectNames()
    }

    fun recalcTimes() {
        val lSec = (lproSubs.value ?: 0) * (lproExpSec.value ?: 0)
        val hSec = (haSubs.value ?: 0) * (haExpSec.value ?: 0)
        val oSec = (oiiiSubs.value ?: 0) * (oiiiExpSec.value ?: 0)
        val sSec = (siiSubs.value ?: 0) * (siiExpSec.value ?: 0)
        val leSec = (lextSubs.value ?: 0) * (lextExpSec.value ?: 0)
        val c1Sec = (custom1Subs.value ?: 0) * (custom1ExpSec.value ?: 0)
        val c2Sec = (custom2Subs.value ?: 0) * (custom2ExpSec.value ?: 0)
        lproTime.value = formatTime(lSec); haTime.value = formatTime(hSec)
        oiiiTime.value = formatTime(oSec); siiTime.value = formatTime(sSec)
        lextTime.value = formatTime(leSec); custom1Time.value = formatTime(c1Sec)
        custom2Time.value = formatTime(c2Sec)
        totalTime.value = formatTime(lSec + hSec + oSec + sSec + leSec + c1Sec + c2Sec)
    }

    private fun formatTime(seconds: Int): String {
        if (seconds == 0) return "00:00"
        return "%02d:%02d".format(seconds / 3600, (seconds % 3600) / 60)
    }

    fun loadSession(id: Long) = viewModelScope.launch {
        val s = repo.getSessionById(id) ?: return@launch
        editingSessionId = s.id
        objectName.value = s.objectName; date.value = s.date
        conditions.value = s.conditions; seeing.value = s.seeing; notes.value = s.notes
        lproSubs.value = s.lproSubs; lproExpSec.value = s.lproExpSec
        haSubs.value = s.haSubs; haExpSec.value = s.haExpSec
        oiiiSubs.value = s.oiiiSubs; oiiiExpSec.value = s.oiiiExpSec
        siiSubs.value = s.siiSubs; siiExpSec.value = s.siiExpSec
        lextSubs.value = s.lextSubs; lextExpSec.value = s.lextExpSec
        custom1Subs.value = s.custom1Subs; custom1ExpSec.value = s.custom1ExpSec
        custom2Subs.value = s.custom2Subs; custom2ExpSec.value = s.custom2ExpSec
        recalcTimes()
    }

    fun saveSession() = viewModelScope.launch {
        val name = objectName.value?.trim() ?: ""
        if (name.isBlank() || date.value.isNullOrBlank()) { saveResult.value = false; return@launch }
        val obj = repo.getObjectByName(name)
        val visibility = obj?.let { "Mar:${it.visibilityMarch} Abr:${it.visibilityApril} May:${it.visibilityMay} Jun:${it.visibilityJune}" } ?: ""
        val session = Session(
            id = if (editingSessionId > 0) editingSessionId else 0,
            sessionNumber = if (editingSessionId > 0) 0 else (repo.getMaxSessionNumber() + 1),
            date = date.value!!, objectName = name, visibility = visibility,
            conditions = conditions.value ?: "Buenas", seeing = seeing.value ?: 3,
            lproSubs = lproSubs.value ?: 0, lproExpSec = lproExpSec.value ?: 0,
            haSubs = haSubs.value ?: 0, haExpSec = haExpSec.value ?: 0,
            oiiiSubs = oiiiSubs.value ?: 0, oiiiExpSec = oiiiExpSec.value ?: 0,
            siiSubs = siiSubs.value ?: 0, siiExpSec = siiExpSec.value ?: 0,
            lextSubs = lextSubs.value ?: 0, lextExpSec = lextExpSec.value ?: 0,
            custom1Name = custom1Name, custom1Subs = custom1Subs.value ?: 0, custom1ExpSec = custom1ExpSec.value ?: 0,
            custom2Name = custom2Name, custom2Subs = custom2Subs.value ?: 0, custom2ExpSec = custom2ExpSec.value ?: 0,
            notes = notes.value ?: ""
        )
        if (editingSessionId > 0) repo.updateSession(session) else repo.insertSession(session)
        if (obj == null && name.isNotBlank()) repo.insertObject(com.astrolog.app.data.entity.AstroObject(name = name, status = "En curso"))
        else if (obj != null && obj.status == "Pendiente") repo.updateObject(obj.copy(status = "En curso"))
        saveResult.value = true
    }
}
