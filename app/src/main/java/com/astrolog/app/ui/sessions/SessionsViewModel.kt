package com.astrolog.app.ui.sessions

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astrolog.app.data.database.AstroDatabase
import com.astrolog.app.data.entity.Session
import com.astrolog.app.data.repository.AstroRepository
import com.astrolog.app.util.ExcelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: AstroRepository

    init {
        val db = AstroDatabase.getDatabase(app)
        repo = AstroRepository(db.sessionDao(), db.astroObjectDao(), db.seasonDao())
    }

    val allSessions = repo.allSessions
    val allObjects = repo.allObjects

    fun deleteSession(session: Session) = viewModelScope.launch {
        repo.deleteSession(session)
    }

    // Importa el Excel del usuario y añade las sesiones que no existan
    fun importFromExcel(uri: Uri, onDone: (Int) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val imported = ExcelManager.importSessions(getApplication(), uri)
        val existing = repo.getAllSessionsOnce().map { it.sessionNumber }.toSet()
        val newOnes = imported.filter { it.sessionNumber !in existing }
        if (newOnes.isNotEmpty()) repo.insertAllSessions(newOnes)

        // Importar también los objetos del calendario
        val importedObjects = ExcelManager.importObjects(getApplication(), uri)
        importedObjects.forEach { obj ->
            if (repo.getObjectByName(obj.name) == null) repo.insertObject(obj)
        }

        viewModelScope.launch(Dispatchers.Main) { onDone(newOnes.size) }
    }

    // Exportar
    fun exportToExcel(uri: Uri, onDone: (Boolean) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val sessions = repo.getAllSessionsOnce()
            val objects = repo.getAllObjectsOnce()
            val stream = getApplication<Application>().contentResolver.openOutputStream(uri)!!
            ExcelManager.exportToExcel(sessions, objects, stream)
            stream.close()
            viewModelScope.launch(Dispatchers.Main) { onDone(true) }
        } catch (e: Exception) {
            e.printStackTrace()
            viewModelScope.launch(Dispatchers.Main) { onDone(false) }
        }
    }

    fun exportToCsv(uri: Uri, onDone: (Boolean) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val sessions = repo.getAllSessionsOnce()
            val stream = getApplication<Application>().contentResolver.openOutputStream(uri)!!
            ExcelManager.exportToCsv(sessions, stream)
            stream.close()
            viewModelScope.launch(Dispatchers.Main) { onDone(true) }
        } catch (e: Exception) {
            e.printStackTrace()
            viewModelScope.launch(Dispatchers.Main) { onDone(false) }
        }
    }
}
