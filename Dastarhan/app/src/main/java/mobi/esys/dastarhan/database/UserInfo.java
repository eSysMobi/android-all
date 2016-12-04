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
    //address
    private Integer city;
    private Integer district;
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
                       Integer city,
                       Integer district,
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

    public void updateUserInfo(String name,
                       String phone,
                       String needChange,
                       String comment) {
        this.name = name;
        this.phone = phone;
        this.needChange = needChange;
        this.comment = comment;
    }

    public void updateAddress(
                       Integer city,
                       Integer district,
                       String street,
                       String house,
                       String corpBuilding,
                       String apartmentOffice,
                       String enterNum,
                       String floor,
                       String domophoneNum) {
        this.city = city;
        this.district = district;
        this.street = street;
        this.house = house;
        this.corpBuilding = corpBuilding;
        this.apartmentOffice = apartmentOffice;
        this.enterNum = enterNum;
        this.floor = floor;
        this.domophoneNum = domophoneNum;
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

    public Integer getCity() {
        return city;
    }

    public Integer getDistrict() {
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

    public boolean equalsByAddress(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
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
        return true;
    }

    public boolean equalsByUserInfo(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        if (name != null ? !name.equals(userInfo.name) : userInfo.name != null) return false;
        if (phone != null ? !phone.equals(userInfo.phone) : userInfo.phone != null) return false;
        if (needChange != null ? !needChange.equals(userInfo.needChange) : userInfo.needChange != null)
            return false;
        return !(comment != null ? !comment.equals(userInfo.comment) : userInfo.comment != null);
    }

    public String getFullAddress() {
        String result = "";
        StringBuilder sb = new StringBuilder();
        if (name != null
                && phone != null
                && city != null
                && district != null
                && house != null
                && apartmentOffice != null
                ) {
            sb.append("street: ");
            if (street != null) {
                sb.append(street);
            } else {
                sb.append("-");
            }
            sb.append(", ").append("house: ");
            if (house != null) {
                sb.append(house);
            } else {
                sb.append("-");
            }
            sb.append(", ").append("corpus_or_building: ");
            if (corpBuilding != null) {
                sb.append(corpBuilding);
            } else {
                sb.append("-");
            }
            sb.append(", ").append("apartment_or_office: ");
            if (apartmentOffice != null) {
                sb.append(apartmentOffice);
            } else {
                sb.append("-");
            }
            sb.append(", ").append("porch: ");
            if (enterNum != null) {
                sb.append(enterNum);
            } else {
                sb.append("-");
            }
            sb.append(", ").append("floor: ");
            if (floor != null) {
                sb.append(floor);
            } else {
                sb.append("-");
            }
            sb.append(", ").append("intercom_code: ");
            if (domophoneNum != null) {
                sb.append(domophoneNum);
            } else {
                sb.append("-");
            }
            result = sb.toString();
        }
        return result;
    }
}
