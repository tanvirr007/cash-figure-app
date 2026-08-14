package app.cash.tanvir.info.util

import org.junit.Assert.assertEquals
import org.junit.Test

class BanglaTextSanitizerTest {

    @Test
    fun testColonUsageVisargaBecomesColon() {
        assertEquals("মোট: ৫০", BanglaTextSanitizer.colonizeVisarga("মোটঃ ৫০"))
        assertEquals("কথা:", BanglaTextSanitizer.colonizeVisarga("কথাঃ"))
        assertEquals("তারিখ: ১২ জুলাই", BanglaTextSanitizer.colonizeVisarga("তারিখঃ ১২ জুলাই"))
        assertEquals("মোট:৫০", BanglaTextSanitizer.colonizeVisarga("মোটঃ৫০"))
        assertEquals("নাম:,পরিমাণ", BanglaTextSanitizer.colonizeVisarga("নামঃ,পরিমাণ"))
        assertEquals("মোট:", BanglaTextSanitizer.colonizeVisarga("মোটঃ"))
    }

    @Test
    fun testInWordVisargaIsPreserved() {
        assertEquals("দুঃখ", BanglaTextSanitizer.colonizeVisarga("দুঃখ"))
        assertEquals("দুঃখিত", BanglaTextSanitizer.colonizeVisarga("দুঃখিত"))
        assertEquals("নিঃশব্দ", BanglaTextSanitizer.colonizeVisarga("নিঃশব্দ"))
    }

    @Test
    fun testMixedTextKeepsRealWordsAndFixesColons() {
        assertEquals("দুঃখিত, মোট: ৫০", BanglaTextSanitizer.colonizeVisarga("দুঃখিত, মোটঃ ৫০"))
    }

    @Test
    fun testTextWithoutVisargaIsReturnedUnchanged() {
        assertEquals("মোট: ৫০", BanglaTextSanitizer.colonizeVisarga("মোট: ৫০"))
        assertEquals("", BanglaTextSanitizer.colonizeVisarga(""))
        assertEquals("plain text", BanglaTextSanitizer.colonizeVisarga("plain text"))
    }
}
