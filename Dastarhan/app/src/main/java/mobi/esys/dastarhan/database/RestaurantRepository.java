package mobi.esys.dastarhan.database;

import java.util.List;

public interface RestaurantRepository {

    void addOrUpdate(Restaurant restaurant);

    Restaurant getById(long id);

    List<Restaurant> getAll();
}