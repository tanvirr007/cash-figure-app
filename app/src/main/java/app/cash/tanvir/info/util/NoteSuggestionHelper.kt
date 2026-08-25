package app.cash.tanvir.info.util

/**
 * Pure utility helper for ranking, filtering, and managing note/remark suggestions
 * learned strictly from the user's past typed history.
 */
object NoteSuggestionHelper {

    /**
     * Filters and ranks note suggestions from the user's typed history.
     *
     * @param history List of distinct non-empty remarks from database, ordered by frequency and recency.
     * @param hidden Set of suggestions that the user has explicitly dismissed/removed.
     * @param query The current text typed by the user in the note input field.
     * @param limit Maximum number of suggestions to return (default: 15).
     * @return Ranked list of suggestions matching the user's keystrokes.
     */
    fun getSuggestions(
        history: List<String>,
        hidden: Set<String> = emptySet(),
        query: String = "",
        limit: Int = 15
    ): List<String> {
        if (history.isEmpty() || limit <= 0) return emptyList()

        val normalizedHidden = hidden.map { it.trim().lowercase() }.toSet()

        // Clean, sanitize, filter hidden, and deduplicate case-insensitively while preserving order
        val seen = mutableSetOf<String>()
        val cleanedHistory = mutableListOf<String>()

        for (item in history) {
            val sanitized = item.replace("\n", " ").replace("\r", " ").trim()
            if (sanitized.isNotBlank()) {
                val lower = sanitized.lowercase()
                if (lower !in normalizedHidden && seen.add(lower)) {
                    cleanedHistory.add(sanitized)
                }
            }
        }

        val trimmedQuery = query.trim().lowercase()
        if (trimmedQuery.isEmpty()) {
            return cleanedHistory.take(limit)
        }

        // Filter matching items
        val matching = cleanedHistory.filter { it.lowercase().contains(trimmedQuery) }

        // Rank prefix matches first, followed by substring matches, maintaining frequency order
        val prefixMatches = matching.filter { it.trim().lowercase().startsWith(trimmedQuery) }
        val otherMatches = matching.filter { !it.trim().lowercase().startsWith(trimmedQuery) }

        return (prefixMatches + otherMatches).take(limit)
    }
}
