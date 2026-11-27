package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IconPackDao {
    @Transaction
    @Query("SELECT * FROM icon_packs ORDER BY updatedAt DESC")
    fun observeAllPacks(): Flow<List<IconPackWithIcons>>

    @Query("SELECT COUNT(*) FROM icon_packs")
    suspend fun countPacks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPack(pack: IconPackEntity): Long

    @Update
    suspend fun updatePack(pack: IconPackEntity)

    @Query("DELETE FROM icon_packs WHERE id = :packId")
    suspend fun deletePackById(packId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIcons(icons: List<PackIconItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIcon(icon: PackIconItemEntity): Long

    @Update
    suspend fun updateIcon(icon: PackIconItemEntity)

    @Query("DELETE FROM pack_icons WHERE id = :iconId")
    suspend fun deleteIconById(iconId: Long)
}
