package app.cash.tanvir.info.data.repository

import app.cash.tanvir.info.data.local.db.dao.DraftDao
import app.cash.tanvir.info.data.local.db.dao.SheetDao
import app.cash.tanvir.info.data.local.db.entity.DraftEntity
import app.cash.tanvir.info.data.local.db.entity.SheetEntity
import app.cash.tanvir.info.domain.model.Denomination
import app.cash.tanvir.info.domain.model.DenominationRow
import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.domain.repository.SheetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [SheetRepository] using Room database.
 */
@Singleton
class SheetRepositoryImpl @Inject constructor(
    private val sheetDao: SheetDao,
    private val draftDao: DraftDao
) : SheetRepository {

    override fun getAllSheets(): Flow<List<Sheet>> {
        return sheetDao.getAllSheets().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun searchSheets(query: String): Flow<List<Sheet>> {
        return sheetDao.searchSheets(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCurrentSheet(): Flow<Sheet?> {
        return sheetDao.getCurrentSheet().map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun getSheetById(id: Long): Sheet? {
        return sheetDao.getSheetById(id)?.toDomainModel()
    }

    override suspend fun saveCurrentSheet(
        quantities: Map<Int, String>,
        grandTotal: Long,
        totalPieces: Long,
        activeDenominations: Int
    ) {
        val jsonObj = JSONObject()
        quantities.forEach { (value, qty) ->
            jsonObj.put(value.toString(), qty)
        }

        val entity = SheetEntity(
            id = -1L,
            name = "Current Working Sheet",
            grandTotal = grandTotal,
            totalPieces = totalPieces,
            activeDenominations = activeDenominations,
            updatedAt = System.currentTimeMillis(),
            quantitiesJson = jsonObj.toString()
        )

        sheetDao.insertSheet(entity)
    }

    override suspend fun saveSheet(sheet: Sheet): Long {
        val entity = sheet.toEntity()
        val id = sheetDao.insertSheet(entity)
        if (sheet.name.isBlank()) {
            val sheetNumber = sheetDao.getHistorySheetCount()
            val updatedEntity = entity.copy(id = id, name = "Sheet #$sheetNumber")
            sheetDao.updateSheet(updatedEntity)
        }
        return id
    }

    override suspend fun saveSheetAndResetCurrent(sheet: Sheet): Long {
        val entity = sheet.toEntity()
        val jsonObj = org.json.JSONObject()
        val currentEntity = SheetEntity(
            id = -1L,
            name = "Current Working Sheet",
            grandTotal = 0L,
            totalPieces = 0L,
            activeDenominations = 0,
            updatedAt = System.currentTimeMillis(),
            quantitiesJson = jsonObj.toString()
        )
        return sheetDao.saveSheetAndResetCurrent(entity, currentEntity)
    }

    override suspend fun updateSheet(sheet: Sheet) {
        sheetDao.updateSheet(sheet.toEntity())
    }

    override suspend fun softDeleteSheet(id: Long) {
        sheetDao.softDeleteSheet(id)
    }

    override suspend fun restoreSheet(id: Long) {
        sheetDao.restoreSheet(id)
    }

    override suspend fun hardDeleteSheet(id: Long) {
        sheetDao.hardDeleteSheet(id)
    }

    override suspend fun clearAllHistory() {
        sheetDao.clearAllHistory()
    }

    override suspend fun restoreSheets(sheets: List<Sheet>) {
        sheetDao.clearAllHistory()
        sheetDao.resetAutoIncrement()

        val sortedSheets = sheets.sortedBy { it.createdAt }
        val defaultNameRegex = Regex("^(Sheet #\\d+|Saved Sheet|Restored Sheet)$", RegexOption.IGNORE_CASE)

        sortedSheets.forEachIndexed { index, sheet ->
            val serialNumber = index + 1
            val assignedName = if (sheet.name.isBlank() || defaultNameRegex.matches(sheet.name.trim())) {
                "Sheet #$serialNumber"
            } else {
                sheet.name
            }
            val entityToInsert = sheet.toEntity().copy(
                id = 0L,
                name = assignedName
            )
            sheetDao.insertSheet(entityToInsert)
        }
    }

    override fun getAllDrafts(): Flow<List<Sheet>> {
        return draftDao.getAllDrafts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getDraftById(id: Long): Sheet? {
        return draftDao.getDraftById(id)?.toDomainModel()
    }

    override suspend fun saveDraft(
        quantities: Map<Int, String>,
        grandTotal: Long,
        totalPieces: Long,
        activeDenominations: Int
    ): Long {
        val now = System.currentTimeMillis()
        val draftNumber = draftDao.getDraftCount() + 1
        val entity = DraftEntity(
            name = "Draft #$draftNumber",
            grandTotal = grandTotal,
            totalPieces = totalPieces,
            activeDenominations = activeDenominations,
            createdAt = now,
            updatedAt = now,
            quantitiesJson = quantities.toQuantitiesJson()
        )
        return draftDao.insertDraft(entity)
    }

    override suspend fun updateDraft(
        id: Long,
        quantities: Map<Int, String>,
        grandTotal: Long,
        totalPieces: Long,
        activeDenominations: Int
    ) {
        val existing = draftDao.getDraftById(id) ?: return
        val entity = DraftEntity(
            id = existing.id,
            name = existing.name,
            grandTotal = grandTotal,
            totalPieces = totalPieces,
            activeDenominations = activeDenominations,
            createdAt = existing.createdAt,
            updatedAt = System.currentTimeMillis(),
            quantitiesJson = quantities.toQuantitiesJson()
        )
        draftDao.insertDraft(entity)
    }

    override suspend fun deleteDraft(id: Long) {
        draftDao.deleteDraft(id)
    }

    override suspend fun restoreDrafts(drafts: List<Sheet>) {
        draftDao.clearAllDrafts()
        val sortedDrafts = drafts.sortedBy { it.createdAt }
        sortedDrafts.forEach { draft ->
            draftDao.insertDraft(
                DraftEntity(
                    name = if (draft.name.isBlank()) "Draft" else draft.name,
                    grandTotal = draft.grandTotal,
                    totalPieces = draft.totalPieces,
                    activeDenominations = draft.activeDenominations,
                    createdAt = if (draft.createdAt > 0) draft.createdAt else System.currentTimeMillis(),
                    updatedAt = if (draft.updatedAt > 0) draft.updatedAt else System.currentTimeMillis(),
                    quantitiesJson = draft.toQuantitiesJson()
                )
            )
        }
    }

    override suspend fun clearAllDrafts() {
        draftDao.clearAllDrafts()
    }

    // ── Mapping extension functions ──

    private fun SheetEntity.toDomainModel(): Sheet {
        val quantitiesMap = this.quantitiesJson.toQuantitiesMap()

        val rows = Denomination.ALL.map { denom ->
            val qty = quantitiesMap[denom.value] ?: 0L
            DenominationRow(
                denomination = denom,
                quantity = qty,
                total = denom.value.toLong() * qty
            )
        }

        val displayName = if (this.name.isNotBlank()) this.name else "Sheet #1"

        return Sheet(
            id = this.id,
            name = displayName,
            rows = rows,
            grandTotal = this.grandTotal,
            totalPieces = this.totalPieces,
            activeDenominations = this.activeDenominations,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            remark = this.remark
        )
    }

    private fun DraftEntity.toDomainModel(): Sheet {
        val quantitiesMap = this.quantitiesJson.toQuantitiesMap()

        val rows = Denomination.ALL.map { denom ->
            val qty = quantitiesMap[denom.value] ?: 0L
            DenominationRow(
                denomination = denom,
                quantity = qty,
                total = denom.value.toLong() * qty
            )
        }

        return Sheet(
            id = this.id,
            name = if (this.name.isNotBlank()) this.name else "Draft",
            rows = rows,
            grandTotal = this.grandTotal,
            totalPieces = this.totalPieces,
            activeDenominations = this.activeDenominations,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            remark = ""
        )
    }

    private fun Sheet.toEntity(): SheetEntity {
        return SheetEntity(
            id = this.id,
            name = this.name,
            grandTotal = this.grandTotal,
            totalPieces = this.totalPieces,
            activeDenominations = this.activeDenominations,
            createdAt = if (this.createdAt > 0) this.createdAt else System.currentTimeMillis(),
            updatedAt = if (this.updatedAt > 0) this.updatedAt else System.currentTimeMillis(),
            quantitiesJson = this.toQuantitiesJson(),
            remark = this.remark
        )
    }

    private fun String.toQuantitiesMap(): Map<Int, Long> {
        val quantitiesMap = mutableMapOf<Int, Long>()
        try {
            val jsonObj = JSONObject(this)
            jsonObj.keys().forEach { key ->
                val denomValue = key.toIntOrNull()
                if (denomValue != null) {
                    val qtyStr = jsonObj.optString(key, "0")
                    val qty = qtyStr.toLongOrNull() ?: 0L
                    quantitiesMap[denomValue] = qty
                }
            }
        } catch (_: Exception) {}
        return quantitiesMap
    }

    private fun Sheet.toQuantitiesJson(): String {
        val jsonObj = JSONObject()
        this.rows.forEach { row ->
            jsonObj.put(row.denomination.value.toString(), row.quantity.toString())
        }
        return jsonObj.toString()
    }

    private fun Map<Int, String>.toQuantitiesJson(): String {
        val jsonObj = JSONObject()
        this.forEach { (value, qty) ->
            jsonObj.put(value.toString(), qty)
        }
        return jsonObj.toString()
    }
}
