package app.cash.tanvir.info.domain.model

/**
 * Data class representing a versioned JSON backup file structure for export and restore.
 */
data class BackupSettings(
    val theme: String = "SYSTEM",
    val language: String = "ENGLISH",
    val disabledDenominations: Set<Int> = emptySet()
)

/**
 * Data class representing a versioned JSON backup file structure for export and restore.
 */
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val settings: BackupSettings? = null,
    val sheets: List<Sheet> = emptyList()
)
