/*
 * Copyright 2015 Devmil Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.devmil.paperlaunch.view.fragments

import android.content.Context
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.util.DisplayMetrics

import java.util.ArrayList

import de.devmil.paperlaunch.R
import de.devmil.paperlaunch.config.LauncherGravity
import de.devmil.paperlaunch.config.UserSettings
import de.devmil.paperlaunch.service.LauncherOverlayService
import de.devmil.paperlaunch.view.preferences.SeekBarPreference

class SettingsFragment : PreferenceFragment() {

    private var userSettings: UserSettings? = null
    private var activationParametersChangedListener: (() -> Unit)? = null

    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = activity

        userSettings = UserSettings(context)

        val screen = preferenceManager.createPreferenceScreen(context)
        preferenceScreen = screen

        addActivationSettings(context, screen)
        addAppearanceSettings(context, screen)
    }

    fun setOnActivationParametersChangedListener(listener: () -> Unit) {
        activationParametersChangedListener = listener
    }

    private fun fireActivationParametersChanged() {
        if (activationParametersChangedListener != null) {
            activationParametersChangedListener!!()
        }
    }

    private fun addActivationSettings(context: Context, screen: PreferenceScreen) {
        val activationCategory = PreferenceCategory(context)

        screen.addPreference(activationCategory)

        activationCategory.isPersistent = false
        activationCategory.setTitle(R.string.fragment_settings_category_activation_title)
        activationCategory.setIcon(R.mipmap.ic_wifi_tethering_black_36dp)

        val sensitivityPreference = SeekBarPreference(context, 5, 40)
        activationCategory.addPreference(sensitivityPreference)

        sensitivityPreference.setValue(userSettings!!.sensitivityDip)
        sensitivityPreference.setTitle(R.string.fragment_settings_activation_sensitivity_title)
        sensitivityPreference.isPersistent = false
        sensitivityPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            userSettings!!.load(activity)
            userSettings!!.sensitivityDip = newValue as Int
            userSettings!!.save(activity)
            fireActivationParametersChanged()
            true
        }

        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)

        val heightDpi = metrics.heightPixels / metrics.density

        val offsetHeightPreference = SeekBarPreference(context, 0, heightDpi.toInt())
        activationCategory.addPreference(offsetHeightPreference)

        offsetHeightPreference.setValue(heightDpi.toInt() - userSettings!!.activationOffsetHeightDip)
        offsetHeightPreference.setTitle(R.string.fragment_settings_activation_offset_height_title)
        offsetHeightPreference.isPersistent = false
        offsetHeightPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            userSettings!!.load(activity)
            userSettings!!.activationOffsetHeightDip = heightDpi.toInt() - newValue as Int
            userSettings!!.save(activity)
            fireActivationParametersChanged()
            true
        }

        val offsetMin = -(heightDpi / 2).toInt()
        val offsetMax = (heightDpi / 2).toInt()

        val offsetPositionPreference = SeekBarPreference(context, offsetMin, offsetMax)
        activationCategory.addPreference(offsetPositionPreference)

        offsetPositionPreference.setValue(userSettings!!.activationOffsetPositionDip)
        offsetPositionPreference.setTitle(R.string.fragment_settings_activation_offset_position_title)
        offsetPositionPreference.isPersistent = false
        offsetPositionPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            userSettings!!.load(activity)
            userSettings!!.activationOffsetPositionDip = newValue as Int
            userSettings!!.save(activity)
            fireActivationParametersChanged()
            true
        }

        val vibrateOnActivationPreference = CheckBoxPreference(context)
        activationCategory.addPreference(vibrateOnActivationPreference)

        vibrateOnActivationPreference.isChecked = userSettings!!.isVibrateOnActivation
        vibrateOnActivationPreference.setTitle(R.string.fragment_settings_activation_vibrate_on_activation_title)
        vibrateOnActivationPreference.setSummaryOn(R.string.fragment_settings_activation_vibrate_on_activation_summary_on)
        vibrateOnActivationPreference.setSummaryOff(R.string.fragment_settings_activation_vibrate_on_activation_summary_off)
        vibrateOnActivationPreference.isPersistent = false
        vibrateOnActivationPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            userSettings!!.load(activity)
            userSettings!!.isVibrateOnActivation = newValue as Boolean
            userSettings!!.save(activity)
            LauncherOverlayService.notifyConfigChanged(activity)
            true
        }
    }

    private fun addAppearanceSettings(context: Context, screen: PreferenceScreen) {
        val apperanceCategory = PreferenceCategory(context)
        screen.addPreference(apperanceCategory)

        apperanceCategory.isPersistent = false
        apperanceCategory.setTitle(R.string.fragment_settings_category_appearance_title)

        val showBackgroundPreference = CheckBoxPreference(context)
        apperanceCategory.addPreference(showBackgroundPreference)

        showBackgroundPreference.isPersistent = false
        showBackgroundPreference.setTitle(R.string.fragment_settings_appearance_background_title)
        showBackgroundPreference.setSummaryOn(R.string.fragment_settings_appearance_background_summary_on)
        showBackgroundPreference.setSummaryOff(R.string.fragment_settings_appearance_background_summary_off)
        showBackgroundPreference.isChecked = userSettings!!.isShowBackground
        showBackgroundPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            userSettings!!.load(activity)
            userSettings!!.isShowBackground = newValue as Boolean
            userSettings!!.save(activity)
            LauncherOverlayService.notifyConfigChanged(activity)
            true
        }

        val sidePreference = ListPreference(context)
        apperanceCategory.addPreference(sidePreference)

        class SideEntry(var title: String, var value: Boolean)

        val sideEntryArray = arrayOf(SideEntry(
                context.getString(R.string.fragment_settings_appearance_side_optionleft),
                false
        ), SideEntry(
                context.getString(R.string.fragment_settings_appearance_side_optionright),
                true
        ))

        val sideEntryTitles = ArrayList<CharSequence>()
        val sideEntryValues = ArrayList<CharSequence>()
        for (se in sideEntryArray) {
            sideEntryTitles.add(se.title)
            sideEntryValues.add(java.lang.Boolean.toString(se.value))
        }

        sidePreference.isPersistent = false
        sidePreference.setTitle(R.string.fragment_settings_appearance_side_title)
        sidePreference.entries = sideEntryTitles.toTypedArray()
        sidePreference.entryValues = sideEntryValues.toTypedArray()
        sidePreference.value = java.lang.Boolean.toString(userSettings!!.isOnRightSide)
        sidePreference.summary = context.getString(getSideSummary(userSettings!!.isOnRightSide))
        sidePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val newBooleanValue = java.lang.Boolean.parseBoolean(newValue as String)
            userSettings!!.load(activity)
            userSettings!!.isOnRightSide = newBooleanValue
            userSettings!!.save(activity)
            sidePreference.summary = context.getString(getSideSummary(userSettings!!.isOnRightSide))
            fireActivationParametersChanged()
            true
        }

        val gravityPreference = ListPreference(context)
        apperanceCategory.addPreference(gravityPreference)

        class GravityEntry(var title: String, var value: LauncherGravity)

        val gravityEntryArray = arrayOf(GravityEntry(
                context.getString(R.string.fragment_settings_appearance_gravity_optiontop),
                LauncherGravity.Top
        ), GravityEntry(
                context.getString(R.string.fragment_settings_appearance_gravity_optioncenter),
                LauncherGravity.Center
        ), GravityEntry(
                context.getString(R.string.fragment_settings_appearance_gravity_optionbottom),
                LauncherGravity.Bottom
        ))

        val gravityEntryTitles = ArrayList<CharSequence>()
        val gravityEntryValues = ArrayList<CharSequence>()
        for (ge in gravityEntryArray) {
            gravityEntryTitles.add(ge.title)
            gravityEntryValues.add(Integer.toString(ge.value.value))
        }

        gravityPreference.isPersistent = false
        gravityPreference.setTitle(R.string.fragment_settings_appearance_gravity_title)
        gravityPreference.entries = gravityEntryTitles.toTypedArray()
        gravityPreference.entryValues = gravityEntryValues.toTypedArray()
        gravityPreference.value = Integer.toString(userSettings!!.launcherGravity.value)
        gravityPreference.summary = context.getString(getGravitySummary(userSettings!!.launcherGravity))
        gravityPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val newIntValue = Integer.parseInt(newValue as String)
            val gravityValue = LauncherGravity.fromValue(newIntValue)
            userSettings!!.load(activity)
            userSettings!!.launcherGravity = gravityValue
            userSettings!!.save(activity)
            gravityPreference.summary = context.getString(getGravitySummary(userSettings!!.launcherGravity))
            fireActivationParametersChanged()
            true
        }

        val itemScalePreference = SeekBarPreference(context, 50, 150)
        apperanceCategory.addPreference(itemScalePreference)

        itemScalePreference.setValue(userSettings!!.itemScalePercent)
        itemScalePreference.setTitle(R.string.fragment_settings_appearance_item_scale_title)
        itemScalePreference.isPersistent = false
        itemScalePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            userSettings!!.load(activity)
            userSettings!!.itemScalePercent = newValue as Int
            userSettings!!.save(activity)
            LauncherOverlayService.notifyDataConfigChanged(activity)
            true
        }

        val showLogoPreference = CheckBoxPreference(context)
        apperanceCategory.addPreference(showLogoPreference)

        showLogoPreference.isChecked = userSettings!!.showLogo
        showLogoPreference.setTitle(R.string.fragment_settings_activation_show_logo_title)
        showLogoPreference.setSummaryOn(R.string.fragment_settings_activation_show_logo_summary_on)
        showLogoPreference.setSummaryOff(R.string.fragment_settings_activation_show_logo_summary_off)
        showLogoPreference.isPersistent = false
        showLogoPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            userSettings!!.load(activity)
            userSettings!!.showLogo = newValue as Boolean
            userSettings!!.save(activity)
            LauncherOverlayService.notifyConfigChanged(activity)
            true
        }
    }

    private fun getSideSummary(isOnRightSide: Boolean): Int {
        return if (isOnRightSide)
            R.string.fragment_settings_appearance_side_optionright_summary
        else
            R.string.fragment_settings_appearance_side_optionleft_summary
    }

    private fun getGravitySummary(gravity: LauncherGravity): Int {
        when (gravity) {
            LauncherGravity.Top -> {
                return R.string.fragment_settings_appearance_gravity_optiontop_summary
            }
            LauncherGravity.Center -> {
                return R.string.fragment_settings_appearance_gravity_optioncenter_summary
            }
            LauncherGravity.Bottom -> {
                return R.string.fragment_settings_appearance_gravity_optionbottom_summary
            }
        }
    }
}
