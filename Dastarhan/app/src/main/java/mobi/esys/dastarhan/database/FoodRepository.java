package mobi.esys.dastarhan.database;

import java.util.List;

public interface FoodRepository {

    void addOrUpdate(Food food);

    Food getById(int id);

    List<Food> getByIds(Integer[] IDs);

    List<Food> getByRestaurantID(int restID);

    List<Food> getByRestaurantIDs(Integer[] restIDs);

    List<Food> getByCategoryID(int catID);

    List<Food> getByFavorite();

    List<Food> getAll();

    void updateFavorites(int id, boolean fav);

    void updateOrdered(int id, boolean isOrdered);
}