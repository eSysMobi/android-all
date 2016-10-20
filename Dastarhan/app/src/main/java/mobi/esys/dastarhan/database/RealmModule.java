package mobi.esys.dastarhan.database;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.RealmConfiguration;

@Module
public class RealmModule {

    private final long dbVersion;

    public RealmModule(long dbVersion) {
        this.dbVersion = dbVersion;
    }

    @Provides
    @Singleton
    public final RealmConfiguration provideConfiguration(Application application) {
        return new RealmConfiguration
                .Builder(application)
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(dbVersion)
                .build();
    }

    @Provides
    @Singleton
    public UnitOfWork provideUnitOfWork(RealmConfiguration realmConfiguration) {
        return new RealmUnitOfWork(realmConfiguration);
    }

    @Provides
    public final FoodRepository provideFoodRepository(RealmConfiguration realmConfiguration, UnitOfWork uow) {
        return new FoodRealmRepository(realmConfiguration, uow);
    }

    @Provides
    public final CuisineRepository provideCuisineRepository(RealmConfiguration realmConfiguration, UnitOfWork uow) {
        return new CuisineRealmRepository(realmConfiguration, uow);
    }

    @Provides
    public final RestaurantRepository provideRestaurantRepository(RealmConfiguration realmConfiguration, UnitOfWork uow) {
        return new RestaurantRealmRepository(realmConfiguration, uow);
    }

    @Provides
    public final OrderRepository provideOrderRepository(RealmConfiguration realmConfiguration, UnitOfWork uow) {
        return new OrderRealmRepository(realmConfiguration, uow);
    }

    @Provides
    public final CartRepository provideCartRepository(RealmConfiguration realmConfiguration, UnitOfWork uow) {
        return new CartRealmRepository(realmConfiguration, uow);
    }

    @Provides
    public final PromoRepository providePromoRepository(RealmConfiguration realmConfiguration, UnitOfWork uow) {
        return new PromoRealmRepository(realmConfiguration, uow);
    }

    @Provides
    public final UserInfoRepository provideUserInfoRepository(RealmConfiguration realmConfiguration, UnitOfWork uow) {
        return new UserInfoRealmRepository(realmConfiguration, uow);
    }

    @Provides
    public final CityRepository provideCityRepository(RealmConfiguration realmConfiguration, UnitOfWork uow) {
        return new CityRealmRepository(realmConfiguration, uow);
    }

    @Provides
    public final DistrictRepository provideDistrictRepository(RealmConfiguration realmConfiguration, UnitOfWork uow) {
        return new DistrictRealmRepository(realmConfiguration, uow);
    }
}