package com.github.st1hy.countthemcalories.inject.activities.mealdetail.fragment;

import com.github.st1hy.countthemcalories.activities.mealdetail.fragment.MealDetailFragment;
import com.github.st1hy.countthemcalories.inject.PerFragment;
import com.github.st1hy.countthemcalories.inject.core.PermissionModule;
import com.github.st1hy.countthemcalories.inject.core.RecyclerViewAdapterDelegateModule;

import dagger.Subcomponent;

@PerFragment
@Subcomponent(modules = {
        MealDetailsModule.class,
        PermissionModule.class,
        RecyclerViewAdapterDelegateModule.class
})
public interface MealDetailComponent {

    void inject(MealDetailFragment fragment);
}
