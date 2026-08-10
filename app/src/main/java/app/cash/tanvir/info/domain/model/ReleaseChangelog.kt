package app.cash.tanvir.info.domain.model

/**
 * One GitHub release's changelog, mirrored from changelog.json (raw GitHub CDN).
 * Pure Kotlin — no Android dependencies. Unit-testable.
 */
data class ReleaseChangelog(
    val tagName: String,
    val publishedAt: Long,
    val items: List<ChangelogItem>
)

/**
 * One main commit entry within a release changelog.
 * @param title   commit title with trailing `(hash)` stripped
 * @param subItems commit body bullets (indented `-` lines)
 */
data class ChangelogItem(
    val title: String,
    val subItems: List<String>
)
