package com.github.st1hy.countthemcalories.activities.ingredientdetail.fragment.inject;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.github.st1hy.countthemcalories.activities.ingredientdetail.fragment.model.IngredientDetailModel;
import com.github.st1hy.countthemcalories.activities.ingredientdetail.fragment.presenter.IngredientDetailPresenter;
import com.github.st1hy.countthemcalories.activities.ingredientdetail.fragment.presenter.IngredientDetailPresenterImpl;
import com.github.st1hy.countthemcalories.activities.ingredientdetail.fragment.view.IngredientDetailFragment;
import com.github.st1hy.countthemcalories.activities.ingredientdetail.fragment.view.IngredientDetailView;
import com.github.st1hy.countthemcalories.activities.ingredientdetail.view.IngredientDetailScreen;
import com.github.st1hy.countthemcalories.core.inject.PerFragment;
import com.github.st1hy.countthemcalories.core.headerpicture.imageholder.ImageHolderDelegate;
import com.github.st1hy.countthemcalories.core.headerpicture.imageholder.WithoutPlaceholderImageHolderDelegate;
import com.github.st1hy.countthemcalories.database.Ingredient;
import com.google.common.base.Preconditions;

import org.parceler.Parcels;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

import static com.github.st1hy.countthemcalories.core.Utils.checkIsSubclass;

@Module
public class IngredientsDetailFragmentModule {

    public static final String EXTRA_INGREDIENT= "ingredient details ingredient";
    public static final String EXTRA_INGREDIENT_ID_LONG = "ingredient details extra id long";

    private final IngredientDetailFragment fragment;
    @Nullable
    private final Bundle savedState;

    public IngredientsDetailFragmentModule(@NonNull IngredientDetailFragment fragment,
                                           @Nullable Bundle savedState) {
        this.fragment = fragment;
        this.savedState = savedState;
    }

    @Provides
    @PerFragment
    public IngredientDetailPresenter providePresenter(IngredientDetailPresenterImpl presenter) {
        return presenter;
    }

    @Provides
    public IngredientDetailView provideView() {
        return fragment;
    }

    @Provides
    public Resources provideResources() {
        return fragment.getResources();
    }

    @Provides
    @Named("savedState")
    @Nullable
    public Bundle provideSavedState() {
        return savedState;
    }

    @Provides
    @Named("arguments")
    public Bundle provideArguments() {
        return Preconditions.checkNotNull(fragment.getArguments());
    }

    @Provides
    public IngredientDetailScreen provideScreen() {
        return checkIsSubclass(fragment.getActivity(), IngredientDetailScreen.class);
    }

    @Provides
    public FragmentActivity provideFragmentActivity() {
        return fragment.getActivity();
    }

    @Provides
    public ImageView provideImageView(IngredientDetailView view) {
        return view.getImageView();
    }

    @Provides
    @PerFragment
    public Ingredient provideIngredient(@Nullable @Named("savedState") Bundle savedState,
                                        @Named("arguments") Bundle arguments) {
        if (savedState != null) {
            return Parcels.unwrap(savedState.getParcelable(IngredientDetailModel.SAVED_INGREDIENT_MODEL));
        } else {
            Ingredient ingredient = Parcels.unwrap(arguments.getParcelable(EXTRA_INGREDIENT));
            Preconditions.checkNotNull(ingredient, "Missing ingredient!");
            return ingredient;
        }
    }

    @Provides
    public long provideIngredientId(@Named("arguments") Bundle arguments) {
        return arguments.getLong(EXTRA_INGREDIENT_ID_LONG, -1L);
    }

    @Provides
    public ImageHolderDelegate provideImageHolderDelegate(WithoutPlaceholderImageHolderDelegate holderDelegate) {
        return holderDelegate;
    }

}
