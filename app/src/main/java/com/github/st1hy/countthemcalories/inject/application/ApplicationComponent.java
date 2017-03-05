package com.github.st1hy.countthemcalories.inject.application;

import com.github.st1hy.countthemcalories.application.CaloriesCounterApplication;
import com.github.st1hy.countthemcalories.activities.addingredient.inject.AddIngredientActivityComponentFactory;
import com.github.st1hy.countthemcalories.activities.addmeal.inject.AddMealActivityComponentFactory;
import com.github.st1hy.countthemcalories.inject.activities.ingredientdetail.IngredientDetailActivityComponentFactory;
import com.github.st1hy.countthemcalories.inject.activities.ingredients.IngredientsActivityComponentFactory;
import com.github.st1hy.countthemcalories.inject.activities.mealdetail.MealDetailActivityComponentFactory;
import com.github.st1hy.countthemcalories.inject.activities.overview.OverviewActivityComponentFactory;
import com.github.st1hy.countthemcalories.inject.activities.settings.SettingsActivityComponentFactory;
import com.github.st1hy.countthemcalories.inject.activities.tags.TagsActivityComponentFactory;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent extends AddMealActivityComponentFactory,
        MealDetailActivityComponentFactory,
        IngredientDetailActivityComponentFactory,
        SettingsActivityComponentFactory,
        OverviewActivityComponentFactory,
        AddIngredientActivityComponentFactory,
        IngredientsActivityComponentFactory,
        TagsActivityComponentFactory {

    void inject(CaloriesCounterApplication application);

}
