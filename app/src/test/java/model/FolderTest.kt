package model

import android.graphics.drawable.Drawable
import de.devmil.paperlaunch.R
import de.devmil.paperlaunch.model.Folder
import de.devmil.paperlaunch.model.IContextAccess
import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.storage.EntryDTO
import de.devmil.paperlaunch.storage.FolderDTO
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FolderTest {

    @Mock private lateinit var mockContextAccess : IContextAccess
    @Mock private lateinit var mockDrawable : Drawable

    @Mock private lateinit var mockEntry1 : IEntry
    @Mock private lateinit var mockEntry2 : IEntry
    @Mock private lateinit var mockEntry3 : IEntry

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

        return Folder(mockContextAccess, fDto, eDto, subEntries)
    }

    @Test fun creatingWorks() {
        createUUT(
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

        val name = folder.name
        Assert.assertEquals(name, "a folder")
    }

    @Test fun folderSummaryIconWorks() {
        `when`(mockContextAccess.getDrawable(R.mipmap.ic_folder_grey600_48dp, true))
                .thenReturn(mockDrawable)

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

        val drawable = folder.folderSummaryIcon

        Assert.assertSame(drawable, mockDrawable)
    }

    @Test fun isFolderWorks() {
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

        Assert.assertTrue(folder.isFolder)
    }

    @Test fun iconWorks() {
        `when`(mockContextAccess.getDrawable(R.mipmap.folder_frame, false))
                .thenReturn(mockDrawable)

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

        val icon = folder.icon

        Assert.assertSame(icon, mockDrawable)
    }

    @Test fun useIconColorWorks() {
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

        Assert.assertTrue(folder.useIconColor)
    }

    @Test fun subEntriesWorks() {
        val folder = createUUT(
                4711,
                18,
                4712,
                "a folder",
                null,
                0,
                -1,
                listOf(mockEntry1, mockEntry2, mockEntry3)
        )

        val subEntries = folder.subEntries!!

        Assert.assertEquals(3, subEntries.size)
        Assert.assertSame(mockEntry1, subEntries[0])
        Assert.assertSame(mockEntry2, subEntries[1])
        Assert.assertSame(mockEntry3, subEntries[2])
    }

}