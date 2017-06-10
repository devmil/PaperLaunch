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
package de.devmil.paperlaunch.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.IBinder
import android.os.Vibrator
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout

import java.util.ArrayList

import de.devmil.paperlaunch.R
import de.devmil.paperlaunch.MainActivity
import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.model.IFolder
import de.devmil.paperlaunch.config.LaunchConfig
import de.devmil.paperlaunch.model.VirtualFolder
import de.devmil.paperlaunch.storage.EntriesDataSource
import de.devmil.paperlaunch.storage.ITransactionAction
import de.devmil.paperlaunch.storage.ITransactionContext
import de.devmil.paperlaunch.config.UserSettings
import de.devmil.paperlaunch.utils.ActivationIndicatorHelper
import de.devmil.paperlaunch.view.utils.ViewUtils
import de.devmil.paperlaunch.view.LauncherView

class LauncherOverlayService : Service() {

    private var mNotification: Notification? = null
    private var mAlreadyRegistered = false
    //private LinearLayout mTouchReceiver = null;
    private var mTouchReceiverContainer: LinearLayout? = null
    private var mLauncherView: LauncherView? = null
    private var mIsLauncherActive = false
    private var mCurrentConfig: LaunchConfig? = null
    private var mEntriesLoaded = false

    //receivers
    private var mScreenOnOffReceiver: ScreenOnOffReceiver? = null
    private var mOrientationChangeReceiver: OrientationChangeReceiver? = null

    private var mState: ServiceState? = null

