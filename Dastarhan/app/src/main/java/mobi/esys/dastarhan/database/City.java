package mobi.esys.dastarhan.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class City extends RealmObject {

    @PrimaryKey
    private int cityID;
    private String cityRuName;
    private String cityEnName;

    /**
     * For Realm usage only
     */
    public City() {
        //For Realm usage only
    }

    public City(int cityID, String cityRuName, String cityEnName) {
        this.cityID = cityID;
        this.cityRuName = cityRuName;
        this.cityEnName = cityEnName;
    }

    public String getCityRuName() {
        return cityRuName;
    }

    public String getCityEnName() {
        return cityEnName;
    }

    public int getCityID() {
        return cityID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        if (cityID != city.cityID) return false;
        if (cityRuName != null ? !cityRuName.equals(city.cityRuName) : city.cityRuName != null)
            return false;
        return !(cityEnName != null ? !cityEnName.equals(city.cityEnName) : city.cityEnName != null);

    }

    @Override
    public int hashCode() {
        int result = cityID;
        result = 31 * result + (cityRuName != null ? cityRuName.hashCode() : 0);
        result = 31 * result + (cityEnName != null ? cityEnName.hashCode() : 0);
        return result;
    }
}
