package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CropEntryDao {
    @Query("SELECT * FROM crop_entries ORDER BY timestamp DESC, id DESC")
    fun getAllEntries(): Flow<List<CropEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CropEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<CropEntryEntity>)

    @Query("DELETE FROM crop_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM crop_entries WHERE LOWER(crop) = LOWER(:cropKey)")
    suspend fun deleteEntriesByCrop(cropKey: String)

    @Query("DELETE FROM crop_entries")
    suspend fun deleteAll()
}
