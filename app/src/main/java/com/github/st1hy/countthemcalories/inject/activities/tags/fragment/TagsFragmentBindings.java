package com.github.st1hy.countthemcalories.inject.activities.tags.fragment;

import com.github.st1hy.countthemcalories.activities.tags.fragment.presenter.TagsDaoAdapter;
import com.github.st1hy.countthemcalories.activities.tags.fragment.view.TagsView;
import com.github.st1hy.countthemcalories.activities.tags.fragment.view.TagsViewImpl;
import com.github.st1hy.countthemcalories.activities.tags.fragment.viewholder.OnTagInteraction;
import com.github.st1hy.countthemcalories.activities.tags.fragment.viewholder.inject.TagComponentFactory;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class TagsFragmentBindings {

    @Binds
    public abstract TagsView provideView(TagsViewImpl view);

    @Binds
    public abstract TagComponentFactory tagComponent(TagsFragmentComponent component);

    @Binds
    public abstract OnTagInteraction onTagInteraction(TagsDaoAdapter adapter);

}
