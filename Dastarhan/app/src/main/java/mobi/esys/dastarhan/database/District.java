package mobi.esys.dastarhan.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class District extends RealmObject {

    @PrimaryKey
    private int districtID;
    private int cityID;
    private String districtRuName;
    private String districtEnName;

    /**
     * For Realm usage only
     */
    public District() {
        //For Realm usage only
    }

    public District(int districtID, int cityID, String districtRuName, String districtEnName) {
        this.districtID = districtID;
        this.cityID = cityID;
        this.districtRuName = districtRuName;
        this.districtEnName = districtEnName;
    }

    public int getDistrictID() {
        return districtID;
    }

    public String getDistrictRuName() {
        return districtRuName;
    }

    public String getDistrictEnName() {
        return districtEnName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        District district = (District) o;

        if (districtID != district.districtID) return false;
        if (cityID != district.cityID) return false;
        if (districtRuName != null ? !districtRuName.equals(district.districtRuName) : district.districtRuName != null)
            return false;
        return !(districtEnName != null ? !districtEnName.equals(district.districtEnName) : district.districtEnName != null);

    }

    @Override
    public int hashCode() {
        int result = districtID;
        result = 31 * result + cityID;
        result = 31 * result + (districtRuName != null ? districtRuName.hashCode() : 0);
        result = 31 * result + (districtEnName != null ? districtEnName.hashCode() : 0);
        return result;
    }
}
