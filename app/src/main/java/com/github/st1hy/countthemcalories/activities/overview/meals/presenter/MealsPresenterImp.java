package com.github.st1hy.countthemcalories.activities.overview.meals.presenter;

import android.support.annotation.NonNull;

import com.github.st1hy.countthemcalories.activities.overview.mealpager.AddMealController;
import com.github.st1hy.countthemcalories.activities.overview.meals.model.CurrentDayModel;
import com.github.st1hy.countthemcalories.core.adapter.delegate.RecyclerViewAdapterDelegate;
import com.github.st1hy.countthemcalories.inject.PerFragment;

import org.joda.time.DateTime;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

@PerFragment
public class MealsPresenterImp implements MealsPresenter {

    @NonNull
    private final MealsAdapter adapter;
    @NonNull
    private final RecyclerViewAdapterDelegate adapterDelegate;
    @Inject
    AddMealController addMealController;
    @Inject
    CurrentDayModel currentDayModel;

    private final CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public MealsPresenterImp(@NonNull MealsAdapter adapter,
                             @NonNull RecyclerViewAdapterDelegate adapterDelegate) {
        this.adapter = adapter;
        this.adapterDelegate = adapterDelegate;
    }

    @Override
    public void onStart() {
        adapterDelegate.onStart();
        adapter.onStart();
        DateTime currentDay = currentDayModel.getCurrentDay();
        subscriptions.add(
                addMealController.getAddNewMealObservable(currentDay)
                        .subscribe(aVoid -> addMealController.addNewMeal(currentDay))
        );
    }

    @Override
    public void onStop() {
        adapterDelegate.onStop();
        adapter.onStop();
        subscriptions.clear();
    }

}
