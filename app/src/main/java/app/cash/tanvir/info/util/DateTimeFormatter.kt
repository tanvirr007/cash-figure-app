package app.cash.tanvir.info.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility to format timestamp into human-readable date and time.
 * For Bangla, it implements custom time-of-day labels:
 * - ভোর (early morning): 4 AM - 6 AM (4:00 to 5:59)
 * - সকাল (morning): 6 AM - 12 PM (6:00 to 11:59)
 * - দুপুর (noon/early afternoon): 12 PM - 3 PM (12:00 to 14:59)
 * - বিকাল (afternoon): 3 PM - 6 PM (15:00 to 17:59)
 * - সন্ধ্যা (evening): 6 PM - 8 PM (18:00 to 19:59)
 * - রাত্রি (night): 8 PM - 4 AM (20:00 to 3:59)
 */
object DateTimeFormatter {

    fun format(timestamp: Long, isBangla: Boolean): String {
        if (!isBangla) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
            return dateFormat.format(Date(timestamp))
        }

        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val label = when (hour) {
            in 4..5 -> "ভোর"
            in 6..11 -> "সকাল"
            in 12..14 -> "দুপুর"
            in 15..17 -> "বিকাল"
            in 18..19 -> "সন্ধ্যা"
            else -> "রাত্রি"
        }

        val datePartFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val timePartFormat = SimpleDateFormat("hh:mm", Locale.ENGLISH)
        val datePart = datePartFormat.format(Date(timestamp))
        val timePart = timePartFormat.format(Date(timestamp))

        var bnDatePart = BanglaDigitConverter.toBangla(datePart)
        bnDatePart = bnDatePart.replace("Jan", "জানুয়ারি")
            .replace("Feb", "ফেব্রুয়ারি")
            .replace("Mar", "মার্চ")
            .replace("Apr", "এপ্রিল")
            .replace("May", "মে")
            .replace("Jun", "জুন")
            .replace("Jul", "জুলাই")
            .replace("Aug", "আগস্ট")
            .replace("Sep", "সেপ্টেম্বর")
            .replace("Oct", "অক্টোবর")
            .replace("Nov", "নভেম্বর")
            .replace("Dec", "ডিসেম্বর")

        val bnTimePart = BanglaDigitConverter.toBangla(timePart)

        return "$bnDatePart, $label $bnTimePart"
    }
}
