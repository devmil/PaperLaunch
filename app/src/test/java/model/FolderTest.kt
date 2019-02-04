package model

import android.graphics.drawable.Drawable
import android.test.InstrumentationTestCase
import de.devmil.paperlaunch.model.Folder
import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.storage.EntryDTO
import de.devmil.paperlaunch.storage.FolderDTO
import org.junit.Assert
import org.junit.Test

class FolderTest : InstrumentationTestCase() {


    private fun createUUT(
            entryId: Long,
            entryOrderIndex: Long,
            folderId: Long,
            name: String,
            icon: Drawable?,
            depth: Int,
            parentFolderId: Long,
            subEntries : List<IEntry>?): Folder {
        val fDto = FolderDTO(folderId, name, icon, depth)
        val eDto = EntryDTO(entryId, entryOrderIndex, 0, folderId, parentFolderId)

        return Folder(fDto, eDto, subEntries)
    }

    @Test fun creatingWorks() {
        val folder = createUUT(
                0,
                0,
                1,
                "folder",
                null,
                0,
                -1,
                null
        )
    }

    @Test fun idWorks() {
        val folder = createUUT(
                4711,
                0,
                4712,
                "folder",
                null,
                0,
                -1,
                null
        )

        Assert.assertEquals(folder.id, 4712L)
    }

    @Test fun entryIdWorks() {
        val folder = createUUT(
                4711,
                0,
                4712,
                "folder",
                null,
                0,
                -1,
                null
        )

        Assert.assertEquals(folder.entryId, 4711L)
    }

    @Test fun orderIndexWorks() {
        val folder = createUUT(
                4711,
                18,
                4712,
                "folder",
                null,
                0,
                -1,
                null
        )

        Assert.assertEquals(folder.orderIndex, 18)
    }

    @Test fun nameWorks() {
        val folder = createUUT(
                4711,
                18,
                4712,
                "a folder",
                null,
                0,
                -1,
                null
        )

/*        val name = folder.getName(instrumentation.context);
        Assert.assertEquals(name, "a folder")*/
    }
}