package mobi.esys.dastarhan.utils;

import mobi.esys.dastarhan.database.City;
import mobi.esys.dastarhan.database.District;

/**
 * Created by ZeyUzh on 27.10.2016.
 */
public interface CityOrDistrictChooser {
    void chooseCity(City city, String localizedName);
    void chooseDistrict(District district, String localizedName);
}
