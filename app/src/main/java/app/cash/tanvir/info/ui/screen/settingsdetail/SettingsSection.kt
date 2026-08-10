package app.cash.tanvir.info.ui.screen.settingsdetail

/**
 * The Settings sections that open as full-page detail views.
 */
enum class SettingsSection(val routeParam: String) {
    THEME("theme"),
    LANGUAGE("language"),
    CURRENCY("currency"),
    MISCELLANEOUS("misc");

    companion object {
        fun fromRouteParam(param: String?): SettingsSection =
            entries.firstOrNull { it.routeParam == param } ?: THEME
    }
}
