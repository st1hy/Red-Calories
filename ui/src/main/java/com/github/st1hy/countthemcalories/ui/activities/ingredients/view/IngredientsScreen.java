package com.github.st1hy.countthemcalories.ui.activities.ingredients.view;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.github.st1hy.countthemcalories.ui.activities.ingredients.model.AddIngredientParams;
import com.github.st1hy.countthemcalories.ui.contract.IngredientTemplate;

import rx.Observable;

public interface IngredientsScreen {

    @NonNull
    @CheckResult
    Observable<Void> getOnAddIngredientClickedObservable();

    @NonNull
    @CheckResult
    Observable.Transformer<AddIngredientParams, IngredientTemplate> addNewIngredient();

    void editIngredientTemplate(long requestID, IngredientTemplate ingredientTemplate);

    void onIngredientSelected(@NonNull IngredientTemplate ingredientTemplate);

    void addToNewMeal(@NonNull IngredientTemplate ingredientTemplate);
}
