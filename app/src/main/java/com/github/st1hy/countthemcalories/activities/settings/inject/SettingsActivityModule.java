package com.github.st1hy.countthemcalories.activities.settings.inject;

import android.support.annotation.NonNull;

import com.github.st1hy.countthemcalories.activities.settings.fragment.presenter.SettingsDrawerPresenter;
import com.github.st1hy.countthemcalories.activities.settings.view.SettingsActivity;
import com.github.st1hy.countthemcalories.core.drawer.presenter.DrawerPresenter;
import com.github.st1hy.countthemcalories.core.drawer.view.DrawerView;
import com.github.st1hy.countthemcalories.core.inject.PerActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingsActivityModule {

    private final SettingsActivity activity;

    public SettingsActivityModule(@NonNull SettingsActivity activity) {
        this.activity = activity;
    }

    @PerActivity
    @Provides
    public DrawerPresenter provideDrawerPresenter(DrawerView drawerView) {
        return new SettingsDrawerPresenter(drawerView);
    }

    @Provides
    public DrawerView provideDrawerView() {
        return activity;
    }
}
