package app.cash.tanvir.info.domain.repository

import app.cash.tanvir.info.domain.model.Sheet
import kotlinx.coroutines.flow.Flow

/**
 * Interface for sheet repository operations.
 */
interface SheetRepository {
    fun getAllSheets(): Flow<List<Sheet>>
    fun searchSheets(query: String): Flow<List<Sheet>>
    fun getCurrentSheet(): Flow<Sheet?>
    suspend fun getSheetById(id: Long): Sheet?
    suspend fun saveCurrentSheet(quantities: Map<Int, String>, grandTotal: Long, totalPieces: Long, activeDenominations: Int)
    suspend fun saveSheet(sheet: Sheet): Long
    suspend fun saveSheetAndResetCurrent(sheet: Sheet): Long
    suspend fun updateSheet(sheet: Sheet)
    suspend fun softDeleteSheet(id: Long)
    suspend fun restoreSheet(id: Long)
    suspend fun hardDeleteSheet(id: Long)
    suspend fun clearAllHistory()
    suspend fun restoreSheets(sheets: List<Sheet>)
    fun getAllDrafts(): Flow<List<Sheet>>
    suspend fun getDraftById(id: Long): Sheet?
    suspend fun saveDraft(quantities: Map<Int, String>, grandTotal: Long, totalPieces: Long, activeDenominations: Int): Long
    suspend fun updateDraft(id: Long, quantities: Map<Int, String>, grandTotal: Long, totalPieces: Long, activeDenominations: Int)
    suspend fun deleteDraft(id: Long)
    suspend fun restoreDrafts(drafts: List<Sheet>)
    suspend fun clearAllDrafts()
}