    internal inner class ScreenOnOffReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null) {
                return
            }
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                finishLauncher()
            }
        }
    }

    internal inner class OrientationChangeReceiver(context: Context) : BroadcastReceiver() {

        private var mLastConfiguration: Int = 0

        init {
            mLastConfiguration = context.resources.configuration.orientation
        }

        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null) {
                return
            }
            if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                val newOrientation = context.resources.configuration.orientation
                if (mLastConfiguration != newOrientation) {
                    mLastConfiguration = newOrientation
                    finishLauncher()
                    ensureData(true)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        mState = ServiceState(this)

        registerScreenOnReceiver()
        registerOrientationChangeReceiver()
    }

    override fun onDestroy() {
        unregisterScreenOnOffReceiver()
        unregisterOrientationChangeReceiver()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && ACTION_LAUNCH == intent.action) {
            adaptState(false)
        } else if (intent != null && ACTION_NOTIFYDATACHANGED == intent.action) {
            adaptState(true)
        } else if (intent != null && ACTION_NOTIFYCONFIGCHANGED == intent.action) {
            reloadConfigMetadata()
            reloadTouchReceiver()
        } else if (intent != null && ACTION_PAUSE == intent.action) {
            mState!!.isActive = false
            mState!!.save(this)
            adaptState(false)
        } else if (intent != null && ACTION_PLAY == intent.action) {
            mState!!.isActive = true
            mState!!.save(this)
            adaptState(false)
        } else if (intent != null && ACTION_ENSUREACTIVATIONTAPPABLE == intent.action) {
            reloadTouchReceiver()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun adaptState(forceReload: Boolean) {
        if (mState!!.isActive) {
            ensureOverlayActive(forceReload)
        } else {
            ensureOverlayInActive()
        }
        ensureNotification(true)
    }

    private fun registerOrientationChangeReceiver() {
        unregisterOrientationChangeReceiver()
        val filter = IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED)

        mOrientationChangeReceiver = OrientationChangeReceiver(this)
        registerReceiver(mOrientationChangeReceiver, filter)
    }

    private fun unregisterOrientationChangeReceiver() {
        if (mOrientationChangeReceiver != null) {
            unregisterReceiver(mOrientationChangeReceiver)
            mOrientationChangeReceiver = null
        }
    }

    private fun registerScreenOnReceiver() {
        unregisterScreenOnOffReceiver()
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        mScreenOnOffReceiver = ScreenOnOffReceiver()
        registerReceiver(mScreenOnOffReceiver, filter)
    }

    private fun unregisterScreenOnOffReceiver() {
        if (mScreenOnOffReceiver != null) {
            unregisterReceiver(mScreenOnOffReceiver)
            mScreenOnOffReceiver = null
        }
    }

    private fun ensureOverlayActive(forceReload: Boolean) {
        val alreadyRegistered = mAlreadyRegistered

        if (!forceReload && alreadyRegistered) {
            return
        }

        ensureConfig(forceReload)

        reloadTouchReceiver()

        ensureData(forceReload)
    }

    private fun ensureOverlayInActive() {
        finishLauncher()
        removeTouchReceiver()
    }

    private fun ensureConfig(forceReload: Boolean) {
        if (forceReload) {
            mCurrentConfig = null
        }
        if (mCurrentConfig == null) {
            mCurrentConfig = LaunchConfig(UserSettings(this))
        }
    }

    private fun reloadConfigMetadata() {
        ensureConfig(false)

        val entries = mCurrentConfig!!.entries
        mCurrentConfig = LaunchConfig(UserSettings(this))
        mCurrentConfig!!.entries = entries
    }

    private fun ensureData(forceReload: Boolean) {
        ensureConfig(forceReload)
        if (forceReload) {
            mEntriesLoaded = false
        }
        if (!mEntriesLoaded) {
            class Local {
                var entries: MutableList<IEntry>? = null
            }

            val local = Local()
            EntriesDataSource.instance.accessData(this, object : ITransactionAction {
                override fun execute(transactionContext: ITransactionContext) {
                    local.entries = transactionContext.loadRootContent().toMutableList()
                }
            })

            mCurrentConfig!!.entries = prepareEntries(local.entries!!)
            mEntriesLoaded = true
        }
    }

    private fun prepareEntries(entries: MutableList<IEntry>): List<IEntry> {

        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)

        val entryHeightDip = mCurrentConfig!!.imageWidthDip + 2 * mCurrentConfig!!.imageMarginDip + 2 * mCurrentConfig!!.entriesMarginDip

        val entryHeightPx = ViewUtils.getPxFromDip(this, entryHeightDip)

        val numberOfEntriesPossible = Math.floor(metrics.heightPixels / entryHeightPx.toDouble()).toInt()

        if (entries.size > numberOfEntriesPossible) {
            val virtualFolderContent = ArrayList<IEntry>()
            while (entries.size >= numberOfEntriesPossible) {
                virtualFolderContent.add(entries[numberOfEntriesPossible - 1])
                entries.removeAt(numberOfEntriesPossible - 1)
            }
            val vf = VirtualFolder(
                    getString(R.string.launcher_virtual_folder_name),
                    getDrawable(R.mipmap.ic_auto_folder_grey),
                    virtualFolderContent)
            entries.add(vf)
        }

        entries
                .filter { it.isFolder }
                .map { it as IFolder }
                .forEach {
                    if(it.subEntries != null) {
                        it.subEntries = prepareEntries(it.subEntries!!.toMutableList())
                    }
                }

        return entries
    }

    private fun createLauncherView(event: MotionEvent): LauncherView {
        val result = LauncherView(this)
        ensureData(false)
        result.doInitialize(mCurrentConfig!!)
        result.doAutoStart(event)

        return result
    }

    @Synchronized private fun handleTouch(touchReceiver: LinearLayout, event: MotionEvent): Boolean {
        if (!mIsLauncherActive) {
            mLauncherView = createLauncherView(event)
            //            Rect hitRect = new Rect();
            //            touchReceiver.getHitRect(hitRect);
            //            if(!hitRect.contains((int)event.getX(), (int)event.getY()))
            //                return false;

            val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT)

            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            wm.addView(mLauncherView, params)
            if (mCurrentConfig!!.isVibrateOnActivation) {
                try {
                    val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    v.vibrate(60)
                } catch (e: Exception) {
                    Log.w(TAG, "Vibrate didn't work", e)
                }

            }
            mIsLauncherActive = true
            mLauncherView!!.setListener(object : LauncherView.ILauncherViewListener {
                override fun onFinished() {
                    finishLauncher()
                }
            })

        } else {
            transferMotionEvent(touchReceiver, mLauncherView!!, event)
        }

        return true
    }

    @Synchronized private fun removeTouchReceiver() {
        removeTouchReceiver(this, mTouchReceiverContainer)
        mTouchReceiverContainer = null
        mAlreadyRegistered = false
    }

    @Synchronized private fun reloadTouchReceiver() {

        val avr = addActivationViewToWindow(
                mTouchReceiverContainer,
                this,
                ViewUtils.getPxFromDip(this, mCurrentConfig!!.launcherSensitivityDip.toFloat()).toInt(),
                ViewUtils.getPxFromDip(this, mCurrentConfig!!.launcherOffsetPositionDip.toFloat()).toInt(),
                ViewUtils.getPxFromDip(this, mCurrentConfig!!.launcherOffsetHeightDip.toFloat()).toInt(),
                mCurrentConfig!!.isOnRightSide,
                Color.TRANSPARENT)

        avr.activationView!!.setOnTouchListener { v, event -> handleTouch(avr.activationView!!, event) }

        avr.activationView!!.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                finishLauncher()
            }
        }

        mTouchReceiverContainer = avr.container

        mAlreadyRegistered = true
    }

    class ActivationViewResult {
        var container: LinearLayout? = null
        var activationView: LinearLayout? = null
    }

    @Synchronized private fun finishLauncher() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (mLauncherView != null) {
            try {
                wm.removeView(mLauncherView)
            } catch (e: Exception) {
            }

            mLauncherView = null
        }
        mIsLauncherActive = false
    }

    private fun transferMotionEvent(from: View, to: LauncherView, event: MotionEvent) {
        val fromX = event.x
        val fromY = event.y

        val toLocation = IntArray(2)
        to.getLocationOnScreen(toLocation)
        val fromLocation = IntArray(2)
        from.getLocationOnScreen(fromLocation)

        val newX = fromX + (fromLocation[0] - toLocation[0])
        val newY = fromY + (fromLocation[1] - toLocation[1])

        to.handleTouchEvent(event.action, newX, newY)
    }

    private fun ensureNotification(force: Boolean = false) {
        if (!force && mNotification != null) {
            return
        }
        val settingsPendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                0
        )

        val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

        val builder = Notification.Builder(this)
                .setContentTitle("PaperLaunch")
                .setContentText(getString(if (mState!!.isActive) R.string.notification_content_active else R.string.notification_content_inactive))
                .setOngoing(true)
                .setLocalOnly(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setLargeIcon(largeIcon)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(settingsPendingIntent)

        if (mState!!.isActive) {
            val pauseIntent = Intent(ACTION_PAUSE)
            pauseIntent.setClass(this, LauncherOverlayService::class.java)
            val pendingPauseIntent = PendingIntent.getService(
                    this,
                    0,
                    pauseIntent,
                    0)
            builder.addAction(Notification.Action(
                    R.mipmap.ic_pause_black_24dp,
                    getString(R.string.notification_pause),
                    pendingPauseIntent
            ))
        } else {
            val playIntent = Intent(ACTION_PLAY)
            playIntent.setClass(this, LauncherOverlayService::class.java)
            val pendingPlayIntent = PendingIntent.getService(
                    this,
                    0,
                    playIntent,
                    0)
            builder.addAction(Notification.Action(
                    R.mipmap.ic_play_arrow_black_24dp,
                    getString(R.string.notification_play),
                    pendingPlayIntent
            ))
        }

        val n = builder.build()
        if (mNotification != null) {
            n.`when` = mNotification!!.`when`
        }
        mNotification = n

        startForeground(NOTIFICATION_ID, n)

    }

    companion object {

        private val TAG = LauncherOverlayService::class.java.simpleName

        private val ACTION_LAUNCH = "ACTION_LAUNCH"
        private val ACTION_NOTIFYDATACHANGED = "ACTION_NOTIFYDATACHANGED"
        private val ACTION_NOTIFYCONFIGCHANGED = "ACTION_NOTIFYCONFIGCHANGED"
        private val ACTION_ENSUREACTIVATIONTAPPABLE = "ACTION_ENSUREACTIVATIONTAPPABLE"
        private val ACTION_PAUSE = "ACTION_PAUSE"
        private val ACTION_PLAY = "ACTION_PLAY"
        private val NOTIFICATION_ID = 2000

        fun launch(context: Context) {
            val launchServiceIntent = Intent(context, LauncherOverlayService::class.java)
            launchServiceIntent.action = ACTION_LAUNCH
            context.startService(launchServiceIntent)
        }

        fun notifyDataChanged(context: Context) {
            val launchServiceIntent = Intent(context, LauncherOverlayService::class.java)
            launchServiceIntent.action = ACTION_NOTIFYDATACHANGED
            context.startService(launchServiceIntent)
        }

        fun notifyConfigChanged(context: Context) {
            val launchServiceIntent = Intent(context, LauncherOverlayService::class.java)
            launchServiceIntent.action = ACTION_NOTIFYCONFIGCHANGED
            context.startService(launchServiceIntent)
        }

        fun ensureActivationTappable(context: Context) {
            val launchServiceIntent = Intent(context, LauncherOverlayService::class.java)
            launchServiceIntent.action = ACTION_ENSUREACTIVATIONTAPPABLE
            context.startService(launchServiceIntent)
        }

        fun removeTouchReceiver(context: Context, container: LinearLayout?) {

            if (container != null) {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.removeView(container)
            }
        }

        fun addActivationViewToWindow(
                oldContainer: LinearLayout?,
                context: Context,
                sensitivityPx: Int,
                offsetPositionPx: Int,
                offsetHeightPx: Int,
                isOnRightSide: Boolean,
                backgroundColor: Int): ActivationViewResult {

            val result = ActivationViewResult()

            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val metrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(metrics)

            val windowRect = Rect(0, 0, metrics.widthPixels, metrics.heightPixels)

            val activationRect = ActivationIndicatorHelper.calculateActivationIndicatorSize(
                    sensitivityPx,
                    offsetPositionPx,
                    offsetHeightPx,
                    isOnRightSide,
                    windowRect
            )

            val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT)

            params.gravity = if (isOnRightSide) Gravity.RIGHT else Gravity.LEFT

            result.container = LinearLayout(context)

            wm.addView(result.container, params)

            result.activationView = LinearLayout(context)
            result.activationView!!.setBackgroundColor(backgroundColor)

            val touchReceiverParams = LinearLayout.LayoutParams(
                    activationRect.width(),
                    activationRect.height())
            touchReceiverParams.setMargins(0, activationRect.top, 0, windowRect.height() - activationRect.bottom)

            result.container!!.addView(result.activationView, touchReceiverParams)

            removeTouchReceiver(context, oldContainer)

            return result
        }
    }
}
