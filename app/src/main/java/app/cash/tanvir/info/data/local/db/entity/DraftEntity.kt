package app.cash.tanvir.info.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a saved draft (unsaved working count snapshot).
 * Drafts are independent from history sheets and from the current working
 * sheet (id = -1); each entry is created via "Save as Draft".
 */
@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "",
    val grandTotal: Long = 0L,
    val totalPieces: Long = 0L,
    val activeDenominations: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val quantitiesJson: String = "{}" // JSON map of denominationValue -> quantity
)
