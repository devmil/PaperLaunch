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
package de.devmil.paperlaunch.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.MainActivity;
import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.model.IFolder;
import de.devmil.paperlaunch.config.LaunchConfig;
import de.devmil.paperlaunch.model.VirtualFolder;
import de.devmil.paperlaunch.storage.EntriesDataSource;
import de.devmil.paperlaunch.storage.ITransactionAction;
import de.devmil.paperlaunch.storage.ITransactionContext;
import de.devmil.paperlaunch.config.UserSettings;
import de.devmil.paperlaunch.utils.ActivationIndicatorHelper;
import de.devmil.paperlaunch.view.utils.ViewUtils;
import de.devmil.paperlaunch.view.LauncherView;

public class LauncherOverlayService extends Service {

    private static final String TAG = LauncherOverlayService.class.getSimpleName();

    private static final String ACTION_LAUNCH = "ACTION_LAUNCH";
    private static final String ACTION_NOTIFYDATACHANGED = "ACTION_NOTIFYDATACHANGED";
    private static final String ACTION_NOTIFYCONFIGCHANGED = "ACTION_NOTIFYCONFIGCHANGED";
    private static final String ACTION_ENSUREACTIVATIONTAPPABLE = "ACTION_ENSUREACTIVATIONTAPPABLE";
    private static final String ACTION_PAUSE = "ACTION_PAUSE";
    private static final String ACTION_PLAY = "ACTION_PLAY";
    private static final int NOTIFICATION_ID = 2000;

    private Notification mNotification = null;
    private boolean mAlreadyRegistered = false;
    //private LinearLayout mTouchReceiver = null;
    private LinearLayout mTouchReceiverContainer = null;
    private LauncherView mLauncherView = null;
    private boolean mIsLauncherActive = false;
    private LaunchConfig mCurrentConfig;
    private boolean mEntriesLoaded = false;

    //receivers
    private ScreenOnOffReceiver mScreenOnOffReceiver;
    private OrientationChangeReceiver mOrientationChangeReceiver;

    private ServiceState mState;

    public LauncherOverlayService() {
    }

