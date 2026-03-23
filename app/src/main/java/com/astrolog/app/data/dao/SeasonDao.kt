package com.astrolog.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.astrolog.app.data.entity.Season

@Dao
interface SeasonDao {

    @Query("SELECT * FROM seasons ORDER BY id DESC")
    fun getAllSeasons(): LiveData<List<Season>>

    @Query("SELECT * FROM seasons ORDER BY id DESC")
    suspend fun getAllSeasonsOnce(): List<Season>

    @Query("SELECT * FROM seasons WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSeason(): Season?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(season: Season): Long

    @Update
    suspend fun update(season: Season)

    @Delete
    suspend fun delete(season: Season)

    @Query("UPDATE seasons SET isActive = 0")
    suspend fun deactivateAll()
}
