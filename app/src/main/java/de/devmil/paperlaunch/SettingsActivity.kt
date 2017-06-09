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
package de.devmil.paperlaunch

import android.app.Activity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.LinearLayout
import android.widget.Toolbar
import de.devmil.paperlaunch.config.UserSettings
import de.devmil.paperlaunch.service.LauncherOverlayService
import de.devmil.paperlaunch.view.fragments.SettingsFragment
import de.devmil.paperlaunch.view.utils.ViewUtils
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class SettingsActivity : Activity(), SettingsFragment.IActivationParametersChangedListener {

    private var mToolbar: Toolbar? = null
    private var mActivationIndicatorContainer: LinearLayout? = null
    private var mFragment: SettingsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mToolbar = findViewById(R.id.activity_settings_toolbar) as Toolbar

        setActionBar(mToolbar)

        actionBar!!.setDisplayHomeAsUpEnabled(true)

        mFragment = SettingsFragment()
        fragmentManager.beginTransaction().replace(R.id.activity_settings_fragment_placeholder, mFragment).commit()

        updateActivationIndicator()

        subscribeToParametersChangedEvent()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSubscription!!.unsubscribe()
    }

    private fun updateActivationIndicator() {
        val us = UserSettings(this)

        val avr = LauncherOverlayService.addActivationViewToWindow(
                mActivationIndicatorContainer,
                this,
                ViewUtils.getPxFromDip(this, us.sensitivityDip.toFloat()).toInt(),
                ViewUtils.getPxFromDip(this, us.activationOffsetPositionDip.toFloat()).toInt(),
                ViewUtils.getPxFromDip(this, us.activationOffsetHeightDip.toFloat()).toInt(),
                us.isOnRightSide,
                ContextCompat.getColor(this, R.color.theme_accent)
        )

        mActivationIndicatorContainer = avr.container

        avr.activationView!!.elevation = ViewUtils.getPxFromDip(this, 3f)

        LauncherOverlayService.notifyConfigChanged(this)
    }

    private var mSubscription: Subscription? = null

    private fun subscribeToParametersChangedEvent() {
        mSubscription = Observable.create(Observable.OnSubscribe<Boolean> { subscriber -> mFragment!!.setOnActivationParametersChangedListener { subscriber.onNext(true) } })
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updateActivationIndicator() }
    }

    override fun onActivationParametersChanged() {
        updateActivationIndicator()
    }

    override fun onPause() {
        super.onPause()
        LauncherOverlayService.removeTouchReceiver(this, mActivationIndicatorContainer)
        mActivationIndicatorContainer = null
    }

    override fun onResume() {
        super.onResume()
        updateActivationIndicator()
    }
}
