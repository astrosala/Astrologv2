package com.astrolog.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.astrolog.app.data.entity.AstroObject

@Dao
interface AstroObjectDao {

    @Query("SELECT * FROM astro_objects ORDER BY name ASC")
    fun getAllObjects(): LiveData<List<AstroObject>>
    
    @Query("SELECT * FROM astro_objects WHERE seasonId = :sId ORDER BY name ASC")
    fun getObjectsBySeason(sId: Long): LiveData<List<AstroObject>>
    
    @Query("SELECT * FROM astro_objects ORDER BY name ASC")
    suspend fun getAllObjectsOnce(): List<AstroObject>

    @Query("SELECT name FROM astro_objects ORDER BY name ASC")
    suspend fun getAllObjectNames(): List<String>

    @Query("SELECT * FROM astro_objects WHERE id = :id")
    suspend fun getById(id: Long): AstroObject?

    @Query("SELECT * FROM astro_objects WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): AstroObject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(obj: AstroObject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(objects: List<AstroObject>)

    @Update
    suspend fun update(obj: AstroObject)

    @Delete
    suspend fun delete(obj: AstroObject)

    @Query("SELECT * FROM astro_objects WHERE alertEnabled = 1")
    suspend fun getObjectsWithAlerts(): List<AstroObject>

    @Query("SELECT COUNT(*) FROM astro_objects")
    suspend fun count(): Int
}
