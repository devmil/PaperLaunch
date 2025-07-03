package de.devmil.paperlaunch.config

import android.content.Context
import android.content.SharedPreferences
import junit.framework.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class UserSettingsTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    private fun createUUT(
            sensitivityDip: Int,
            activationOffsetPositionDip: Int,
            activationOffsetHeightDip: Int,
            isShowBackground: Boolean,
            isVibrateOnActivation: Boolean,
            isOnRightSide: Boolean,
            launcherGravity: LauncherGravity,
            showLogo: Boolean,
            itemScalePercent: Int
    ): UserSettings {
        //Expect loaded user settings
        `when`(mockContext.getSharedPreferences(UserSettings.SHARED_PREFS_NAME, Context.MODE_PRIVATE))
                .thenReturn(mockSharedPreferences)

        `when`(mockSharedPreferences.getInt(eq(UserSettings.KEY_SENSITIVITY_DIP), anyInt()))
                .thenReturn(sensitivityDip)
        `when`(mockSharedPreferences.getInt(eq(UserSettings.KEY_ACTIVATION_OFFSET_POSITION_DIP), anyInt()))
                .thenReturn(activationOffsetPositionDip)
        `when`(mockSharedPreferences.getInt(eq(UserSettings.KEY_ACTIVATION_OFFSET_HEIGHT_DIP), anyInt()))
                .thenReturn(activationOffsetHeightDip)
        `when`(mockSharedPreferences.getBoolean(eq(UserSettings.KEY_SHOW_BACKGROUND), anyBoolean()))
                .thenReturn(isShowBackground)
        `when`(mockSharedPreferences.getBoolean(eq(UserSettings.KEY_VIBRATE_ON_ACTIVATION), anyBoolean()))
                .thenReturn(isVibrateOnActivation)
        `when`(mockSharedPreferences.getBoolean(eq(UserSettings.KEY_IS_ON_RIGHT_SIDE), anyBoolean()))
                .thenReturn(isOnRightSide)
        `when`(mockSharedPreferences.getInt(eq(UserSettings.KEY_LAUNCHER_GRAVITY), anyInt()))
                .thenReturn(launcherGravity.value)
        `when`(mockSharedPreferences.getBoolean(eq(UserSettings.KEY_SHOW_LOGO), anyBoolean()))
                .thenReturn(showLogo)
        `when`(mockSharedPreferences.getInt(eq(UserSettings.KEY_ITEM_SCALE_PERCENT), anyInt()))
                .thenReturn(itemScalePercent)

        return UserSettings(mockContext)
    }

    private fun createUUT(): UserSettings {
        return createUUT(
                0,
                0,
                0,
                false,
                false,
                false,
                LauncherGravity.Center,
                false,
                0
        )
    }

    @Test
    fun createWorks() {
        createUUT()
    }
}