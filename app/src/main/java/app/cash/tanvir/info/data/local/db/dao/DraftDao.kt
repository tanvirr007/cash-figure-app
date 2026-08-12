package app.cash.tanvir.info.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.cash.tanvir.info.data.local.db.entity.DraftEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for draft operations.
 */
@Dao
interface DraftDao {

    @Query("SELECT * FROM drafts ORDER BY updatedAt DESC")
    fun getAllDrafts(): Flow<List<DraftEntity>>

    @Query("SELECT * FROM drafts WHERE id = :id LIMIT 1")
    suspend fun getDraftById(id: Long): DraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: DraftEntity): Long

    @Query("DELETE FROM drafts WHERE id = :id")
    suspend fun deleteDraft(id: Long)

    @Query("SELECT COUNT(*) FROM drafts")
    suspend fun getDraftCount(): Int

    @Query("DELETE FROM drafts")
    suspend fun clearAllDrafts()
}
