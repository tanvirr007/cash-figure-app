package app.cash.tanvir.info.domain.model

/**
 * Represents one row in the calculator:
 * a denomination, how many of it, and the row total.
 */
data class DenominationRow(
    val denomination: Denomination,
    val quantity: Long = 0L,
    val total: Long = denomination.value * quantity
)
