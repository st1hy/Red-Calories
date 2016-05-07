package com.github.st1hy.countthemcalories.activities.ingredients.view;

import android.content.ComponentName;
import android.content.Intent;

import com.github.st1hy.countthemcalories.BuildConfig;
import com.github.st1hy.countthemcalories.activities.addingredient.view.AddIngredientActivity;
import com.github.st1hy.countthemcalories.activities.tags.presenter.TagsPresenterImp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import rx.plugins.TestRxPlugins;
import timber.log.Timber;

import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class IngredientsActivityRoboTest {

    private IngredientsActivity activity;

    private final Timber.Tree tree = new Timber.Tree() {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            System.out.println(tag +" " + message);
        }
    };

    @Before
    public void setup() {
        Timber.plant(tree);
        TagsPresenterImp.debounceTime = 0;
        TestRxPlugins.registerImmediateHookIO();
        activity = Robolectric.setupActivity(IngredientsActivity.class);
    }

    @After
    public void tearDown() throws Exception {
        Timber.uproot(tree);
        TestRxPlugins.reset();
        TagsPresenterImp.debounceTime = 250;
    }

    @Test
    public void testActivityStart() {
        assertThat(activity, notNullValue());
        assertThat(activity.presenter, notNullValue());
        assertThat(activity.getComponent(), notNullValue());
        assertThat(activity.component, notNullValue());
    }

    @Test
    public void testAddIngredient() throws Exception {
        activity.fab.performClick();

        Intent request = shadowOf(activity).peekNextStartedActivity();
        assertThat(request, hasComponent(new ComponentName(activity, AddIngredientActivity.class)));
    }
}