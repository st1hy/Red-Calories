package com.github.st1hy.countthemcalories.inject;

import com.github.st1hy.countthemcalories.activities.settings.model.SettingsModel;
import com.github.st1hy.countthemcalories.core.headerpicture.HeaderPicturePickerUtils;
import com.github.st1hy.countthemcalories.database.DaoSession;
import com.github.st1hy.countthemcalories.inject.addingredient.AddIngredientTestComponent;
import com.github.st1hy.countthemcalories.inject.addmeal.AddMealTestComponent;
import com.github.st1hy.countthemcalories.inject.application.ApplicationComponent;
import com.github.st1hy.countthemcalories.inject.application.ApplicationModule;
import com.github.st1hy.countthemcalories.inject.common.ActivityModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationTestComponent extends ApplicationComponent {

    DaoSession getDaoSession();

    SettingsModel getSettingsModel();

    @Override
    AddIngredientTestComponent newAddIngredientActivityComponent(ActivityModule activityModule);

    @Override
    AddMealTestComponent newAddMealActivityComponent(ActivityModule activityModule);

    HeaderPicturePickerUtils testHeaderPicturePickerUtils();
}
