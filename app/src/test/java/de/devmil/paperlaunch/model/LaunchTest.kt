package de.devmil.paperlaunch.model

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import de.devmil.paperlaunch.R
import de.devmil.paperlaunch.storage.EntryDTO
import de.devmil.paperlaunch.storage.LaunchDTO
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LaunchTest {
    @Mock
    private lateinit var mockContextAccess : IContextAccess
    @Mock
    private lateinit var mockDrawable : Drawable
    @Mock
    private lateinit var mockIntent : Intent
    @Mock
    private lateinit var mockUri : Uri

    private fun createUUT(
            entryId: Long,
            entryOrderIndex: Long,
            launchId: Long,
            name: String?,
            icon: Drawable?,
            intent: Intent,
            parentFolderId: Long): Launch {

        val lDto = LaunchDTO(launchId, name, intent, icon)
        val eDto = EntryDTO(entryId, entryOrderIndex, launchId, 0, parentFolderId)

        return Launch(mockContextAccess, lDto, eDto)
    }

    @Test
    fun creatingWorks() {
        createUUT(
                0,
                0,
                1,
                "some app",
                null,
                mockIntent,
                -1
        )
    }

    @Test
    fun idWorks() {
        val launch = createUUT(
                0,
                0,
                4711,
                "some app",
                null,
                mockIntent,
                -1
        )
        Assert.assertEquals(4711L, launch.id)
    }

    @Test
    fun entryIdWorks() {
        val launch = createUUT(
                42,
                0,
                1,
                "some app",
                null,
                mockIntent,
                -1
        )
        Assert.assertEquals(42L, launch.entryId)
    }

    @Test
    fun orderIndexWorks() {
        val launch = createUUT(
                0,
                43,
                1,
                "some app",
                null,
                mockIntent,
                -1
        )
        Assert.assertEquals(43L, launch.orderIndex)
    }

    @Test
    fun nameOverrideWorks() {
        val launch = createUUT(
                0,
                43,
                1,
                "this is some app!!",
                null,
                mockIntent,
                -1
        )
        Assert.assertEquals("this is some app!!", launch.name)
    }

    @Test
    fun nameRetrievalForActionMainWorks() {
        //providing no name leads to a name retrieval (at least it should ;) )
        val launch = createUUT(
                0,
                43,
                1,
                null,
                null,
                mockIntent,
                -1
        )
        `when`(mockIntent.action)
                .thenReturn(Intent.ACTION_MAIN)
        `when`(mockContextAccess.getAppName(mockIntent))
                .thenReturn("retrieved app name")

        Assert.assertEquals("retrieved app name", launch.name)
    }

    @Test
    fun nameRetrievalForActionViewWithUrlWorks() {
        //providing no name leads to a name retrieval (at least it should ;) )
        val launch = createUUT(
                0,
                43,
                1,
                null,
                null,
                mockIntent,
                -1
        )
        //mock a URL view intent
        `when`(mockIntent.action)
                .thenReturn(Intent.ACTION_VIEW)
        `when`(mockIntent.hasExtra(Launch.EXTRA_URL_NAME))
                .thenReturn(true)
        `when`(mockIntent.getStringExtra(Launch.EXTRA_URL_NAME))
                .thenReturn("http://this.is.a.url")

        Assert.assertEquals("http://this.is.a.url", launch.name)
    }

    @Test
    fun nameRetrievalForActionViewWithoutUrlWorks() {
        //providing no name leads to a name retrieval (at least it should ;) )
        val launch = createUUT(
                0,
                43,
                1,
                null,
                null,
                mockIntent,
                -1
        )
        //mock a non-URL view intent
        `when`(mockIntent.action)
                .thenReturn(Intent.ACTION_VIEW)
        `when`(mockIntent.hasExtra(Launch.EXTRA_URL_NAME))
                .thenReturn(false)
        `when`(mockIntent.data)
                .thenReturn(mockUri)
        `when`(mockUri.toString())
                .thenReturn("http://this.is.a.uri")

        Assert.assertEquals("http://this.is.a.uri", launch.name)
    }

    @Test
    fun nameRetrievalForActionMainCacheWorks() {
        //providing no name leads to a name retrieval (at least it should ;) )
        val launch = createUUT(
                0,
                43,
                1,
                null,
                null,
                mockIntent,
                -1
        )
        `when`(mockIntent.action)
                .thenReturn(Intent.ACTION_MAIN)
        `when`(mockContextAccess.getAppName(mockIntent))
                .thenReturn("retrieved app name")

        Assert.assertEquals("retrieved app name", launch.name)
        //reset all expectations
        Mockito.reset(mockIntent)
        //here the cached value gets retrieved
        Assert.assertEquals("retrieved app name", launch.name)
    }

    @Test
    fun iconRetrievalForActionMainWorks() {
        //providing no icon leads to a name retrieval (at least it should ;) )
        val launch = createUUT(
                0,
                43,
                1,
                null,
                null,
                mockIntent,
                -1
        )
        `when`(mockIntent.action)
                .thenReturn(Intent.ACTION_MAIN)
        `when`(mockContextAccess.getAppIcon(mockIntent))
                .thenReturn(mockDrawable)

        Assert.assertSame(mockDrawable, launch.icon)
    }

    @Test
    fun iconOverrideWorks() {
        //when the icon is provided directly then this icon is used
        val launch = createUUT(
                0,
                43,
                1,
                null,
                mockDrawable,
                mockIntent,
                -1
        )
        Assert.assertSame(mockDrawable, launch.icon)
    }

    @Test
    fun iconRetrievalForActionViewWorks() {
        //providing no icon leads to a name retrieval (at least it should ;) )
        val launch = createUUT(
                0,
                43,
                1,
                null,
                null,
                mockIntent,
                -1
        )
        //mock a URL view intent
        `when`(mockIntent.action)
                .thenReturn(Intent.ACTION_VIEW)
        `when`(mockContextAccess.getDrawable(R.mipmap.ic_web_black_48dp, false))
                .thenReturn(mockDrawable)

        Assert.assertSame(mockDrawable, launch.icon)
    }

    @Test
    fun iconRetrievalForOtherActionWorks() {
        //providing no icon leads to a name retrieval (at least it should ;) )
        val launch = createUUT(
                0,
                43,
                1,
                null,
                null,
                mockIntent,
                -1
        )
        //mock a URL view intent
        `when`(mockIntent.action)
                .thenReturn(Intent.ACTION_ANSWER)

        Assert.assertNull(launch.icon)
    }

    @Test
    fun iconRetrievalForActionMainCacheWorks() {
        //providing no name leads to a name retrieval (at least it should ;) )
        val launch = createUUT(
                0,
                43,
                1,
                null,
                null,
                mockIntent,
                -1
        )
        `when`(mockIntent.action)
                .thenReturn(Intent.ACTION_MAIN)
        `when`(mockContextAccess.getAppIcon(mockIntent))
                .thenReturn(mockDrawable)

        Assert.assertSame(mockDrawable, launch.icon)
        //reset all expectations
        Mockito.reset(mockIntent)
        //here the cached value gets retrieved
        Assert.assertSame(mockDrawable, launch.icon)
    }

    @Test
    fun folderSummaryIconIsiconWorks() {
        //when the icon is provided directly then this icon is used
        val launch = createUUT(
                0,
                43,
                1,
                null,
                mockDrawable,
                mockIntent,
                -1
        )
        Assert.assertSame(mockDrawable, launch.folderSummaryIcon)
    }

    @Test
    fun isFolderWorks() {
        //when the icon is provided directly then this icon is used
        val launch = createUUT(
                0,
                43,
                1,
                null,
                null,
                mockIntent,
                -1
        )
        Assert.assertFalse(launch.isFolder)
    }

    @Test
    fun useIconColorWorks() {
        //when the icon is provided directly then this icon is used
        val launch = createUUT(
                0,
                43,
                1,
                null,
                null,
                mockIntent,
                -1
        )
        Assert.assertTrue(launch.useIconColor)
    }

    @Test
    fun launchIntentWorks() {
        //when the icon is provided directly then this icon is used
        val launch = createUUT(
                0,
                43,
                1,
                null,
                null,
                mockIntent,
                -1
        )
        Assert.assertSame(mockIntent, launch.launchIntent)
    }
}