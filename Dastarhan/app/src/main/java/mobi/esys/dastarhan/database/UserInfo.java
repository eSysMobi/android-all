package mobi.esys.dastarhan.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class UserInfo extends RealmObject {

    @PrimaryKey
    private int inner_id;
    private String name;
    private String phone;
    private String email;
    private String pass;
    //adress
    private String city;
    private String district;
    private String street;
    private String house;
    private String corpBuilding;
    private String apartmentOffice;
    private String enterNum;
    private String floor;
    private String domophoneNum;
    //advanced
    private String needChange;
    private String comment;
    private Integer serverAddressID;

    /**
     * For Realm usage only
     */
    public UserInfo() {
        //For Realm usage only
    }

    public void update(String email,
                       String pass) {
        this.email = email;
        this.pass = pass;
    }

    public void update(String name,
                       String phone,
                       String city,
                       String district,
                       String street,
                       String house,
                       String corpBuilding,
                       String apartmentOffice,
                       String enterNum,
                       String floor,
                       String domophoneNum,
                       String needChange,
                       String comment) {
        this.name = name;
        this.phone = phone;
        this.city = city;
        this.district = district;
        this.street = street;
        this.house = house;
        this.corpBuilding = corpBuilding;
        this.apartmentOffice = apartmentOffice;
        this.enterNum = enterNum;
        this.floor = floor;
        this.domophoneNum = domophoneNum;
        this.needChange = needChange;
        this.comment = comment;
        //clear serverAddressID
        serverAddressID = null;
    }

    public void updateAddressID(Integer serverAddressID) {
        this.serverAddressID = serverAddressID;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPass() {
        return pass;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getStreet() {
        return street;
    }

    public String getHouse() {
        return house;
    }

    public String getCorpBuilding() {
        return corpBuilding;
    }

    public String getApartmentOffice() {
        return apartmentOffice;
    }

    public String getEnterNum() {
        return enterNum;
    }

    public String getFloor() {
        return floor;
    }

    public String getDomophoneNum() {
        return domophoneNum;
    }

    public String getNeedChange() {
        return needChange;
    }

    public String getComment() {
        return comment;
    }

    public Integer getServerAddressID() {
        return serverAddressID;
    }

    public boolean equalsByAddressInfo(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        if (name != null ? !name.equals(userInfo.name) : userInfo.name != null) return false;
        if (phone != null ? !phone.equals(userInfo.phone) : userInfo.phone != null) return false;
        if (city != null ? !city.equals(userInfo.city) : userInfo.city != null) return false;
        if (district != null ? !district.equals(userInfo.district) : userInfo.district != null)
            return false;
        if (street != null ? !street.equals(userInfo.street) : userInfo.street != null)
            return false;
        if (house != null ? !house.equals(userInfo.house) : userInfo.house != null) return false;
        if (corpBuilding != null ? !corpBuilding.equals(userInfo.corpBuilding) : userInfo.corpBuilding != null)
            return false;
        if (apartmentOffice != null ? !apartmentOffice.equals(userInfo.apartmentOffice) : userInfo.apartmentOffice != null)
            return false;
        if (enterNum != null ? !enterNum.equals(userInfo.enterNum) : userInfo.enterNum != null)
            return false;
        if (floor != null ? !floor.equals(userInfo.floor) : userInfo.floor != null) return false;
        if (domophoneNum != null ? !domophoneNum.equals(userInfo.domophoneNum) : userInfo.domophoneNum != null)
            return false;
        if (needChange != null ? !needChange.equals(userInfo.needChange) : userInfo.needChange != null)
            return false;
        return !(comment != null ? !comment.equals(userInfo.comment) : userInfo.comment != null);
    }
}
