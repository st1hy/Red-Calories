package com.github.st1hy.countthemcalories.activities.addingredient.fragment.inject;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.github.st1hy.countthemcalories.activities.addingredient.fragment.model.AddIngredientModel;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.model.EnergyConverter;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.model.IngredientTagsModel;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.presenter.AddIngredientPresenter;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.presenter.AddIngredientPresenterImp;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.view.AddIngredientFragment;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.view.AddIngredientView;
import com.github.st1hy.countthemcalories.activities.addingredient.view.AddIngredientScreen;
import com.github.st1hy.countthemcalories.core.FragmentDepends;
import com.github.st1hy.countthemcalories.core.inject.PerFragment;
import com.github.st1hy.countthemcalories.core.permissions.PermissionsHelper;
import com.github.st1hy.countthemcalories.core.withpicture.imageholder.ImageHolderDelegate;
import com.github.st1hy.countthemcalories.core.withpicture.imageholder.NewImageHolderDelegate;
import com.github.st1hy.countthemcalories.database.IngredientTemplate;
import com.github.st1hy.countthemcalories.database.JointIngredientTag;
import com.github.st1hy.countthemcalories.database.Tag;
import com.github.st1hy.countthemcalories.database.unit.AmountUnitType;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.parceler.Parcels;

import java.math.BigDecimal;
import java.util.ArrayList;

import javax.inject.Named;
import javax.inject.Provider;

import dagger.Module;
import dagger.Provides;

@Module
public class AddIngredientFragmentModule {

    public static final String ARG_AMOUNT_UNIT = "amount unit type";
    public static final String ARG_EDIT_REQUEST_ID_LONG = "edit ingredient extra request id";
    public static final String ARG_EDIT_INGREDIENT_PARCEL = "edit ingredient extra parcel";
    public static final String ARG_EXTRA_NAME = "extra ingredient name";
    private static final Function<JointIngredientTag, Tag> JOINT_INGREDIENT_TO_TAG_FUNCTION = new Function<JointIngredientTag, Tag>() {
        @Nullable
        @Override
        public Tag apply(JointIngredientTag input) {
            return input.getTag();
        }
    };


    private final AddIngredientFragment fragment;
    private final Bundle savedState;

    public AddIngredientFragmentModule(AddIngredientFragment fragment, @Nullable Bundle savedState) {
        this.fragment = fragment;
        this.savedState = savedState;
    }

    @Provides
    public AddIngredientView provideView() {
        return fragment;
    }

    @Provides
    public AddIngredientScreen provideScreen() {
        return FragmentDepends.checkIsSubclass(fragment.getActivity(), AddIngredientScreen.class);
    }

    @Provides
    @PerFragment
    public AddIngredientPresenter providePresenter(AddIngredientPresenterImp presenter) {
        return presenter;
    }

    @Provides
    @PerFragment
    public IngredientTagsModel provideIngredientTagModel(@Nullable @Named("savedState") Bundle savedState,
                                                         @Nullable IngredientTemplate template) {
        if (savedState != null) {
            Parcelable parcelable = savedState.getParcelable(IngredientTagsModel.SAVED_TAGS_MODEL);
            return Parcels.unwrap(parcelable);
        } else {
            ArrayList<Tag> tags;
            if (template != null) {
                tags = Lists.newArrayList(Collections2.transform(template.getTags(), JOINT_INGREDIENT_TO_TAG_FUNCTION));
            } else {
                tags = new ArrayList<>(5);
            }
            return new IngredientTagsModel(tags);
        }
    }

    @Provides
    @Nullable
    @Named("savedState")
    public Bundle provideSavedStateBundle() {
        return savedState;
    }

    @Provides
    @Named("arguments")
    public Bundle provideArguments() {
        return fragment.getArguments();
    }

    @Provides
    public Resources provideResources() {
        return fragment.getResources();
    }

    @Provides
    public AmountUnitType provideAmountUnitType(@Named("arguments") Bundle arguments) {
        return (AmountUnitType) arguments.getSerializable(ARG_AMOUNT_UNIT);
    }

    @Provides
    @Nullable
    public IngredientTemplate provideIngredientTemplate(@Named("arguments") Bundle arguments) {
        return arguments.getParcelable(ARG_EDIT_INGREDIENT_PARCEL);
    }

    @Provides
    public FragmentActivity provideFragmentActivity() {
        return fragment.getActivity();
    }

    @Provides
    public ImageHolderDelegate provideImageHolderDelegate(Picasso picasso,
                                                          PermissionsHelper permissionsHelper,
                                                          Provider<ImageView> image) {
        return new NewImageHolderDelegate(picasso, permissionsHelper, image);
    }


    @Provides
    public ImageView provideImageViewProvider() {
        return fragment.getImageView();
    }

    @Provides
    public String provideInitialName(@Named("arguments") Bundle arguments) {
        return arguments.getString(ARG_EXTRA_NAME, "");
    }

    @Provides
    @PerFragment
    public AddIngredientModel provideIngredientModel(@Nullable IngredientTemplate templateSource,
                                                     @NonNull String name,
                                                     @NonNull AmountUnitType amountUnitType,
                                                     @NonNull EnergyConverter energyConverter) {
        if (savedState != null) {
            Parcelable parcelable = savedState.getParcelable(AddIngredientModel.SAVED_INGREDIENT_MODEL);
            return Parcels.unwrap(parcelable);
        } else {
            String energyValue;
            Uri imageUri;
            DateTime creationDate;
            if (templateSource != null) {
                name = templateSource.getName();
                amountUnitType = templateSource.getAmountType();
                final BigDecimal energyDensityAmount = templateSource.getEnergyDensityAmount();
                energyValue = energyConverter.fromDatabaseToCurrent(amountUnitType, energyDensityAmount)
                        .toPlainString();
                imageUri = templateSource.getImageUri();
                creationDate = templateSource.getCreationDate();
            } else {
                energyValue = "";
                imageUri = Uri.EMPTY;
                creationDate = null;
            }
            return new AddIngredientModel(name, amountUnitType, energyValue, imageUri, creationDate);
        }
    }
}
