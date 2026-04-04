package com.astrolog.app.ui.wishlist

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.astrolog.app.data.database.AstroDatabase
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.data.entity.Season
import com.astrolog.app.data.repository.AstroRepository
import kotlinx.coroutines.launch

class WishlistViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: AstroRepository
    private val prefs = app.getSharedPreferences("astrolog_prefs", Context.MODE_PRIVATE)

    val activeSeason = MutableLiveData<Season?>()
    val allSeasons: MutableLiveData<List<Season>> = MutableLiveData()
    
    // Cambiado: Declaramos el LiveData pero no lo inicializamos aquí para evitar el error de 'repo'
    val allObjects: LiveData<List<AstroObject>>

    init {
        val db = AstroDatabase.getDatabase(app)
        repo = AstroRepository(db.sessionDao(), db.astroObjectDao(), db.seasonDao())
        
        // Ahora inicializamos allObjects después de que repo ya existe 
        allObjects = repo.allObjects
        
        loadData()
    }

    private fun loadData() = viewModelScope.launch {
        activeSeason.value = repo.getActiveSeason()
        // Cargamos las temporadas y las exponemos
        repo.allSeasons.observeForever { allSeasons.value = it }
    }

    fun saveObject(obj: AstroObject) = viewModelScope.launch {
        if (obj.name.isBlank()) return@launch
        if (obj.id == 0L) repo.insertObject(obj)
        else repo.updateObject(obj)
    }

    fun toggleAlert(obj: AstroObject, enabled: Boolean, months: String) = viewModelScope.launch {
        repo.updateObject(obj.copy(alertEnabled = enabled, alertMonths = months))
    }

    fun cycleStatus(obj: AstroObject) = viewModelScope.launch {
        val next = when (obj.status) {
            "Pendiente" -> "En curso"
            "En curso" -> "Completado"
            else -> "Pendiente"
        }
        repo.updateObject(obj.copy(status = next))
    }

    fun deleteObject(obj: AstroObject) = viewModelScope.launch {
        repo.deleteObject(obj)
    }
}
