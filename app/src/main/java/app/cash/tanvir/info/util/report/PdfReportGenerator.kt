package app.cash.tanvir.info.util.report

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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

        val margin = 72f
        val rightMargin = 595f - margin // 523f
        var y = margin + 24f // Start below top margin: 72 + 24 = 96f

        // Title Header
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#00695C") // Deep Teal
        canvas.drawText(if (isBangla) "ক্যাশ রিপোর্ট" else "Cash Report", margin, y, paint)

        y += 25f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        val dateStr = formatPdfDate(sheet.updatedAt, isBangla)
        canvas.drawText(if (isBangla) "তারিখ: $dateStr" else "Date: $dateStr", margin, y, paint)

        y += 20f
        canvas.drawLine(margin, y, rightMargin, y, paint)
        y += 30f

        // Summary Card
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        val totalFormatted = CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = isBangla)
        canvas.drawText(if (isBangla) "সর্বমোট: $totalFormatted" else "Grand Total: $totalFormatted", margin, y, paint)

        y += 20f
        paint.textSize = 11f
        paint.isFakeBoldText = false
        val words = if (isBangla) NumberToWordsConverter.toBangla(sheet.grandTotal) else NumberToWordsConverter.toEnglish(sheet.grandTotal)
        canvas.drawText(if (isBangla) "কথায়: $words" else "In Words: $words", margin, y, paint)

        y += 20f
        canvas.drawText(
            if (isBangla) {
                "মোট নোট: ${app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(sheet.totalPieces)}  |  নোটের ধরণ: ${app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(sheet.activeDenominations)}"
            } else {
                "Total Pieces: ${sheet.totalPieces}  |  Active Denominations: ${sheet.activeDenominations}"
            },
            margin, y, paint
        )

        y += 30f
        canvas.drawLine(margin, y, rightMargin, y, paint)
        
        // Table Title
        y += 45f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#00695C") // Deep Teal
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(if (isBangla) "ক্যাশ ব্রেকডাউন" else "Cash Breakdown", (margin + rightMargin) / 2f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 28f

        // Breakdown Table Header
        val tableTop = y - 16f
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        
        // Draw top horizontal line
        canvas.drawLine(margin, tableTop, rightMargin, tableTop, paint)
        
        // Draw header text
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(if (isBangla) "নোটের মান" else "Denomination", (margin + 240f) / 2f, y, paint)
        canvas.drawText(if (isBangla) "সংখ্যা" else "Quantity", 300f, y, paint)
        canvas.drawText(if (isBangla) "সাবটোটাল" else "Subtotal", (360f + rightMargin) / 2f, y, paint)
        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = false

        y += 8f
        canvas.drawLine(margin, y, rightMargin, y, paint)

        val activeRows = sheet.rows.filter { it.quantity > 0 }
        if (activeRows.isEmpty()) {
            y += 18f
            canvas.drawText(if (isBangla) "কোনো হিসাব নেই।" else "No cash counted.", margin + 8f, y, paint)
            y += 8f
            canvas.drawLine(margin, y, rightMargin, y, paint)
            
            val tableBottom = y
            y += 24f
            
            // Draw vertical lines
            canvas.drawLine(margin, tableTop, margin, tableBottom, paint)
            canvas.drawLine(240f, tableTop, 240f, tableBottom, paint)
            canvas.drawLine(360f, tableTop, 360f, tableBottom, paint)
            canvas.drawLine(rightMargin, tableTop, rightMargin, tableBottom, paint)
        } else {
            y += 18f
            activeRows.forEach { row ->
                val denomLabel = if (isBangla) row.denomination.labelBn else row.denomination.label
                val subtotalFormatted = CurrencyFormatter.format(row.total, useBengaliDigits = isBangla)
                val qtyStr = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(row.quantity) else row.quantity.toString()

                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(denomLabel, margin + 8f, y, paint)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(qtyStr, 300f, y, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(subtotalFormatted, rightMargin, y, paint)
                paint.textAlign = Paint.Align.LEFT
                
                y += 8f
                canvas.drawLine(margin, y, rightMargin, y, paint)
                y += 18f
            }
            
            // Grand Total Row
            val totalFormatted = CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = isBangla)
            val totalLabel = if (isBangla) "সর্বমোট:" else "Grand Total:"
            canvas.drawText(totalLabel, margin, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(totalFormatted, rightMargin, y, paint)
            paint.textAlign = Paint.Align.LEFT

            val rowGap = paint.fontMetrics.descent + 4f
            y += rowGap
            canvas.drawLine(margin, y, rightMargin, y, paint)

            val tableBottom = y
            y += 24f

            // Draw vertical lines
            canvas.drawLine(margin, tableTop, margin, tableBottom, paint)
            canvas.drawLine(240f, tableTop, 240f, tableBottom - rowGap - 18f, paint)
            canvas.drawLine(360f, tableTop, 360f, tableBottom - rowGap - 18f, paint)
            canvas.drawLine(rightMargin, tableTop, rightMargin, tableBottom, paint)
        }

        // Notes section
        paint.textSize = 12f
        paint.color = Color.BLACK
        val label = if (isBangla) "নোট: " else "Notes: "
        paint.isFakeBoldText = true
        canvas.drawText(label, margin, y, paint)
        val labelWidth = paint.measureText(label)
        
        paint.isFakeBoldText = false
        val notesText = if (sheet.remark.isNotBlank()) {
            sheet.remark.replace("\n", " ").replace("\r", " ")
        } else {
            "N/A"
        }
        val notesTextLines = wrapText(notesText, paint, rightMargin - margin - labelWidth)
        if (notesTextLines.isNotEmpty()) {
            canvas.drawText(notesTextLines[0], margin + labelWidth, y, paint)
            y += 16f
            for (i in 1 until notesTextLines.size) {
                canvas.drawText(notesTextLines[i], margin, y, paint)
                y += 16f
            }
        } else {
            y += 16f
        }
        y += 10f
        canvas.drawLine(margin, y, rightMargin, y, paint)

        // Seal Image
        var sealBitmap: Bitmap? = null
        try {
            context?.let { ctx ->
                ctx.assets.open("seal.png").use { inputStream ->
                    sealBitmap = BitmapFactory.decodeStream(inputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (sealBitmap != null) {
            val processedBitmap = removeOuterWhiteBackground(sealBitmap!!)
            val originalWidth = processedBitmap.width.toFloat()
            val originalHeight = processedBitmap.height.toFloat()
            val aspectRatio = originalWidth / originalHeight
            val targetWidth = 150f
            val targetHeight = targetWidth / aspectRatio

            val sealLeft = (595f - targetWidth) / 2f
            val sealTop = maxOf(y + 30f, 842f - 72f - targetHeight)

            canvas.drawBitmap(processedBitmap, null, RectF(sealLeft, sealTop, sealLeft + targetWidth, sealTop + targetHeight), paint)
        }

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

    private fun removeOuterWhiteBackground(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val mutableBitmap = src.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(width * height)
        mutableBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val visited = java.util.BitSet(width * height)
        val queue = java.util.LinkedList<Int>()

        // Helper to check if pixel is white or close to white (RGB > 240)
        fun isWhite(color: Int): Boolean {
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            return r > 240 && g > 240 && b > 240
        }

        // Add 4 corners as seeds for flood fill
        val corners = intArrayOf(
            0,
            width - 1,
            (height - 1) * width,
            height * width - 1
        )
        for (corner in corners) {
            if (isWhite(pixels[corner])) {
                queue.add(corner)
                visited.set(corner)
            }
        }

        val dx = intArrayOf(-1, 1, 0, 0)
        val dy = intArrayOf(0, 0, -1, 1)

        while (!queue.isEmpty()) {
            val idx = queue.poll()!!
            val x = idx % width
            val y = idx / width

            // Make the outer background white pixel completely transparent (alpha = 0)
            pixels[idx] = pixels[idx] and 0x00FFFFFF

            for (i in 0 until 4) {
                val nx = x + dx[i]
                val ny = y + dy[i]
                if (nx in 0 until width && ny in 0 until height) {
                    val nIdx = ny * width + nx
                    if (!visited.get(nIdx) && isWhite(pixels[nIdx])) {
                        visited.set(nIdx)
                        queue.add(nIdx)
                    }
                }
            }
        }

        mutableBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return mutableBitmap
    }
}
