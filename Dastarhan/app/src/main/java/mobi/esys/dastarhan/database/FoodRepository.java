package mobi.esys.dastarhan.database;

import java.util.List;

public interface FoodRepository {

    void addOrUpdate(Food food);

    Food getById(long id);

    List<Food> getByRestaurantID(long restID);

    List<Food> getByCategoryID(long catID);

    List<Food> getByFavorite();

    void updateFavorites(long id, boolean fav);

    void updateOrdered(long id, boolean isOrdered);
}