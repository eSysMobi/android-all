package mobi.esys.dastarhan.database;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import io.realm.RealmConfiguration;

@Singleton
@Component(modules = RealmModule.class, dependencies = Application.class)
public interface RealmComponent {
    RealmConfiguration configuration();

    FoodRepository foodRepository();

    CuisineRepository cuisineRepository();

    RestaurantRepository restaurantRepository();

    OrderRepository orderRepository();

    CartRepository cartRepository();

    PromoRepository promoRepository();

    UnitOfWork getUow();
}