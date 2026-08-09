package app.cash.tanvir.info.util.report

import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.util.CurrencyFormatter
import app.cash.tanvir.info.util.NumberToWordsConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvReportGenerator {

    fun generateCsv(sheet: Sheet, isBangla: Boolean = false): ByteArray {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val rawDate = dateFormat.format(Date(sheet.updatedAt))
        val dateStr = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(rawDate) else rawDate
        val words = if (isBangla) NumberToWordsConverter.toBangla(sheet.grandTotal) else NumberToWordsConverter.toEnglish(sheet.grandTotal)
        val totalPiecesStr = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(sheet.totalPieces) else sheet.totalPieces.toString()
        val activeDenomStr = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(sheet.activeDenominations) else sheet.activeDenominations.toString()

        val sb = StringBuilder()
        sb.append(if (isBangla) "ক্যাশ ফিগার রিপোর্ট\n" else "Cash Figure Report\n")
        sb.append(if (isBangla) "তারিখ,\"$dateStr\"\n" else "Date,\"$dateStr\"\n")
        sb.append(if (isBangla) "সর্বমোট,\"${CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = true)}\"\n" else "Grand Total,\"${CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = false)}\"\n")
        sb.append(if (isBangla) "কথায়,\"$words\"\n" else "Amount in Words,\"$words\"\n")
        sb.append(if (isBangla) "মোট নোট,$totalPiecesStr\n" else "Total Pieces,$totalPiecesStr\n")
        sb.append(if (isBangla) "নোটের ধরণ,$activeDenomStr\n\n" else "Active Denominations,$activeDenomStr\n\n")

        sb.append(if (isBangla) "নোটের মান,সংখ্যা,সাবটোটাল\n" else "Denomination,Quantity,Subtotal\n")
        sheet.rows.filter { it.quantity > 0 }.forEach { row ->
            val denomLabel = if (isBangla) row.denomination.labelBn else row.denomination.label
            val qtyStr = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(row.quantity) else row.quantity.toString()
            val subtotal = CurrencyFormatter.format(row.total, useBengaliDigits = isBangla)
            sb.append("\"$denomLabel\",$qtyStr,\"$subtotal\"\n")
        }

        val content = sb.toString().toByteArray(Charsets.UTF_8)
        // Prepend UTF-8 BOM so spreadsheet apps (Excel, etc.) correctly interpret Bangla text
        return if (isBangla) {
            val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
            bom + content
        } else {
            content
        }
    }
}
