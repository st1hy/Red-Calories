package com.github.st1hy.countthemcalories.database.rx.timeperiod;

import android.database.Cursor;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.github.st1hy.countthemcalories.contract.model.CalorieStatistics;
import com.github.st1hy.countthemcalories.database.IngredientDao;
import com.github.st1hy.countthemcalories.database.IngredientTemplateDao;
import com.github.st1hy.countthemcalories.database.MealDao;
import com.github.st1hy.countthemcalories.database.Weight;
import com.github.st1hy.countthemcalories.database.WeightDao;
import com.github.st1hy.countthemcalories.database.rx.AbstractRxDatabaseModel;

import org.greenrobot.greendao.query.CursorQuery;
import org.greenrobot.greendao.query.Query;
import org.joda.time.DateTime;

import java.util.List;

import javax.inject.Provider;

import dagger.internal.SingleCheck;
import rx.Observable;
import rx.subjects.PublishSubject;


public class TimePeriodModel extends AbstractRxDatabaseModel {

    public TimePeriodModel() {
    }

    private final Provider<CursorQuery> queryProvider = SingleCheck.provider(() -> {
        String sql = "SELECT " +
                "M." + MealDao.Properties.CreationDate.columnName + ", " +
                "I." + IngredientDao.Properties.Amount.columnName + ", " +
                "IT." + IngredientTemplateDao.Properties.EnergyDensityAmount.columnName + ", " +
                "IT." + IngredientTemplateDao.Properties.AmountType.columnName + " " +
                "from " + MealDao.TABLENAME + " M " +
                "join " + IngredientDao.TABLENAME + " I " +
                "on I." + IngredientDao.Properties.PartOfMealId.columnName +
                " = M." + MealDao.Properties.Id.columnName + " " +
                "join " + IngredientTemplateDao.TABLENAME + " IT " +
                "on I." + IngredientDao.Properties.IngredientTypeId.columnName +
                " = IT." + IngredientTemplateDao.Properties.Id.columnName + " " +
                "where M." + MealDao.Properties.CreationDate.columnName + " " +
                "between " + "(?) and (?) " +
                "order by M." + MealDao.Properties.CreationDate.columnName + " asc;";
        return CursorQuery.internalCreate(dao(), sql, new Object[2]);
    });

    private final Provider<Query<Weight>> weightQueryProvider = SingleCheck.provider(() -> session()
            .getWeightDao()
            .queryBuilder()
            .where(WeightDao.Properties.MeasurementDate.between(null, null))
            .orderAsc(WeightDao.Properties.MeasurementDate)
            .build());

    private DateTime start, end;
    private final PublishSubject<CalorieStatistics> updates = PublishSubject.create();

    @NonNull
    @CheckResult
    public Observable<CalorieStatistics> updates() {
        return updates.asObservable();
    }

    public void refresh() {
        if (start != null && end != null) {
            refresh(start, end);
        }
    }

    public void refresh(@NonNull final DateTime start, @NonNull final DateTime end) {
        this.start = start;
        this.end = end;
        loadData(start, end).subscribe(updates::onNext);
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources") //Min Api 19, current min 16
    @NonNull
    @CheckResult
    private Observable<CalorieStatistics> loadData(@NonNull final DateTime start,
                                                   @NonNull final DateTime end) {
        return fromDatabaseTask(() -> {
            CursorQuery query = queryProvider.get().forCurrentThread();
            query.setParameter(0, start.getMillis());
            query.setParameter(1, end.getMillis());
            Cursor cursor = query.query();
            Query<Weight> weightQuery = weightQueryProvider.get().forCurrentThread();
            weightQuery.setParameter(0, start.getMillis() - 1);
            weightQuery.setParameter(1, end.getMillis() + 1);
            List<Weight> list = weightQuery.listLazy();
            try {
                return new TimePeriod.Builder(cursor, start, end, list).build();
            } finally {
                cursor.close();
            }
        });
    }

    @NonNull
    private MealDao dao() {
        return session().getMealDao();
    }
}
