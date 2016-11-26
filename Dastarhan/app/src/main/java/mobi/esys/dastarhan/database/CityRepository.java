package mobi.esys.dastarhan.database;

import java.util.List;

public interface CityRepository {

    void createOrUpdate(City district);

    List<City> getCities();

    City getCityByName(String name);

    City getCityByID(int id);
}