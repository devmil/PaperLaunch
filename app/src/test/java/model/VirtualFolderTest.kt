package model

import android.graphics.drawable.Drawable
import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.model.VirtualFolder
import junit.framework.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class VirtualFolderTest {

    @Mock
    private lateinit var mockDrawable : Drawable

    @Mock
    private lateinit var mockEntry1 : IEntry
    @Mock
    private lateinit var mockEntry2 : IEntry
    @Mock
    private lateinit var mockEntry3 : IEntry

    private fun createUUT(name: String, icon: Drawable, subEntries: List<IEntry>) : VirtualFolder {
        return VirtualFolder(
                name,
                icon,
                subEntries
        )
    }

    @Test
    fun createTest() {
        createUUT(
                "Virtual folder",
                mockDrawable,
                listOf(mockEntry1, mockEntry2, mockEntry3)
        )
    }

    @Test
    fun subEntriesWork() {
        val vFolder = createUUT(
                "Virtual folder",
                mockDrawable,
                listOf(mockEntry1, mockEntry2, mockEntry3)
        )

        assertEquals(3, vFolder.subEntries!!.size)
        assertSame(mockEntry1, vFolder.subEntries!![0])
        assertSame(mockEntry2, vFolder.subEntries!![1])
        assertSame(mockEntry3, vFolder.subEntries!![2])
    }

    @Test
    fun idIsInvalidWorks() {
        val vFolder = createUUT(
                "Virtual folder",
                mockDrawable,
                listOf(mockEntry1, mockEntry2, mockEntry3)
        )

        assertEquals(-1, vFolder.id)
    }

    @Test
    fun entryIdIsInvalidWorks() {
        val vFolder = createUUT(
                "Virtual folder",
                mockDrawable,
                listOf(mockEntry1, mockEntry2, mockEntry3)
        )

        assertEquals(-1, vFolder.entryId)
    }

    @Test
    fun orderIndexIsInvalidWorks() {
        val vFolder = createUUT(
                "Virtual folder",
                mockDrawable,
                listOf(mockEntry1, mockEntry2, mockEntry3)
        )

        assertEquals(-1, vFolder.orderIndex)
    }

    @Test
    fun iconWorks() {
        val vFolder = createUUT(
                "Virtual folder",
                mockDrawable,
                listOf(mockEntry1, mockEntry2, mockEntry3)
        )

        assertSame(mockDrawable, vFolder.icon)
    }

    @Test
    fun folderSummaryIconWorks() {
        val vFolder = createUUT(
                "Virtual folder",
                mockDrawable,
                listOf(mockEntry1, mockEntry2, mockEntry3)
        )

        assertSame(mockDrawable, vFolder.folderSummaryIcon)
    }

    @Test
    fun isFolderWorks() {
        val vFolder = createUUT(
                "Virtual folder",
                mockDrawable,
                listOf(mockEntry1, mockEntry2, mockEntry3)
        )

        assertTrue(vFolder.isFolder)
    }

    @Test
    fun useIconColorWorks() {
        val vFolder = createUUT(
                "Virtual folder",
                mockDrawable,
                listOf(mockEntry1, mockEntry2, mockEntry3)
        )

        assertFalse(vFolder.useIconColor)
    }
}