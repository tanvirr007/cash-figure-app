package app.cash.tanvir.info.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a saved calculation sheet in the database.
 * Special ID = -1L represents the current active working sheet.
 */
@Entity(tableName = "sheets")
data class SheetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "",
    val grandTotal: Long = 0L,
    val totalPieces: Long = 0L,
    val activeDenominations: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val quantitiesJson: String = "{}" // JSON map of denominationValue -> quantity
)
