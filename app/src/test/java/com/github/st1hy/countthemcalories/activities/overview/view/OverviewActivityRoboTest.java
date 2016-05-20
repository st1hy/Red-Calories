package com.github.st1hy.countthemcalories.activities.overview.view;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.github.st1hy.countthemcalories.BuildConfig;
import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.addmeal.view.AddMealActivity;
import com.github.st1hy.countthemcalories.activities.ingredients.view.IngredientsActivityRoboTest;
import com.github.st1hy.countthemcalories.activities.mealdetail.view.MealDetailActivity;
import com.github.st1hy.countthemcalories.activities.settings.view.SettingsActivity;
import com.github.st1hy.countthemcalories.application.CaloriesCounterApplication;
import com.github.st1hy.countthemcalories.application.inject.ApplicationModule;
import com.github.st1hy.countthemcalories.application.inject.DbModelsModule;
import com.github.st1hy.countthemcalories.core.adapter.RxDaoRecyclerAdapter;
import com.github.st1hy.countthemcalories.core.inject.ApplicationTestComponent;
import com.github.st1hy.countthemcalories.core.inject.DaggerApplicationTestComponent;
import com.github.st1hy.countthemcalories.database.DaoSession;
import com.github.st1hy.countthemcalories.database.Ingredient;
import com.github.st1hy.countthemcalories.database.IngredientDao;
import com.github.st1hy.countthemcalories.database.Meal;
import com.github.st1hy.countthemcalories.database.MealDao;
import com.github.st1hy.countthemcalories.database.application.inject.DatabaseModule;
import com.github.st1hy.countthemcalories.database.parcel.MealParcel;
import com.github.st1hy.countthemcalories.testutils.RobolectricConfig;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenu;

import java.math.BigDecimal;

import rx.plugins.TestRxPlugins;
import timber.log.Timber;

import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = RobolectricConfig.sdk, packageName = RobolectricConfig.packageName)
public class OverviewActivityRoboTest {
    public static final Meal[] exampleMeals = new Meal[]{
            new Meal(1L, "Meal 1", Uri.EMPTY, DateTime.now()),
            new Meal(2L, "Meal 2", Uri.EMPTY, DateTime.now()),
            new Meal(3L, "Meal past", Uri.EMPTY, DateTime.now().minusDays(1)),
    };
    public static final Ingredient[] exampleIngredients = new Ingredient[] {
            new Ingredient(1L, new BigDecimal("30"), exampleMeals[0].getId(), IngredientsActivityRoboTest.exampleIngredients[0].getId()),
            new Ingredient(2L, new BigDecimal("20"), exampleMeals[0].getId(), IngredientsActivityRoboTest.exampleIngredients[1].getId()),
            new Ingredient(3L, new BigDecimal("70"), exampleMeals[1].getId(), IngredientsActivityRoboTest.exampleIngredients[1].getId()),
    };
    public static final Meal additionalMeal = new Meal(4L, "Meal 3", Uri.EMPTY, DateTime.now());
    public static final Ingredient additionalIngredient = new Ingredient(4L, new BigDecimal("80"), additionalMeal.getId(), IngredientsActivityRoboTest.exampleIngredients[0].getId());


    private OverviewActivity activity;
    private DaoSession session;

