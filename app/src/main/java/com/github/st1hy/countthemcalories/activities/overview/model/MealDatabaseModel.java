package com.github.st1hy.countthemcalories.activities.overview.model;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.github.st1hy.countthemcalories.core.rx.RxDatabaseModel;
import com.github.st1hy.countthemcalories.database.DaoSession;
import com.github.st1hy.countthemcalories.database.Ingredient;
import com.github.st1hy.countthemcalories.database.IngredientDao;
import com.github.st1hy.countthemcalories.database.Meal;
import com.github.st1hy.countthemcalories.database.MealDao;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Provider;

import dagger.Lazy;
import dagger.internal.DoubleCheckLazy;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.CursorQuery;
import de.greenrobot.dao.query.Query;
import rx.Observable;

public class MealDatabaseModel extends RxDatabaseModel<Meal> {

    private final Lazy<MealDao> mealDaoLazy;
    private Query<Meal> filteredByDateSortedByName;

    public MealDatabaseModel(@NonNull Lazy<DaoSession> session) {
        super(session);
        mealDaoLazy = DoubleCheckLazy.create(new Provider<MealDao>() {
            @Override
            public MealDao get() {
                return session().getMealDao();
            }
        });
    }

    private MealDao dao() {
        return mealDaoLazy.get();
    }

    @Override
    public void performReadEntity(@NonNull Cursor cursor, @NonNull Meal output) {
        dao().readEntity(cursor, output, 0);
    }

    @NonNull
    public Observable<Void> addNew(@NonNull Meal meal, Collection<Ingredient> ingredients) {
        return fromDatabaseTask(addNewCall(meal, ingredients));
    }

    @NonNull
    private Callable<Void> addNewCall(final Meal meal, final Collection<Ingredient> ingredients) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                performInsert(meal);
                IngredientDao ingredientDao = session().getIngredientDao();
                for (Ingredient ingredient : ingredients) {
                    ingredient.setPartOfMeal(meal);
                    ingredientDao.insert(ingredient);
                }
                meal.resetIngredients();
                meal.getIngredients();
                return null;
            }
        };
    }

    @NonNull
    public Observable<List<Meal>> getAllFiltered(@NonNull final DateTime from, @NonNull final DateTime to) {
        return fromDatabaseTask(filteredBetween(from, to));
    }

    @NonNull
    private Callable<List<Meal>> filteredBetween(@NonNull final DateTime from, @NonNull final DateTime to) {
        return new Callable<List<Meal>>() {
            @Override
            public List<Meal> call() throws Exception {
                Query<Meal> query = filteredByDateSortedByNameSingleton().forCurrentThread();
                query.setParameter(0, from.getMillis());
                query.setParameter(1, to.getMillis());
                List<Meal> list = query.list();
                for (Meal meal : list) {
                    List<Ingredient> ingredients = meal.getIngredients();
                    for (Ingredient ingredient : ingredients) {
                        ingredient.getIngredientType();
                    }
                }
                return list;
            }
        };
    }

    @NonNull
    @Override
    protected Meal performGetById(long id) {
        Meal meal = dao().load(id);
        meal.getIngredients();
        return meal;
    }

    @NonNull
    @Override
    protected Meal performInsert(@NonNull Meal meal) {
        dao().insert(meal);
        meal.getIngredients();
        return meal;
    }

    @Override
    protected void performRemove(@NonNull Meal data) {
        List<Ingredient> ingredients = data.getIngredients();
        data.delete();
        for (Ingredient ingredient: ingredients) {
            ingredient.delete();
        }
    }

    @Override
    protected void performRemoveAll() {
        dao().deleteAll();
    }

    @NonNull
    @Override
    protected CursorQuery allSortedByName() {
        return dao().queryBuilder()
                .orderAsc(MealDao.Properties.Name)
                .buildCursor();
    }

    @NonNull
    @Override
    protected CursorQuery filteredSortedByNameQuery() {
        return dao().queryBuilder()
                .where(MealDao.Properties.Name.like(""))
                .orderAsc(MealDao.Properties.Name)
                .buildCursor();
    }

    @NonNull
    protected Query<Meal> filteredByDateSortedByName() {
        return dao().queryBuilder()
                .where(MealDao.Properties.CreationDate.between("", ""))
                .orderAsc(MealDao.Properties.Name)
                .build();
    }

    @NonNull
    protected Query<Meal> filteredByDateSortedByNameSingleton() {
        if (filteredByDateSortedByName == null) {
            filteredByDateSortedByName = filteredByDateSortedByName();
        }
        return filteredByDateSortedByName;
    }

    @Override
    protected long readKey(@NonNull Cursor cursor, int columnIndex) {
        return dao().readKey(cursor, columnIndex);
    }

    @NonNull
    @Override
    protected Property getKeyProperty() {
        return MealDao.Properties.Id;
    }

    @Override
    protected long getKey(Meal meal) {
        return meal.getId();
    }
}
