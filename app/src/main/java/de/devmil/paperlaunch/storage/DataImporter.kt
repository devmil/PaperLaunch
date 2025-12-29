package de.devmil.paperlaunch.storage

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import com.google.gson.Gson
import de.devmil.paperlaunch.utils.IntentSerializer
import java.lang.Exception

class DataImporter(private val context: Context) {

    data class ExportData(
        val version: Int = 1,
        val entries: List<ExportEntry>
    )

    data class ExportEntry(
        val type: String,
        val name: String?,
        val icon: String?,
        val intentUri: String?,
        val entries: List<ExportEntry>?
    )

    fun importFromJson(json: String) {
        val data = Gson().fromJson(json, ExportData::class.java)

        EntriesDataSource.instance.accessData(context, object : ITransactionAction {
            override fun execute(transactionContext: ITransactionContext) {
                // Clear existing data
                transactionContext.clear()

                // Restore data
                data.entries.forEachIndexed { index, entry ->
                    restoreEntry(transactionContext, -1, entry, index, 0)
                }
            }
        })
    }

    private fun restoreEntry(
        transactionContext: ITransactionContext,
        parentFolderId: Long,
        entry: ExportEntry,
        orderIndex: Int,
        depth: Int
    ) {
        if (entry.type == "folder") {
            val folder = transactionContext.createFolder(parentFolderId, orderIndex, depth)
            folder.dto.name = entry.name
            if (entry.icon != null) {
                folder.dto.icon = decodeIcon(entry.icon)
            }
            transactionContext.updateFolderData(folder)

            entry.entries?.forEachIndexed { index, childEntry ->
                restoreEntry(transactionContext, folder.id, childEntry, index, depth + 1)
            }
        } else if (entry.type == "launch") {
            val launch = transactionContext.createLaunch(parentFolderId, orderIndex)
            launch.dto.name = entry.name
            launch.dto.launchIntent = entry.intentUri?.let { IntentSerializer.deserialize(it) }
            if (entry.icon != null) {
                launch.dto.icon = decodeIcon(entry.icon)
            }
            transactionContext.updateLaunchData(launch)
        }
    }

    private fun decodeIcon(base64: String): BitmapDrawable? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            BitmapDrawable(context.resources, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
