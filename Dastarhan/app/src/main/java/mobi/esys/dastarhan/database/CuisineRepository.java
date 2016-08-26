package mobi.esys.dastarhan.database;

import java.util.List;

public interface CuisineRepository {

    void addOrUpdate(Cuisine cuisine);

    Cuisine getById(long id);

    List<Cuisine> getAll();
}