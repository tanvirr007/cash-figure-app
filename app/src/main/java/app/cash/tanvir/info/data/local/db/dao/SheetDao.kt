package app.cash.tanvir.info.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.cash.tanvir.info.data.local.db.entity.SheetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for sheet database operations.
 */
@Dao
interface SheetDao {

    @Query("SELECT * FROM sheets WHERE isDeleted = 0 AND id != -1 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllSheets(): Flow<List<SheetEntity>>

    @Query("SELECT * FROM sheets WHERE isDeleted = 0 AND id != -1 AND (name LIKE '%' || :query || '%' OR grandTotal LIKE '%' || :query || '%') ORDER BY isPinned DESC, updatedAt DESC")
    fun searchSheets(query: String): Flow<List<SheetEntity>>

    @Query("SELECT * FROM sheets WHERE isDeleted = 1 AND id != -1 ORDER BY updatedAt DESC")
    fun getDeletedSheets(): Flow<List<SheetEntity>>

    @Query("SELECT * FROM sheets WHERE id = :id LIMIT 1")
    suspend fun getSheetById(id: Long): SheetEntity?

    @Query("SELECT * FROM sheets WHERE id = -1 LIMIT 1")
    fun getCurrentSheet(): Flow<SheetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSheet(sheet: SheetEntity): Long

    @Update
    suspend fun updateSheet(sheet: SheetEntity)

    @Query("UPDATE sheets SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteSheet(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sheets SET isDeleted = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreSheet(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM sheets WHERE id = :id")
    suspend fun hardDeleteSheet(id: Long)

    @Query("SELECT COUNT(*) FROM sheets WHERE isDeleted = 0 AND id != -1")
    suspend fun getHistorySheetCount(): Int

    @Query("DELETE FROM sqlite_sequence WHERE name = 'sheets'")
    suspend fun resetAutoIncrement()

    @Query("DELETE FROM sheets WHERE id != -1")
    suspend fun clearAllHistory()
}
