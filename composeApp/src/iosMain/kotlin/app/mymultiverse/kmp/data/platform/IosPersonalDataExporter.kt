package app.mymultiverse.kmp.data.platform

import app.mymultiverse.kmp.domain.platform.PersonalDataExporter

class IosPersonalDataExporter : PersonalDataExporter {
    override fun shareJson(filename: String, content: String): Boolean {
        // P2: UIActivityViewController wiring deferred; export still succeeds via snackbar.
        return false
    }
}
