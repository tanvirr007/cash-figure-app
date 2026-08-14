package app.cash.tanvir.info.util.report

import app.cash.tanvir.info.domain.model.Denomination
import app.cash.tanvir.info.domain.model.DenominationRow
import app.cash.tanvir.info.domain.model.Sheet
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvReportGeneratorTest {

    @Test
    fun testGenerateCsv_EnglishNoBom() {
        val rows = listOf(
            DenominationRow(Denomination.ALL.first(), quantity = 100) // 1000 * 100 = 100,000
        )
        val sheet = Sheet(
            rows = rows,
            grandTotal = 100000L,
            totalPieces = 100L,
            activeDenominations = 1,
            updatedAt = 1786270304000L // Some specific timestamp
        )

        val csvBytes = CsvReportGenerator.generateCsv(sheet, isBangla = false)

        // Verify it does NOT start with the 3 UTF-8 BOM bytes
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val startsWithBom = csvBytes.size >= 3 &&
                csvBytes[0] == bom[0] &&
                csvBytes[1] == bom[1] &&
                csvBytes[2] == bom[2]
        org.junit.Assert.assertFalse(startsWithBom)

        // Verify the content contains BDT symbol
        val csvText = String(csvBytes, Charsets.UTF_8)
        assertTrue(csvText.contains("BDT"))
        assertTrue(csvText.contains("CASH REPORT"))
        assertTrue(csvText.contains("\"Grand Total\",,\"BDT 1,00,000\""))
    }

    @Test
    fun testGenerateCsv_BanglaPrependBom() {
        val rows = listOf(
            DenominationRow(Denomination.ALL.first(), quantity = 100)
        )
        val sheet = Sheet(
            rows = rows,
            grandTotal = 100000L,
            totalPieces = 100L,
            activeDenominations = 1,
            updatedAt = 1786270304000L,
            remark = "ব্যাংক হিসাবঃ ৫০০"
        )

        val csvBytes = CsvReportGenerator.generateCsv(sheet, isBangla = true)

        val expectedBom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        assertArrayEquals(expectedBom, csvBytes.take(3).toByteArray())

        val csvText = String(csvBytes.drop(3).toByteArray(), Charsets.UTF_8)
        assertTrue(csvText.contains("ক্যাশ রিপোর্ট"))
        assertTrue(csvText.contains("\"সর্বমোট\",,\"৳১,০০,০০০/-\""))
        assertTrue(csvText.contains("মন্তব্য,\"ব্যাংক হিসাব: ৫০০\""))
    }
}
