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
            Denomination(1000, "BDT 1000", "৳১০০০"),
            Denomination(500, "BDT 500", "৳৫০০"),
            Denomination(200, "BDT 200", "৳২০০"),
            Denomination(100, "BDT 100", "৳১০০"),
            Denomination(50, "BDT 50", "৳৫০"),
            Denomination(20, "BDT 20", "৳২০"),
            Denomination(10, "BDT 10", "৳১০"),
            Denomination(5, "BDT 5", "৳৫"),
            Denomination(2, "BDT 2", "৳২"),
            Denomination(1, "BDT 1", "৳১")
        )
    }
}
