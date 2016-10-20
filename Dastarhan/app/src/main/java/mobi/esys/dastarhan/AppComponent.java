package mobi.esys.dastarhan;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import io.realm.RealmConfiguration;
import mobi.esys.dastarhan.database.CartRepository;
import mobi.esys.dastarhan.database.CityRepository;
import mobi.esys.dastarhan.database.CuisineRepository;
import mobi.esys.dastarhan.database.DistrictRepository;
import mobi.esys.dastarhan.database.FoodRepository;
import mobi.esys.dastarhan.database.OrderRepository;
import mobi.esys.dastarhan.database.PromoRepository;
import mobi.esys.dastarhan.database.RealmModule;
import mobi.esys.dastarhan.database.RestaurantRepository;
import mobi.esys.dastarhan.database.UnitOfWork;
import mobi.esys.dastarhan.database.UserInfoRepository;
import mobi.esys.dastarhan.net.NetModule;

@Singleton
@Component(modules = {RealmModule.class, NetModule.class}, dependencies = Application.class)
public interface AppComponent {
    RealmConfiguration configuration();

    FoodRepository foodRepository();

    CuisineRepository cuisineRepository();

    RestaurantRepository restaurantRepository();

    OrderRepository orderRepository();

    CartRepository cartRepository();

    PromoRepository promoRepository();

    UserInfoRepository userInfoRepository();

    CityRepository cityRepository();

    DistrictRepository districtRepository();

    UnitOfWork getUow();

    void inject(CurrentRestaurantFragment fragment);

    void inject(AddAddressActivity fragment);

    void inject(SplashActivity activity);
}