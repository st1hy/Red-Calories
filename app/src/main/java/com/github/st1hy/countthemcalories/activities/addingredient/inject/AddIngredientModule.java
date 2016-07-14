package com.github.st1hy.countthemcalories.activities.addingredient.inject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.model.AddIngredientType;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.view.AddIngredientFragment;
import com.github.st1hy.countthemcalories.activities.addingredient.view.AddIngredientActivity;
import com.github.st1hy.countthemcalories.database.unit.AmountUnitType;

import dagger.Module;
import dagger.Provides;

import static com.github.st1hy.countthemcalories.activities.addingredient.fragment.inject.AddIngredientFragmentModule.ARG_AMOUNT_UNIT;
import static com.github.st1hy.countthemcalories.activities.addingredient.fragment.inject.AddIngredientFragmentModule.ARG_EDIT_INGREDIENT_PARCEL;
import static com.github.st1hy.countthemcalories.activities.addingredient.fragment.inject.AddIngredientFragmentModule.ARG_EDIT_REQUEST_ID_LONG;

@Module
public class AddIngredientModule {
    private final AddIngredientActivity activity;

    public AddIngredientModule(@NonNull AddIngredientActivity activity) {
        this.activity = activity;
    }

    @Provides
    public AddIngredientFragment provideContent(Bundle arguments, FragmentManager fragmentManager) {
        final String tag = "add ingredient content";

        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new AddIngredientFragment();
            fragment.setArguments(arguments);

            fragmentManager.beginTransaction()
                    .add(R.id.add_ingredient_content_frame, fragment, tag)
                    .setTransitionStyle(FragmentTransaction.TRANSIT_NONE)
                    .commit();
        }
        return (AddIngredientFragment) fragment;
    }

    @Provides
    public Bundle provideArguments(Intent intent, AmountUnitType unitType) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARG_EDIT_INGREDIENT_PARCEL,
                intent.getParcelableExtra(ARG_EDIT_INGREDIENT_PARCEL));
        arguments.putSerializable(ARG_AMOUNT_UNIT, unitType);
        arguments.putLong(ARG_EDIT_REQUEST_ID_LONG,
                intent.getLongExtra(ARG_EDIT_REQUEST_ID_LONG, -1L));
        return arguments;
    }

    @Provides
    public FragmentManager provideFragmentManager() {
        return activity.getSupportFragmentManager();
    }

    @Provides
    public Intent provideIntent() {
        return activity.getIntent();
    }

    @Provides
    public AmountUnitType provideUnitType(Intent intent) {
        String action = intent.getAction();
        if (AddIngredientType.DRINK.getAction().equals(action)) {
            return AmountUnitType.VOLUME;
        } else if (AddIngredientType.MEAL.getAction().equals(action)) {
            return AmountUnitType.MASS;
        }
        return AmountUnitType.MASS;
    }

}
