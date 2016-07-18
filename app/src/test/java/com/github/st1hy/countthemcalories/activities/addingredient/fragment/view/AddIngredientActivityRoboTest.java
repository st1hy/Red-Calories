package com.github.st1hy.countthemcalories.activities.addingredient.fragment.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.st1hy.countthemcalories.BuildConfig;
import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.presenter.AddIngredientPresenter;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.presenter.AddIngredientPresenterImp;
import com.github.st1hy.countthemcalories.activities.addingredient.view.AddIngredientActivity;
import com.github.st1hy.countthemcalories.activities.overview.fragment.view.OverviewActivityRoboTest;
import com.github.st1hy.countthemcalories.activities.tags.view.TagsActivity;
import com.github.st1hy.countthemcalories.core.baseview.TestPermissionHelper;
import com.github.st1hy.countthemcalories.database.DaoSession;
import com.github.st1hy.countthemcalories.database.IngredientTemplate;
import com.github.st1hy.countthemcalories.database.IngredientTemplateDao;
import com.github.st1hy.countthemcalories.database.JointIngredientTag;
import com.github.st1hy.countthemcalories.database.JointIngredientTagDao;
import com.github.st1hy.countthemcalories.database.Tag;
import com.github.st1hy.countthemcalories.database.TagDao;
import com.github.st1hy.countthemcalories.testutils.RobolectricConfig;
import com.github.st1hy.countthemcalories.testutils.TimberUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import java.math.BigDecimal;
import java.util.List;

import rx.plugins.TestRxPlugins;
import timber.log.Timber;

import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static com.github.st1hy.countthemcalories.activities.addingredient.fragment.model.AddIngredientModel.IngredientTypeCreateException.ErrorType.NO_NAME;
import static com.github.st1hy.countthemcalories.activities.addingredient.fragment.model.AddIngredientModel.IngredientTypeCreateException.ErrorType.ZERO_VALUE;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = RobolectricConfig.sdk, packageName = RobolectricConfig.packageName)
public class AddIngredientActivityRoboTest {
    public static Tag[] exampleTags = new Tag[]{new Tag(1L, "Test tag"), new Tag(2L, "Tag2"), new Tag(3L, "meal")};

    private AddIngredientActivity activity;
    private AddIngredientFragment fragment;
    private DaoSession daoSession;

    @Before
    public void setup() {
        Timber.plant(TimberUtils.ABOVE_WARN);
        TestRxPlugins.registerImmediateHookIO();
        daoSession = OverviewActivityRoboTest.prepareDatabase();
        prepareDb();
        activity = Robolectric.setupActivity(AddIngredientActivity.class);
        fragment = (AddIngredientFragment) activity.getSupportFragmentManager()
                .findFragmentByTag("add ingredient content");
    }

    private void prepareDb() {
        TagDao tagDao = daoSession.getTagDao();
        tagDao.deleteAll();
        tagDao.insertOrReplaceInTx(exampleTags);
        assertEquals(3, tagDao.loadAll().size());
        IngredientTemplateDao templateDao = daoSession.getIngredientTemplateDao();
        templateDao.deleteAll();
        assertEquals(0, templateDao.loadAll().size());
        JointIngredientTagDao jointIngredientTagDao = daoSession.getJointIngredientTagDao();
        jointIngredientTagDao.deleteAll();
        assertEquals(0, jointIngredientTagDao.loadAll().size());
    }

    @After
    public void tearDown() throws Exception {
        Timber.uprootAll();
        TestRxPlugins.reset();
    }

    @SuppressLint("SetTextI18n")
    @Test
    public void testSaveButtonAction() throws Exception {
        fragment.name.setText("Name");
        fragment.energyDensityValue.setText("300.6");

        ShadowActivity shadowActivity = shadowOf(activity);
        shadowActivity.onCreateOptionsMenu(new RoboMenu(activity));
        shadowActivity.clickMenuItem(R.id.action_save);

        assertTrue(shadowActivity.isFinishing());
        assertThat(shadowActivity.getResultCode(), equalTo(Activity.RESULT_OK));

        List<IngredientTemplate> list = daoSession.getIngredientTemplateDao().loadAll();
        assertThat(list.size(), equalTo(1));
    }

    @Test
    public void testImageButton() throws Exception {
        checkNotNull(activity.findViewById(R.id.add_ingredient_image)).performClick();
        TestPermissionHelper.grantPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        ShadowAlertDialog shadowAlertDialog = shadowOf(RuntimeEnvironment.application).getLatestAlertDialog();
        assertThat(shadowAlertDialog, notNullValue());
        assertThat(shadowAlertDialog.getTitle().toString(), equalTo(activity.getString(R.string.add_ingredient_image_select_title)));
        assertThat(shadowAlertDialog.getItems(), arrayWithSize(2));
        shadowAlertDialog.clickOnItem(0);
        assertThat(shadowOf(activity).getNextStartedActivity(), hasAction(Intent.ACTION_CHOOSER));
    }

    @Test
    public void testSaveInstance() throws Exception {
        Bundle bundle = new Bundle();
        fragment.onSaveInstanceState(bundle);
        assertThat(bundle.isEmpty(), equalTo(false));
    }

