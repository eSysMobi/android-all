package mobi.esys.dastarhan.database;

import java.util.List;

public interface FoodRepository {

    void addOrUpdate(Food food);

    Food getById(int id);

    List<Food> getByRestaurantID(int restID);

    List<Food> getByCategoryID(int catID);

    List<Food> getByFavorite();

    void updateFavorites(int id, boolean fav);

    void updateOrdered(int id, boolean isOrdered);
}