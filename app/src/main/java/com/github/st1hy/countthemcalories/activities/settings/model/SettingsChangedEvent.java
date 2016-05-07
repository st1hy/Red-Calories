package com.github.st1hy.countthemcalories.activities.settings.model;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

import static android.content.SharedPreferences.*;

public abstract class SettingsChangedEvent {

    @Nullable
    public static SettingsChangedEvent from(@NonNull SettingsModel model, @NonNull String key) {
        switch (key) {
            case SettingsModel.PREFERRED_MASS_ENERGY_UNIT:
                return new EnergyUnit.Mass(model.getPreferredGravimetricUnit());
            case SettingsModel.PREFERRED_VOLUME_ENERGY_UNIT:
                return new EnergyUnit.Volume(model.getPreferredVolumetricUnit());
            default:
                return null;
        }
    }

    @NonNull
    public static Observable<SettingsChangedEvent> create(@NonNull final SettingsModel model) {

        return Observable.create(new Observable.OnSubscribe<SettingsChangedEvent>() {
            @Override
            public void call(final Subscriber<? super SettingsChangedEvent> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    final OnSharedPreferenceChangeListener preferenceChangeListener = new OnSharedPreferenceChangeListener() {
                        @Override
                        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                            SettingsChangedEvent event = SettingsChangedEvent.from(model, key);
                            if (event != null) {
                                subscriber.onNext(event);
                            }
                        }
                    };
                    final SharedPreferences preferences = model.getPreferences();
                    preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

                    subscriber.add(new MainThreadSubscription() {
                        @Override
                        protected void onUnsubscribe() {
                            preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
                        }
                    });
                }
            }
        });
    }

}