    @Test
    public void testOnStop() throws Exception {
        AddIngredientPresenter presenterMock = Mockito.mock(AddIngredientPresenterImp.class);
        fragment.presenter = presenterMock;
        fragment.onStop();
        verify(presenterMock).onStop();
    }

    @Test
    public void testAddRemoveTag() throws Exception {
        assertThat(fragment.tagsRecycler.getChildCount(), equalTo(1));

        Tag tag = addTag(0);

        assertThat(fragment.tagsRecycler.getChildCount(), equalTo(2));
        View tagView = fragment.tagsRecycler.getChildAt(0);
        TextView textView = (TextView) tagView.findViewById(R.id.add_ingredient_category_name);
        assertThat(textView.getText().toString(), equalTo(tag.getName()));

        tagView.findViewById(R.id.add_ingredient_category_remove).performClick();
        assertThat(fragment.tagsPresenter.getItemCount(), equalTo(1));
    }

    private Tag addTag(int position) {
        int addTagPos = fragment.tagsRecycler.getChildCount() - 1;
        fragment.tagsRecycler.getChildAt(addTagPos).findViewById(R.id.add_ingredient_category_add).performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        ShadowActivity.IntentForResult activityForResult = shadowActivity.getNextStartedActivityForResult();
        assertThat(activityForResult.intent, hasAction(TagsActivity.ACTION_PICK_TAG));
        assertThat(activityForResult.intent, hasComponent(new ComponentName(activity, TagsActivity.class)));

        final Tag tag = daoSession.getTagDao().loadAll().get(position);

        Intent result = new Intent();
        result.putExtra(TagsActivity.EXTRA_TAG_ID, tag.getId());
        result.putExtra(TagsActivity.EXTRA_TAG_NAME, tag.getName());
        shadowActivity.receiveResult(activityForResult.intent, Activity.RESULT_OK, result);
        return tag;
    }

    @SuppressLint("SetTextI18n")
    @Test
    public void testSaveWithTags() throws Exception {
        final String name = "Name";
        final String value = "300.6"; //default in settings model kcal / 100 g
        fragment.name.setText(name);
        fragment.energyDensityValue.setText(value);

        Tag tag = addTag(0);
        Tag tag1 = addTag(1);

        ShadowActivity shadowActivity = shadowOf(activity);
        shadowActivity.onCreateOptionsMenu(new RoboMenu(activity));
        shadowActivity.clickMenuItem(R.id.action_save);

        assertTrue(shadowActivity.isFinishing());
        assertThat(shadowActivity.getResultCode(), equalTo(Activity.RESULT_OK));

        List<IngredientTemplate> list = daoSession.getIngredientTemplateDao().loadAll();
        assertThat(list.size(), equalTo(1));
        IngredientTemplate template = list.get(0);
        assertThat(template.getId(), notNullValue());
        assertThat(template.getName(), equalTo(name));
        BigDecimal energyDensity = template.getEnergyDensityAmount();
        assertThat(energyDensity.toPlainString(), equalTo("12.577104")); //default in database kJ / g double precision
        List<JointIngredientTag> tags = template.getTags();
        assertThat(tags, hasSize(2));
        assertThat(tags.get(0).getTag().getId(), equalTo(tag.getId()));
        assertThat(tags.get(0).getTag().getName(), equalTo(tag.getName()));
        assertThat(tags.get(1).getTag().getId(), equalTo(tag1.getId()));
        assertThat(tags.get(1).getTag().getName(), equalTo(tag1.getName()));
    }

    @Test
    public void testNameError() throws Exception {
        assertThat(fragment.name.getError(), nullValue());

        fragment.name.setText("");
        fragment.energyDensityValue.setText("12");

        assertThat(fragment.name.getError().toString(), equalTo(activity.getString(
                NO_NAME.getErrorResId()
        )));
        assertTrue(fragment.name.hasFocus());
        assertThat(shadowOf(activity).isFinishing(), equalTo(false));
    }

    @Test
    public void testValueError() throws Exception {
        assertThat(fragment.energyDensityValue.getError(), nullValue());

        fragment.name.setText("Name");
        fragment.energyDensityValue.setText("0000");

        assertThat(fragment.energyDensityValue.getError().toString(), equalTo(activity.getString(
                ZERO_VALUE.getErrorResId()
        )));
        assertTrue(fragment.energyDensityValue.hasFocus());
        assertThat(shadowOf(activity).isFinishing(), equalTo(false));
    }

    @Test
    public void testGetImageView() throws Exception {
        assertThat(fragment.getImageView(), equalTo(activity.findViewById(R.id.add_ingredient_image)));
    }

    @Test
    public void testSearchIngredientInTheInternet() throws Exception {
        fragment.name.setText("Eggs");
        checkNotNull(fragment.getView()).findViewById(R.id.add_ingredient_name_search)
                .performClick();
        Intent nextStartedActivity = shadowOf(activity).getNextStartedActivity();
        assertThat(nextStartedActivity, allOf(hasAction(Intent.ACTION_VIEW),
                hasData("https://google.com/#q=Eggs+calories")));
    }
}