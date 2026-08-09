package app.cash.tanvir.info.util.report

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.util.CurrencyFormatter
import app.cash.tanvir.info.util.NumberToWordsConverter
import java.io.ByteArrayOutputStream

/**
 * Generates PDF reports using Android's native [PdfDocument] framework.
 */
object PdfReportGenerator {

    fun generatePdf(context: Context? = null, sheet: Sheet, isBangla: Boolean = false): ByteArray {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page dimensions in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        context?.let { ctx ->
            try {
                val banglaTypeface = androidx.core.content.res.ResourcesCompat.getFont(ctx, app.cash.tanvir.info.R.font.tiro_bangla)
                    ?: Typeface.createFromAsset(ctx.assets, "fonts/tiro_bangla.ttf")
                paint.typeface = banglaTypeface
            } catch (_: Exception) {
                try {
                    val banglaTypeface = Typeface.createFromAsset(ctx.assets, "fonts/tiro_bangla.ttf")
                    paint.typeface = banglaTypeface
                } catch (_: Exception) {}
            }
        }

        var y = 50f

        // Title Header
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#00695C") // Deep Teal
        canvas.drawText(if (isBangla) "ক্যাশ রিপোর্ট" else "Cash Report", 40f, y, paint)

        y += 25f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        val dateStr = formatPdfDate(sheet.updatedAt, isBangla)
        canvas.drawText(if (isBangla) "তারিখ: $dateStr" else "Date: $dateStr", 40f, y, paint)

        y += 20f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 30f

        // Summary Card
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        val totalFormatted = CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = isBangla)
        canvas.drawText(if (isBangla) "সর্বমোট: $totalFormatted" else "Grand Total: $totalFormatted", 40f, y, paint)

        y += 20f
        paint.textSize = 11f
        paint.isFakeBoldText = false
        val words = if (isBangla) NumberToWordsConverter.toBangla(sheet.grandTotal) else NumberToWordsConverter.toEnglish(sheet.grandTotal)
        canvas.drawText(if (isBangla) "কথায়: $words" else "In Words: $words", 40f, y, paint)

        y += 20f
        canvas.drawText(
            if (isBangla) {
                "মোট নোট: ${app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(sheet.totalPieces)}  |  নোটের ধরণ: ${app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(sheet.activeDenominations)}"
            } else {
                "Total Pieces: ${sheet.totalPieces}  |  Active Denominations: ${sheet.activeDenominations}"
            },
            40f, y, paint
        )

        y += 30f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 25f

        // Breakdown Table Header
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText(if (isBangla) "নোটের মান" else "Denomination", 40f, y, paint)
        canvas.drawText(if (isBangla) "সংখ্যা" else "Quantity", 250f, y, paint)
        canvas.drawText(if (isBangla) "সাবটোটাল" else "Subtotal", 440f, y, paint)

        y += 10f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 20f

        // Breakdown Items (exclude 0 quantity)
        paint.textSize = 12f
        paint.isFakeBoldText = false

        val activeRows = sheet.rows.filter { it.quantity > 0 }
        if (activeRows.isEmpty()) {
            canvas.drawText(if (isBangla) "কোনো হিসাব নেই।" else "No cash counted.", 40f, y, paint)
        } else {
            activeRows.forEach { row ->
                val denomLabel = if (isBangla) row.denomination.labelBn else row.denomination.label
                val subtotalFormatted = CurrencyFormatter.format(row.total, useBengaliDigits = isBangla)
                val qtyStr = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(row.quantity) else row.quantity.toString()

                canvas.drawText(denomLabel, 40f, y, paint)
                canvas.drawText(qtyStr, 250f, y, paint)
                canvas.drawText(subtotalFormatted, 440f, y, paint)
                y += 22f
            }
        }

        y += 15f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 20f

        // Notes section
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText(if (isBangla) "নোট:" else "Notes:", 40f, y, paint)
        y += 18f
        paint.isFakeBoldText = false
        val notesText = if (sheet.remark.isNotBlank()) sheet.remark else "N/A"
        val notesTextLines = wrapText(notesText, paint, 515f)
        notesTextLines.forEach { line ->
            canvas.drawText(line, 40f, y, paint)
            y += 16f
        }
        y += 10f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 30f

        // Footer
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText(if (isBangla) "ক্যাশ ফিগার অ্যাপ দ্বারা প্রস্তুতকৃত" else "Generated by Cash Figure App", 40f, y, paint)

        pdfDocument.finishPage(page)

        val stream = ByteArrayOutputStream()
        pdfDocument.writeTo(stream)
        pdfDocument.close()
        return stream.toByteArray()
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine = testLine
                } else {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine)
                    }
                    currentLine = word
                }
            }
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }
        }
        return lines
    }

    private fun formatPdfDate(timestamp: Long, isBangla: Boolean): String {
        return app.cash.tanvir.info.util.DateTimeFormatter.format(timestamp, isBangla)
    }
}
