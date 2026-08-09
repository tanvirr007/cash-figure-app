package app.cash.tanvir.info.util.report

import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.util.CurrencyFormatter
import app.cash.tanvir.info.util.NumberToWordsConverter

object CsvReportGenerator {

    fun generateCsv(sheet: Sheet, isBangla: Boolean = false): ByteArray {
        val dateStr = app.cash.tanvir.info.util.DateTimeFormatter.format(sheet.updatedAt, isBangla)
        val words = if (isBangla) NumberToWordsConverter.toBangla(sheet.grandTotal) else NumberToWordsConverter.toEnglish(sheet.grandTotal)
        val totalPiecesStr = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(sheet.totalPieces) else sheet.totalPieces.toString()
        val activeDenomStr = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(sheet.activeDenominations) else sheet.activeDenominations.toString()

        val sb = StringBuilder()
        sb.append(if (isBangla) "ক্যাশ রিপোর্ট\n" else "CASH REPORT\n")
        sb.append(if (isBangla) "তারিখ,\"$dateStr\"\n" else "Date,\"$dateStr\"\n")
        sb.append(if (isBangla) "সর্বমোট,\"${CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = true)}\"\n" else "Grand Total,\"${CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = false)}\"\n")
        sb.append(if (isBangla) "কথায়,\"$words\"\n" else "Amount in Words,\"$words\"\n")
        sb.append(if (isBangla) "মোট নোট,$totalPiecesStr\n" else "Total Pieces,$totalPiecesStr\n")
        sb.append(if (isBangla) "নোটের ধরণ,$activeDenomStr\n" else "Active Denominations,$activeDenomStr\n")
        val escapedNotes = (if (sheet.remark.isNotBlank()) sheet.remark else "N/A").replace("\"", "\"\"")
        sb.append(if (isBangla) "নোট,\"$escapedNotes\"\n\n" else "Notes,\"$escapedNotes\"\n\n")

        sb.append(if (isBangla) "নোটের মান,সংখ্যা,সাবটোটাল\n" else "Denomination,Quantity,Subtotal\n")
        sheet.rows.filter { it.quantity > 0 }.forEach { row ->
            val denomLabel = if (isBangla) row.denomination.labelBn else row.denomination.label
            val qtyStr = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(row.quantity) else row.quantity.toString()
            val subtotal = CurrencyFormatter.format(row.total, useBengaliDigits = isBangla)
            sb.append("\"$denomLabel\",$qtyStr,\"$subtotal\"\n")
        }
        val totalLabel = if (isBangla) "সর্বমোট" else "Grand Total"
        val totalFormatted = CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = isBangla)
        sb.append("\"$totalLabel\",,\"$totalFormatted\"\n")

        val content = sb.toString().toByteArray(Charsets.UTF_8)
        // Prepend UTF-8 BOM so spreadsheet apps (Excel, etc.) correctly interpret non-ASCII characters (like Bangla text and the Taka symbol "৳")
        return if (isBangla) {
            val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
            bom + content
        } else {
            content
        }
    }
}
