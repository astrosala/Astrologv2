package com.astrolog.app.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.astrolog.app.data.dao.ObjectSummary
import com.astrolog.app.data.database.AstroDatabase
import com.astrolog.app.data.repository.AstroRepository
import kotlinx.coroutines.launch

data class StatsData(
    val totalSessions: Int,
    val totalTime: String,
    val totalSubs: Int,
    val avgSeeing: Float,
    val lproTime: String,
    val haTime: String,
    val oiiiTime: String,
    val summaryByObject: List<ObjectSummary>
)

class StatsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: AstroRepository
    val stats = MutableLiveData<StatsData>()

    init {
        val db = AstroDatabase.getDatabase(app)
        repo = AstroRepository(db.sessionDao(), db.astroObjectDao(), db.seasonDao())
        loadStats()
    }

    fun loadStats() = viewModelScope.launch {
        val sessions = repo.countSessions()
        val lproSec = repo.totalLproSec()
        val haSec = repo.totalHaSec()
        val oiiiSec = repo.totalOiiiSec()
        val totalSec = lproSec + haSec + oiiiSec
        val totalSubs = (repo.totalLproSubs()) + (repo.totalHaSubs()) + (repo.totalOiiiSubs())
        val avgSeeing = repo.avgSeeing()
        val summaryByObject = repo.getSummaryByObject()

        stats.value = StatsData(
            totalSessions = sessions,
            totalTime = formatTime(totalSec),
            totalSubs = totalSubs,
            avgSeeing = avgSeeing,
            lproTime = formatTime(lproSec),
            haTime = formatTime(haSec),
            oiiiTime = formatTime(oiiiSec),
            summaryByObject = summaryByObject
        )
    }

    private fun formatTime(seconds: Int): String {
        if (seconds == 0) return "—"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return "%02d:%02d".format(h, m)
    }
}