    private final Timber.Tree tree = new Timber.Tree() {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            System.out.println(tag + " " + message);
        }
    };

    @Before
    public void setup() {
        Timber.plant(tree);
        RxDaoRecyclerAdapter.debounceTime = 0;
        TestRxPlugins.registerImmediateHookIO();

        session = prepareDatabase();
        addMealsIngredients(session);

        activity = Robolectric.setupActivity(OverviewActivity.class);
    }

    public static DaoSession prepareDatabase() {
        CaloriesCounterApplication application = (CaloriesCounterApplication) RuntimeEnvironment.application;
        application.setComponent(DaggerApplicationTestComponent.builder()
                .applicationModule(new ApplicationModule(application))
                .databaseModule(new DatabaseModule())
                .dbModelsModule(new DbModelsModule())
                .build());
        ApplicationTestComponent component = (ApplicationTestComponent) application.getComponent();

        return component.getDaoSession();
    }

    public static void addMealsIngredients(DaoSession session) {
        IngredientsActivityRoboTest.addExampleIngredientsTagsAndJoin(session);
        IngredientDao ingredientDao = session.getIngredientDao();
        MealDao mealDao = session.getMealDao();
        ingredientDao.deleteAll();
        mealDao.deleteAll();

        mealDao.insertInTx(exampleMeals);
        ingredientDao.insertInTx(exampleIngredients);
        session.clear();
        ingredientDao.loadAll();
        mealDao.loadAll();
    }

    @After
    public void tearDown() throws Exception {
        Timber.uproot(tree);
        TestRxPlugins.reset();
        RxDaoRecyclerAdapter.debounceTime = 250;
    }

    @Test
    public void testDisplaysMeals() throws Exception {
        assertThat(activity.recyclerView.getChildCount(), equalTo(3));
        TextView name1 = (TextView) activity.recyclerView.getChildAt(0).findViewById(R.id.overview_item_name);
        assertThat(name1.getText().toString(), equalTo(exampleMeals[0].getName()));
        TextView name2 = (TextView) activity.recyclerView.getChildAt(1).findViewById(R.id.overview_item_name);
        assertThat(name2.getText().toString(), equalTo(exampleMeals[1].getName()));
    }

    @Test
    public void testAddMeal() {
        Assert.assertThat(activity.recyclerView.getChildCount(), equalTo(3));
        activity.fab.performClick();
        Intent resultIntent = shadowOf(activity).getNextStartedActivity();
        assertThat(resultIntent, equalTo(new Intent(activity, AddMealActivity.class)));
        session.getMealDao().insertInTx(additionalMeal);
        session.getIngredientDao().insertInTx(additionalIngredient);
        activity.presenter.onStop();
        activity.presenter.onStart();
        assertThat(activity.recyclerView.getChildCount(), equalTo(4));
        TextView name3 = (TextView) activity.recyclerView.getChildAt(2).findViewById(R.id.overview_item_name);
        assertThat(name3.getText().toString(), equalTo(additionalMeal.getName()));
    }

    @Test
    public void testOpenSettings() throws Exception {
        shadowOf(activity).onCreateOptionsMenu(new RoboMenu());
        assertTrue(shadowOf(activity).clickMenuItem(R.id.action_settings));
        Intent nextStartedActivity = shadowOf(activity).getNextStartedActivity();
        assertThat(nextStartedActivity, hasComponent(new ComponentName(activity, SettingsActivity.class)));
    }

    @Test
    public void testEditMeal() throws Exception {
        Assert.assertThat(activity.recyclerView.getChildCount(), equalTo(3));
        View item = activity.recyclerView.getChildAt(0).findViewById(R.id.overview_item_root);
        item.performClick();
        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertNotNull(intent);
        assertThat(intent, hasComponent(new ComponentName(activity, MealDetailActivity.class)));
        assertThat(intent, hasExtra(equalTo(MealDetailActivity.EXTRA_MEAL_PARCEL),
                any(MealParcel.class)));
        Intent result = new Intent();
        result.putExtra(MealDetailActivity.EXTRA_RESULT_MEAL_ID_LONG, exampleMeals[0].getId());
        shadowOf(activity).receiveResult(intent, MealDetailActivity.RESULT_EDIT, result);
        intent = shadowOf(activity).getNextStartedActivity();
        assertThat(intent, hasComponent(new ComponentName(activity, AddMealActivity.class)));
        assertThat(intent, hasExtra(equalTo(AddMealActivity.EXTRA_MEAL_PARCEL),
                any(MealParcel.class)));
    }


    @Test
    public void testRemoveMeal() throws Exception {
        Assert.assertThat(activity.recyclerView.getChildCount(), equalTo(3));
        View item = activity.recyclerView.getChildAt(0).findViewById(R.id.overview_item_root);
        item.performClick();
        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertNotNull(intent);
        assertThat(intent, hasComponent(new ComponentName(activity, MealDetailActivity.class)));
        assertThat(intent, hasExtra(equalTo(MealDetailActivity.EXTRA_MEAL_PARCEL),
                any(MealParcel.class)));
        Intent result = new Intent();
        result.putExtra(MealDetailActivity.EXTRA_RESULT_MEAL_ID_LONG, exampleMeals[0].getId());
        shadowOf(activity).receiveResult(intent, MealDetailActivity.RESULT_DELETE, result);
        Assert.assertThat(activity.recyclerView.getChildCount(), equalTo(2));
    }
}
