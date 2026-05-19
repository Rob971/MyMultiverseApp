package app.mymultiverse.kmp.i18n

internal object LocaleTestFiles {
    fun readStringKeys(xml: String): Set<String> {
        val regex = Regex("""<string\s+name="([^"]+)"""")
        return regex.findAll(xml).map { it.groupValues[1] }.toSet()
    }

    fun readStringValue(xml: String, key: String): String {
        val regex = Regex("""<string\s+name="$key">([\s\S]*?)</string>""")
        return regex.find(xml)?.groupValues?.get(1)?.trim().orEmpty()
    }

    fun stringsFile(localeDir: String): java.io.File =
        locateProjectRoot().resolve("composeApp/src/commonMain/composeResources/$localeDir/strings.xml")

    private fun locateProjectRoot(): java.io.File {
        var current = java.io.File(checkNotNull(System.getProperty("user.dir")))
        for (depth in 0..6) {
            if (current.resolve("composeApp").isDirectory) return current
            current = current.parentFile ?: break
        }
        error("Could not locate project root from user.dir=${System.getProperty("user.dir")}")
    }
}
