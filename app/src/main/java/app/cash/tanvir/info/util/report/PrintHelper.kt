package app.cash.tanvir.info.util.report

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import app.cash.tanvir.info.domain.model.Sheet
import java.io.FileOutputStream

/**
 * Native Android [PrintManager] integration.
 * Prints calculation reports directly using native Android print framework.
 */
object PrintHelper {

    fun printSheet(context: Context, sheet: Sheet, isBangla: Boolean = false) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "CashFigure_Report_${sheet.id}"

        printManager.print(
            jobName,
            object : PrintDocumentAdapter() {
                private var pdfData: ByteArray? = null

                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }

                    pdfData = PdfReportGenerator.generatePdf(context, sheet, isBangla)

                    val info = PrintDocumentInfo.Builder("CashFigure_Report.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()

                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    val data = pdfData ?: return
                    try {
                        FileOutputStream(destination?.fileDescriptor).use { os ->
                            os.write(data)
                            os.flush()
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            },
            null
        )
    }
}
