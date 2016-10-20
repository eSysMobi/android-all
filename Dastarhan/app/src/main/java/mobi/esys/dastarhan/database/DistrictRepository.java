package mobi.esys.dastarhan.database;

import java.util.List;

public interface DistrictRepository {

    void createOrUpdate(District district);

    List<District> getDistrictsOfCity(int cityID);
}