package com.github.st1hy.countthemcalories.activities.tags.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.tags.inject.DaggerTagsComponent;
import com.github.st1hy.countthemcalories.activities.tags.inject.TagsComponent;
import com.github.st1hy.countthemcalories.activities.tags.inject.TagsModule;
import com.github.st1hy.countthemcalories.activities.tags.presenter.TagsPresenter;
import com.github.st1hy.countthemcalories.core.rx.RxAlertDialog;
import com.github.st1hy.countthemcalories.core.ui.BaseActivity;
import com.github.st1hy.countthemcalories.core.ui.Visibility;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.view.RxView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

public class TagsActivity extends BaseActivity implements TagsView {
    public static final String ACTION_PICK_TAG = "pick tag";
    TagsComponent component;

    @Inject
    TagsPresenter presenter;
    @Inject
    RecyclerView.Adapter adapter;

    @Bind(R.id.tags_toolbar)
    Toolbar toolbar;
    @Bind(R.id.tags_recycler)
    RecyclerView recyclerView;
    @Bind(R.id.tags_add_new)
    FloatingActionButton fab;
    @Bind(R.id.tags_no_tags_button)
    View notTagsButton;
    @Bind(R.id.tags_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @NonNull
    protected TagsComponent getComponent() {
        if (component == null) {
            component = DaggerTagsComponent.builder()
                    .applicationComponent(getAppComponent())
                    .tagsModule(new TagsModule(this))
                    .build();
        }
        return component;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tags_activity);
        ButterKnife.bind(this);
        getComponent().inject(this);
        setSupportActionBar(toolbar);
        assertNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        presenter.onAddTagClicked(Observable.merge(RxView.clicks(fab), RxView.clicks(notTagsButton)));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        presenter.onRefresh(RxSwipeRefreshLayout.refreshes(swipeRefreshLayout));
    }

    @NonNull
    @Override
    public Observable<String> showEditTextDialog(int newTagDialogTitle) {
        final RxAlertDialog rxAlertDialog = RxAlertDialog.Builder.with(this)
                .title(newTagDialogTitle)
                .customView(R.layout.tags_new_tag_dialog_content)
                .positiveButton(android.R.string.ok)
                .show();
        return rxAlertDialog.observePositiveClick()
                .map(new Func1<Void, String>() {
                    @Override
                    public String call(Void aVoid) {
                        EditText text = (EditText) rxAlertDialog.getCustomView();
                        return assertNotNull(text).getText().toString();
                    }
                });
    }

    @Override
    public void setNoTagsButtonVisibility(@NonNull Visibility visibility) {
        Timber.d("No tags: %s", visibility);
        //noinspection WrongConstant
        notTagsButton.setVisibility(visibility.getVisibility());
    }

    @Override
    public void setDataRefreshing(boolean isRefreshing) {
        swipeRefreshLayout.setRefreshing(isRefreshing);
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.onStop();
    }
}
