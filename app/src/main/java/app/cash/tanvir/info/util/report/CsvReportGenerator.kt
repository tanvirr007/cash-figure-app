package app.cash.tanvir.info.util.report

import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.util.CurrencyFormatter
import app.cash.tanvir.info.util.NumberToWordsConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvReportGenerator {

    fun generateCsv(sheet: Sheet, isBangla: Boolean = false): ByteArray {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateStr = dateFormat.format(Date(sheet.updatedAt))
        val words = if (isBangla) NumberToWordsConverter.toBangla(sheet.grandTotal) else NumberToWordsConverter.toEnglish(sheet.grandTotal)

        val sb = StringBuilder()
        sb.append("Cash Figure Report\n")
        sb.append("Date,\"$dateStr\"\n")
        sb.append("Grand Total,\"${CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = isBangla)}\"\n")
        sb.append("Amount in Words,\"$words\"\n")
        sb.append("Total Pieces,${sheet.totalPieces}\n")
        sb.append("Active Denominations,${sheet.activeDenominations}\n\n")

        sb.append("Denomination,Quantity,Subtotal\n")
        sheet.rows.filter { it.quantity > 0 }.forEach { row ->
            val denomLabel = if (isBangla) row.denomination.labelBn else row.denomination.label
            val subtotal = CurrencyFormatter.format(row.total, useBengaliDigits = isBangla)
            sb.append("\"$denomLabel\",${row.quantity},\"$subtotal\"\n")
        }

        return sb.toString().toByteArray(Charsets.UTF_8)
    }
}
