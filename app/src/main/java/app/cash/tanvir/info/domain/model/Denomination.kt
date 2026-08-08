package app.cash.tanvir.info.domain.model

/**
 * Represents a Bangladeshi currency denomination.
 * All 10 denominations from ৳1000 down to ৳1.
 */
data class Denomination(
    val value: Int,
    val label: String,
    val labelBn: String
) {
    companion object {
        val ALL = listOf(
            Denomination(1000, "৳1000", "৳১০০০"),
            Denomination(500, "৳500", "৳৫০০"),
            Denomination(200, "৳200", "৳২০০"),
            Denomination(100, "৳100", "৳১০০"),
            Denomination(50, "৳50", "৳৫০"),
            Denomination(20, "৳20", "৳২০"),
            Denomination(10, "৳10", "৳১০"),
            Denomination(5, "৳5", "৳৫"),
            Denomination(2, "৳2", "৳২"),
            Denomination(1, "৳1", "৳১")
        )
    }
}
