package de.devmil.paperlaunch;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import java.util.concurrent.TimeUnit;

import de.devmil.paperlaunch.config.UserSettings;
import de.devmil.paperlaunch.service.LauncherOverlayService;
import de.devmil.paperlaunch.view.utils.ViewUtils;
import de.devmil.paperlaunch.view.fragments.SettingsFragment;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class SettingsActivity extends Activity implements SettingsFragment.IActivationParametersChangedListener {

    private Toolbar mToolbar;
    private LinearLayout mActivationIndicatorContainer;
    private SettingsFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar)findViewById(R.id.activity_settings_toolbar);

        setActionBar(mToolbar);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.activity_settings_fragment_placeholder, mFragment = new SettingsFragment()).commit();

        updateActivationIndicator();

        subscribeToParametersChangedEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscription.unsubscribe();
    }

    private void updateActivationIndicator() {
        UserSettings us = new UserSettings(this);

        LauncherOverlayService.ActivationViewResult avr = LauncherOverlayService.addActivationViewToWindow(
                mActivationIndicatorContainer,
                this,
                (int)ViewUtils.getPxFromDip(this, us.getSensitivityDip()),
                (int)ViewUtils.getPxFromDip(this, us.getActivationOffsetPositionDip()),
                (int)ViewUtils.getPxFromDip(this, us.getActivationOffsetHeightDip()),
                us.isOnRightSide(),
                getResources().getColor(R.color.theme_accent)
        );

        mActivationIndicatorContainer = avr.container;

        avr.activationView.setElevation(ViewUtils.getPxFromDip(this, 3));

        LauncherOverlayService.notifyConfigChanged(this);
    }

    private Subscription mSubscription;

    private void subscribeToParametersChangedEvent() {
        mSubscription = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                mFragment.setOnActivationParametersChangedListener(new SettingsFragment.IActivationParametersChangedListener() {
                    @Override
                    public void onActivationParametersChanged() {
                        subscriber.onNext(true);
                    }
                });
            }
        })
        .debounce(100, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                updateActivationIndicator();
            }
        });
    }

    @Override
    public void onActivationParametersChanged() {
        updateActivationIndicator();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LauncherOverlayService.removeTouchReceiver(this, mActivationIndicatorContainer);
        mActivationIndicatorContainer = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateActivationIndicator();
    }
}