    class ScreenOnOffReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null) {
                return;
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                finishLauncher();
            }
        }
    }

    class OrientationChangeReceiver extends BroadcastReceiver {

        private int mLastConfiguration;

        public OrientationChangeReceiver(Context context) {
            super();
            mLastConfiguration = context.getResources().getConfiguration().orientation;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null) {
                return;
            }
            if(intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                int newOrientation = context.getResources().getConfiguration().orientation;
                if(mLastConfiguration != newOrientation) {
                    mLastConfiguration = newOrientation;
                    finishLauncher();
                    ensureData(true);
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mState = new ServiceState(this);

        registerScreenOnReceiver();
        registerOrientationChangeReceiver();
    }

    @Override
    public void onDestroy() {
        unregisterScreenOnOffReceiver();
        unregisterOrientationChangeReceiver();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && ACTION_LAUNCH.equals(intent.getAction())) {
            adaptState(false);
        }
        else if(intent != null && ACTION_NOTIFYDATACHANGED.equals(intent.getAction())) {
            adaptState(true);
        }
        else if (intent != null && ACTION_NOTIFYCONFIGCHANGED.equals(intent.getAction())) {
            reloadConfigMetadata();
            reloadTouchReceiver();
        }
        else if(intent != null && ACTION_PAUSE.equals(intent.getAction())) {
            mState.setIsActive(false);
            mState.save(this);
            adaptState(false);
        }
        else if(intent != null && ACTION_PLAY.equals(intent.getAction())) {
            mState.setIsActive(true);
            mState.save(this);
            adaptState(false);
        } else if(intent != null && ACTION_ENSUREACTIVATIONTAPPABLE.equals(intent.getAction())) {
            reloadTouchReceiver();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void adaptState(boolean forceReload) {
        if(mState.getIsActive()) {
            ensureOverlayActive(forceReload);
        } else {
            ensureOverlayInActive();
        }
        ensureNotification(true);
    }

    private void registerOrientationChangeReceiver() {
        unregisterOrientationChangeReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);

        mOrientationChangeReceiver = new OrientationChangeReceiver(this);
        registerReceiver(mOrientationChangeReceiver, filter);
    }

    private void unregisterOrientationChangeReceiver() {
        if(mOrientationChangeReceiver != null) {
            unregisterReceiver(mOrientationChangeReceiver);
            mOrientationChangeReceiver = null;
        }
    }

    private void registerScreenOnReceiver() {
        unregisterScreenOnOffReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenOnOffReceiver = new ScreenOnOffReceiver();
        registerReceiver(mScreenOnOffReceiver, filter);
    }

    private void unregisterScreenOnOffReceiver() {
        if(mScreenOnOffReceiver != null) {
            unregisterReceiver(mScreenOnOffReceiver);
            mScreenOnOffReceiver = null;
        }
    }

    public static void launch(Context context) {
        Intent launchServiceIntent = new Intent(context, LauncherOverlayService.class);
        launchServiceIntent.setAction(ACTION_LAUNCH);
        context.startService(launchServiceIntent);
    }

    public static void notifyDataChanged(Context context) {
        Intent launchServiceIntent = new Intent(context, LauncherOverlayService.class);
        launchServiceIntent.setAction(ACTION_NOTIFYDATACHANGED);
        context.startService(launchServiceIntent);
    }

    public static void notifyConfigChanged(Context context) {
        Intent launchServiceIntent = new Intent(context, LauncherOverlayService.class);
        launchServiceIntent.setAction(ACTION_NOTIFYCONFIGCHANGED);
        context.startService(launchServiceIntent);
    }

    public static void ensureActivationTappable(Context context) {
        Intent launchServiceIntent = new Intent(context, LauncherOverlayService.class);
        launchServiceIntent.setAction(ACTION_ENSUREACTIVATIONTAPPABLE);
        context.startService(launchServiceIntent);
    }

    private void ensureOverlayActive(boolean forceReload) {
        boolean alreadyRegistered = mAlreadyRegistered;

        if(!forceReload && alreadyRegistered) {
            return;
        }

        ensureConfig(forceReload);

        reloadTouchReceiver();

        ensureData(forceReload);
    }

    private void ensureOverlayInActive() {
        finishLauncher();
        removeTouchReceiver();
    }

    private void ensureConfig(boolean forceReload) {
        if(forceReload) {
            mCurrentConfig = null;
        }
        if(mCurrentConfig == null) {
            mCurrentConfig = new LaunchConfig(new UserSettings(this));
        }
    }

    private void reloadConfigMetadata() {
        ensureConfig(false);

        List<IEntry> entries =  mCurrentConfig.getEntries();
        mCurrentConfig = new LaunchConfig(new UserSettings(this));
        mCurrentConfig.setEntries(entries);
    }

    private void ensureData(boolean forceReload) {
        ensureConfig(forceReload);
        if(forceReload) {
            mEntriesLoaded = false;
        }
        if(!mEntriesLoaded) {
            class Local {
                List<IEntry> entries = null;
            }
            final Local local = new Local();
            EntriesDataSource.getInstance().accessData(this, new ITransactionAction() {
                @Override
                public void execute(ITransactionContext transactionContext) {
                    local.entries = transactionContext.loadRootContent();
                }
            });

            mCurrentConfig.setEntries(prepareEntries(local.entries));
            mEntriesLoaded = true;
        }
    }

    private List<IEntry> prepareEntries(List<IEntry> entries) {

        final WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        float entryHeightDip = mCurrentConfig.getImageWidthDip()
                + 2 * mCurrentConfig.getImageMarginDip()
                + 2 * mCurrentConfig.getEntriesMarginDip();

        int entryHeightPx = (int)ViewUtils.getPxFromDip(this, entryHeightDip);

        int numberOfEntriesPossible = metrics.heightPixels / entryHeightPx;

        if(entries.size() > numberOfEntriesPossible) {
            List<IEntry> virtualFolderContent = new ArrayList<>();
            while(entries.size() >= numberOfEntriesPossible) {
                virtualFolderContent.add(entries.get(numberOfEntriesPossible - 1));
                entries.remove(numberOfEntriesPossible - 1);
            }
            VirtualFolder vf = new VirtualFolder(
                    getString(R.string.launcher_virtual_folder_name),
                    getDrawable(R.mipmap.ic_auto_folder_grey),
                    virtualFolderContent);
            entries.add(vf);
        }

        for(IEntry entry : entries) {
            if(entry.isFolder()) {
                IFolder folder = (IFolder)entry;
                folder.setSubEntries(prepareEntries(folder.getSubEntries()));
            }
        }

        return entries;
    }

    private LauncherView createLauncherView(MotionEvent event) {
        LauncherView result = new LauncherView(this);
        ensureData(false);
        result.doInitialize(mCurrentConfig);
        result.doAutoStart(event);

        return result;
    }

    private synchronized boolean handleTouch(final LinearLayout touchReceiver, final MotionEvent event) {
        if(!mIsLauncherActive) {
            mLauncherView = createLauncherView(event);
//            Rect hitRect = new Rect();
//            touchReceiver.getHitRect(hitRect);
//            if(!hitRect.contains((int)event.getX(), (int)event.getY()))
//                return false;

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);

            final WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

            wm.addView(mLauncherView, params);
            if(mCurrentConfig.isVibrateOnActivation()) {
                try {
                    Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    v.vibrate(60);
                } catch (Exception e) {
                    Log.w(TAG, "Vibrate didn't work", e);
                }
            }
            mIsLauncherActive = true;
            mLauncherView.setListener(new LauncherView.ILauncherViewListener() {
                @Override
                public void onFinished() {
                    finishLauncher();
                }
            });

        } else {
            transferMotionEvent(touchReceiver, mLauncherView, event);
        }

        return true;
    }

    private synchronized void removeTouchReceiver() {
        removeTouchReceiver(this, mTouchReceiverContainer);
        mTouchReceiverContainer = null;
        mAlreadyRegistered = false;
    }

    public static void removeTouchReceiver(Context context, LinearLayout container) {

        if(container != null) {
            WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            wm.removeView(container);
        }
    }

    private synchronized void reloadTouchReceiver() {

        final ActivationViewResult avr = addActivationViewToWindow(
                mTouchReceiverContainer,
                this,
                (int)ViewUtils.getPxFromDip(this, mCurrentConfig.getLauncherSensitivityDip()),
                (int)ViewUtils.getPxFromDip(this, mCurrentConfig.getLauncherOffsetPositionDip()),
                (int)ViewUtils.getPxFromDip(this, mCurrentConfig.getLauncherOffsetHeightDip()),
                mCurrentConfig.isOnRightSide(),
                Color.TRANSPARENT);

        avr.activationView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleTouch(avr.activationView, event);
            }
        });

        avr.activationView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    finishLauncher();
                }
            }
        });

        mTouchReceiverContainer = avr.container;

        mAlreadyRegistered = true;
    }

    public static class ActivationViewResult {
        public LinearLayout container;
        public LinearLayout activationView;
    }

    public static ActivationViewResult addActivationViewToWindow(
            LinearLayout oldContainer,
            Context context,
            int sensitivityPx,
            int offsetPositionPx,
            int offsetHeightPx,
            boolean isOnRightSide,
            int backgroundColor) {

        ActivationViewResult result = new ActivationViewResult();

        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        Rect windowRect = new Rect(0, 0, metrics.widthPixels, metrics.heightPixels);

        Rect activationRect = ActivationIndicatorHelper.calculateActivationIndicatorSize(
                sensitivityPx,
                offsetPositionPx,
                offsetHeightPx,
                isOnRightSide,
                windowRect
        );

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        params.gravity = isOnRightSide ? Gravity.RIGHT : Gravity.LEFT;

        result.container = new LinearLayout(context);

        wm.addView(result.container, params);

        result.activationView = new LinearLayout(context);
        result.activationView.setBackgroundColor(backgroundColor);

        LinearLayout.LayoutParams touchReceiverParams = new LinearLayout.LayoutParams(
                activationRect.width(),
                activationRect.height());
        touchReceiverParams.setMargins(0, activationRect.top, 0, windowRect.height() - activationRect.bottom);

        result.container.addView(result.activationView, touchReceiverParams);

        removeTouchReceiver(context, oldContainer);

        return result;
    }

    private synchronized void finishLauncher() {
        final WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (mLauncherView != null) {
            try {
                wm.removeView(mLauncherView);
            }
            catch(Exception e) {
            }
            mLauncherView = null;
        }
        mIsLauncherActive = false;
    }

    private void transferMotionEvent(View from, LauncherView to, MotionEvent event) {
        float fromX = event.getX();
        float fromY = event.getY();

        int[] toLocation = new int[2];
        to.getLocationOnScreen(toLocation);
        int[] fromLocation = new int[2];
        from.getLocationOnScreen(fromLocation);

        float newX = fromX + (fromLocation[0] - toLocation[0]);
        float newY = fromY + (fromLocation[1] - toLocation[1]);

        to.handleTouchEvent(event.getAction(), newX, newY);
    }

    private void ensureNotification() {
        ensureNotification(false);
    }

    private void ensureNotification(boolean force) {
        if(!force && mNotification != null) {
            return;
        }
        PendingIntent settingsPendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                0
        );

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("PaperLaunch")
                .setContentText(getString(mState.getIsActive() ? R.string.notification_content_active : R.string.notification_content_inactive))
                .setOngoing(true)
                .setLocalOnly(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(settingsPendingIntent)
        ;

        if(mState.getIsActive()) {
            Intent pauseIntent = new Intent(ACTION_PAUSE);
            pauseIntent.setClass(this, LauncherOverlayService.class);
            PendingIntent pendingPauseIntent = PendingIntent.getService(
                    this,
                    0,
                    pauseIntent,
                    0);
            builder.addAction(new Notification.Action(
                R.mipmap.ic_pause_black_24dp,
                getString(R.string.notification_pause),
                pendingPauseIntent
            ));
        } else {
            Intent playIntent = new Intent(ACTION_PLAY);
            playIntent.setClass(this, LauncherOverlayService.class);
            PendingIntent pendingPlayIntent = PendingIntent.getService(
                    this,
                    0,
                    playIntent,
                    0);
            builder.addAction(new Notification.Action(
                    R.mipmap.ic_play_arrow_black_24dp,
                    getString(R.string.notification_play),
                    pendingPlayIntent
            ));
        }

        Notification n = builder.build();
        if(mNotification != null) {
            n.when = mNotification.when;
        }
        mNotification = n;

        startForeground(NOTIFICATION_ID, n);

    }
}
