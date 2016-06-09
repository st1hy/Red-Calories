package com.github.st1hy.countthemcalories.activities.overview.model.commands;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.github.st1hy.countthemcalories.activities.overview.model.RxMealsDatabaseModel;
import com.github.st1hy.countthemcalories.core.command.CommandResponse;
import com.github.st1hy.countthemcalories.core.command.InsertResult;
import com.github.st1hy.countthemcalories.core.command.UndoInvalidator;
import com.github.st1hy.countthemcalories.database.Ingredient;
import com.github.st1hy.countthemcalories.database.Meal;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import rx.Observable;

public class MealsDatabaseCommands {
    private final RxMealsDatabaseModel databaseModel;

    private final AtomicReference<CommandResponse> responseRef = new AtomicReference<>();
    private final UndoInvalidator<Meal, Void> insertInvalidator = new UndoInvalidator<>(responseRef);
    private final UndoInvalidator<Void, Meal> deleteInvalidator = new UndoInvalidator<>(responseRef);

    @Inject
    public MealsDatabaseCommands(@NonNull RxMealsDatabaseModel databaseModel) {
        this.databaseModel = databaseModel;
    }

    @NonNull
    public Observable<CommandResponse<Void, Meal>> delete(@NonNull Meal meal) {
        return new DeleteCommand(databaseModel, this, meal)
                .executeInTx()
                .compose(deleteInvalidator);
    }

    @NonNull
    public Observable<CommandResponse<Meal, Void>> insert(@NonNull Meal meal,
                                                                    @NonNull List<Ingredient> ingredients) {
        return new InsertCommand(databaseModel, this, meal, ingredients)
                .executeInTx()
                .compose(insertInvalidator);
    }
}
