package app.mymultiverse.kmp.data.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import app.mymultiverse.kmp.domain.platform.PersonalDataExporter

class AndroidPersonalDataExporter(
    private val context: Context,
) : PersonalDataExporter {
    override fun shareJson(filename: String, content: String): Boolean =
        runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, filename)
                putExtra(Intent.EXTRA_TEXT, content)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, filename).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        }.getOrElse {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(filename, content))
            true
        }
}
