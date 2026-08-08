package app.cash.tanvir.info.domain.model

/**
 * A complete calculation sheet containing all denomination rows
 * and computed summary values.
 */
data class Sheet(
    val id: Long = 0L,
    val name: String = "",
    val rows: List<DenominationRow> = Denomination.ALL.map { DenominationRow(it) },
    val grandTotal: Long = rows.sumOf { it.total },
    val totalPieces: Long = rows.sumOf { it.quantity },
    val activeDenominations: Int = rows.count { it.quantity > 0 },
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
