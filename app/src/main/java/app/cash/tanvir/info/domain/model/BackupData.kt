package app.cash.tanvir.info.domain.model

/**
 * Data class representing a versioned JSON backup file structure for export and restore.
 */
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val sheets: List<Sheet> = emptyList()
)
