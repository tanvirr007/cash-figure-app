package app.cash.tanvir.info.ui.animation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppMotionTest {

    @Test
    fun `tabIndex identifies bottom navigation tab routes`() {
        assertEquals(0, tabIndex("calculator"))
        assertEquals(0, tabIndex("calculator?loadDraftId=-1"))
        assertEquals(0, tabIndex("calculator?loadDraftId=42"))
        assertEquals(1, tabIndex("history"))
        assertEquals(2, tabIndex("settings"))
    }

    @Test
    fun `tabIndex returns -1 for sub-screens and unknown routes`() {
        assertEquals(-1, tabIndex("report/1?fromSave=true"))
        assertEquals(-1, tabIndex("draft"))
        assertEquals(-1, tabIndex("about"))
        assertEquals(-1, tabIndex("changelog"))
        assertEquals(-1, tabIndex("update"))
        assertEquals(-1, tabIndex("settings-detail?section=appearance"))
        assertEquals(-1, tabIndex(null))
        assertEquals(-1, tabIndex(""))
    }
}
