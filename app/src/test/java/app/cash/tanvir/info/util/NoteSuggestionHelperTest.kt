package app.cash.tanvir.info.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteSuggestionHelperTest {

    @Test
    fun `empty history returns empty suggestions`() {
        val result = NoteSuggestionHelper.getSuggestions(
            history = emptyList(),
            hidden = emptySet(),
            query = ""
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `empty query returns clean history preserving frequency order`() {
        val history = listOf("City Bank", "BRAC Bank", "Islami Bank", "Daily Sales")
        val result = NoteSuggestionHelper.getSuggestions(
            history = history,
            hidden = emptySet(),
            query = ""
        )
        assertEquals(listOf("City Bank", "BRAC Bank", "Islami Bank", "Daily Sales"), result)
    }

    @Test
    fun `hidden suggestions are excluded case-insensitively`() {
        val history = listOf("City Bank", "BRAC Bank", "Islami Bank", "Daily Sales")
        val hidden = setOf("city bank", "islami bank")
        val result = NoteSuggestionHelper.getSuggestions(
            history = history,
            hidden = hidden,
            query = ""
        )
        assertEquals(listOf("BRAC Bank", "Daily Sales"), result)
    }

    @Test
    fun `query filters history case-insensitively and prioritizes prefix matches`() {
        val history = listOf("Bank Asia", "City Bank", "BRAC Bank", "Daily Sales", "Salary Payment")
        // Query "bank"
        val result = NoteSuggestionHelper.getSuggestions(
            history = history,
            hidden = emptySet(),
            query = "bank"
        )
        // "Bank Asia" starts with "Bank", others contain "Bank"
        assertEquals(listOf("Bank Asia", "City Bank", "BRAC Bank"), result)
    }

    @Test
    fun `query matches bangla text strokes`() {
        val history = listOf("ব্র্যাক ব্যাংক", "ক্যাশ জমা", "সিটি ব্যাংক", "দৈনিক বিক্রি", "ক্যাশ উত্তোলন")
        val result = NoteSuggestionHelper.getSuggestions(
            history = history,
            hidden = emptySet(),
            query = "ক্যাশ"
        )
        assertEquals(listOf("ক্যাশ জমা", "ক্যাশ উত্তোলন"), result)
    }

    @Test
    fun `deduplication preserves first casing and order`() {
        val history = listOf("BRAC Bank", "brac bank", "BRAC BANK", "City Bank")
        val result = NoteSuggestionHelper.getSuggestions(
            history = history,
            hidden = emptySet(),
            query = ""
        )
        assertEquals(listOf("BRAC Bank", "City Bank"), result)
    }

    @Test
    fun `blank and whitespace-only entries are filtered out`() {
        val history = listOf("", "   ", "\n", "BRAC Bank", " \t ")
        val result = NoteSuggestionHelper.getSuggestions(
            history = history,
            hidden = emptySet(),
            query = ""
        )
        assertEquals(listOf("BRAC Bank"), result)
    }

    @Test
    fun `limit parameter is respected`() {
        val history = (1..20).map { "Note $it" }
        val result = NoteSuggestionHelper.getSuggestions(
            history = history,
            hidden = emptySet(),
            query = "",
            limit = 5
        )
        assertEquals(5, result.size)
        assertEquals(listOf("Note 1", "Note 2", "Note 3", "Note 4", "Note 5"), result)
    }
}
