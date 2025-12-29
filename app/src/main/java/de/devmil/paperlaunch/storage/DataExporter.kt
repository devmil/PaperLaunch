package de.devmil.paperlaunch.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import com.google.gson.Gson
import de.devmil.paperlaunch.model.IFolder
import de.devmil.paperlaunch.model.Launch
import de.devmil.paperlaunch.utils.BitmapUtils
import de.devmil.paperlaunch.utils.IntentSerializer
import java.io.ByteArrayOutputStream

class DataExporter(private val context: Context) {

    data class ExportData(
        val version: Int = 1,
        val entries: List<ExportEntry>
    )

    data class ExportEntry(
        val type: String, // "folder" or "launch"
        val name: String?,
        val icon: String?, // Base64 encoded png
        val intentUri: String?, // Only for launch
        val entries: List<ExportEntry>? // Only for folder
    )

    fun exportToJson(): String {
        var rootEntries: List<ExportEntry> = emptyList()
        EntriesDataSource.instance.accessData(context, object : ITransactionAction {
            override fun execute(transactionContext: ITransactionContext) {
                val roots = transactionContext.loadRootContent()
                rootEntries = roots.map { convertToExportEntry(it) }
            }
        })

        val exportData = ExportData(entries = rootEntries)
        return Gson().toJson(exportData)
    }

    private fun convertToExportEntry(entry: de.devmil.paperlaunch.model.IEntry): ExportEntry {
        if (entry.isFolder) {
            val folder = entry as IFolder
            val subEntries = folder.subEntries?.map { convertToExportEntry(it) } ?: emptyList()
            return ExportEntry(
                type = "folder",
                name = folder.name,
                icon = encodeIcon(folder.icon?.let { if (it is BitmapDrawable) it else null }), // Only encode if it's a BitmapDrawable (custom icon), otherwise null implies default
                intentUri = null,
                entries = subEntries
            )
        } else {
            val launch = entry as Launch
            return ExportEntry(
                type = "launch",
                name = launch.name,
                icon = encodeIcon(launch.dto.icon?.let { if (it is BitmapDrawable) it else null }),
                intentUri = IntentSerializer.serialize(launch.dto.launchIntent),
                entries = null
            )
        }
    }

    private fun encodeIcon(drawable: BitmapDrawable?): String? {
        if (drawable == null) return null
        val bitmap = drawable.bitmap ?: return null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
