package app.cash.tanvir.info.util

import org.junit.Assert.assertEquals
import org.junit.Test

class BanglaTextSanitizerTest {

    @Test
    fun testBanglaColonBecomesVisarga() {
        assertEquals("মোটঃ ৫০", BanglaTextSanitizer.colonToVisarga("মোট: ৫০", isBangla = true))
        assertEquals("কথাঃ", BanglaTextSanitizer.colonToVisarga("কথা:", isBangla = true))
        assertEquals("তারিখঃ ১২ জুলাই", BanglaTextSanitizer.colonToVisarga("তারিখ: ১২ জুলাই", isBangla = true))
        assertEquals("মোটঃ৫০", BanglaTextSanitizer.colonToVisarga("মোট:৫০", isBangla = true))
        assertEquals("নামঃ,পরিমাণ", BanglaTextSanitizer.colonToVisarga("নাম:,পরিমাণ", isBangla = true))
        assertEquals("মোটঃ", BanglaTextSanitizer.colonToVisarga("মোট:", isBangla = true))
        assertEquals("দুঃখিত, মোটঃ ৫০", BanglaTextSanitizer.colonToVisarga("দুঃখিত, মোট: ৫০", isBangla = true))
    }

    @Test
    fun testSpaceBeforeColonStillConverts() {
        assertEquals("মোটঃ ৫০", BanglaTextSanitizer.colonToVisarga("মোট : ৫০", isBangla = true))
    }

    @Test
    fun testTimeSeparatorsArePreserved() {
        assertEquals("রাত ১১:৩০", BanglaTextSanitizer.colonToVisarga("রাত ১১:৩০", isBangla = true))
        assertEquals("দুপুর ১০:৪৫, ১২ জুলাই", BanglaTextSanitizer.colonToVisarga("দুপুর ১০:৪৫, ১২ জুলাই", isBangla = true))
        assertEquals("10:45", BanglaTextSanitizer.colonToVisarga("10:45", isBangla = true))
        assertEquals("৫০০: ৩০", BanglaTextSanitizer.colonToVisarga("৫০০: ৩০", isBangla = true))
    }

    @Test
    fun testEnglishColonsAreKeptInBanglaMode() {
        assertEquals("Bank: Sonali", BanglaTextSanitizer.colonToVisarga("Bank: Sonali", isBangla = true))
        assertEquals("500: টাকা", BanglaTextSanitizer.colonToVisarga("500: টাকা", isBangla = true))
    }

    @Test
    fun testBengaliDigitBeforeColonConvertsWhenNotTime() {
        assertEquals("৫০০ঃ মোট", BanglaTextSanitizer.colonToVisarga("৫০০: মোট", isBangla = true))
    }

    @Test
    fun testInWordVisargaIsPreserved() {
        assertEquals("দুঃখ", BanglaTextSanitizer.colonToVisarga("দুঃখ", isBangla = true))
        assertEquals("দুঃখিত", BanglaTextSanitizer.colonToVisarga("দুঃখিত", isBangla = true))
        assertEquals("নিঃশব্দ", BanglaTextSanitizer.colonToVisarga("নিঃশব্দ", isBangla = true))
    }

    @Test
    fun testEnglishModeIsNoOp() {
        assertEquals("মোট: ৫০", BanglaTextSanitizer.colonToVisarga("মোট: ৫০", isBangla = false))
        assertEquals("মোটঃ ৫০", BanglaTextSanitizer.colonToVisarga("মোটঃ ৫০", isBangla = false))
        assertEquals("Bank: Sonali", BanglaTextSanitizer.colonToVisarga("Bank: Sonali", isBangla = false))
        assertEquals("রাত ১১:৩০", BanglaTextSanitizer.colonToVisarga("রাত ১১:৩০", isBangla = false))
    }

    @Test
    fun testTextWithoutColonIsReturnedUnchanged() {
        assertEquals("", BanglaTextSanitizer.colonToVisarga("", isBangla = true))
        assertEquals("plain text", BanglaTextSanitizer.colonToVisarga("plain text", isBangla = true))
        assertEquals("মোট ৫০", BanglaTextSanitizer.colonToVisarga("মোট ৫০", isBangla = true))
    }
}
