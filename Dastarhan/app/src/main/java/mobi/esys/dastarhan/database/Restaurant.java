package mobi.esys.dastarhan.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Restaurant extends RealmObject {

    @PrimaryKey
    private int server_id;
    private String ru_name;
    private String en_name;
    private String additional_ru;
    private String additional_en;
    private String picture;
    private Integer vegetarian;
    private Integer featured;
    private Integer approved;
    private String cuisines;
    //address
    private Integer city_id;
    private Integer district_id;
    //time
    private String schedule;
    private String time1;
    private String time2;
    //contact
    private String contact_name_ru;
    private String contact_name_en;
    private String phone;
    private String mobile;
    private String email1;
    private String email2;
    private String contact_email;
    private String order_phone;
    //other
    private Integer min_order;
    private String payment_methods;
    //technical
    private Integer total_rating;
    private Integer total_votes;
    private Integer user_vote;

    /**
     * For Realm usage only
     */
    public Restaurant() {
        //For Realm usage only
    }

    public Restaurant(int server_id,
                      String ru_name,
                      String en_name,
                      String additional_ru,
                      String additional_en,
                      String picture,
                      Integer vegetarian,
                      Integer featured,
                      Integer approved,
                      String cuisines,
                      Integer city_id,
                      Integer district_id,
                      String schedule,
                      String time1,
                      String time2,
                      String contact_name_ru,
                      String contact_name_en,
                      String phone,
                      String mobile,
                      String email1,
                      String email2,
                      String contact_email,
                      String order_phone,
                      Integer min_order,
                      String payment_methods,
                      Integer total_rating,
                      Integer total_votes) {
        this.server_id = server_id;
        this.ru_name = ru_name;
        this.en_name = en_name;
        this.additional_ru = additional_ru;
        this.additional_en = additional_en;
        this.picture = picture;
        this.vegetarian = vegetarian;
        this.featured = featured;
        this.approved = approved;
        this.cuisines = cuisines;
        this.city_id = city_id;
        this.district_id = district_id;
        this.schedule = schedule;
        this.time1 = time1;
        this.time2 = time2;
        this.contact_name_ru = contact_name_ru;
        this.contact_name_en = contact_name_en;
        this.phone = phone;
        this.mobile = mobile;
        this.email1 = email1;
        this.email2 = email2;
        this.contact_email = contact_email;
        this.order_phone = order_phone;
        this.min_order = min_order;
        this.payment_methods = payment_methods;
        this.total_rating = total_rating;
        this.total_votes = total_votes;
    }

    public int getServer_id() {
        return server_id;
    }

    public String getRu_name() {
        return ru_name;
    }

    public String getEn_name() {
        return en_name;
    }

    public String getAdditional_ru() {
        return additional_ru;
    }

    public String getAdditional_en() {
        return additional_en;
    }

    public String getPicture() {
        return picture;
    }

    public Integer getVegetarian() {
        return vegetarian;
    }

    public Integer getFeatured() {
        return featured;
    }

    public Integer getApproved() {
        return approved;
    }

    public String getCuisines() {
        return cuisines;
    }

    public Integer getCity_id() {
        return city_id;
    }

    public Integer getDistrict_id() {
        return district_id;
    }

    public String getSchedule() {
        return schedule;
    }

    public String getTime1() {
        return time1;
    }

    public String getTime2() {
        return time2;
    }

    public String getContact_name_ru() {
        return contact_name_ru;
    }

    public String getContact_name_en() {
        return contact_name_en;
    }

    public String getPhone() {
        return phone;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail1() {
        return email1;
    }

    public String getEmail2() {
        return email2;
    }

    public String getContact_email() {
        return contact_email;
    }

    public String getOrder_phone() {
        return order_phone;
    }

    public Integer getMin_order() {
        return min_order;
    }

    public String getPayment_methods() {
        return payment_methods;
    }

    public Integer getTotal_rating() {
        return total_rating;
    }

    public Integer getTotal_votes() {
        return total_votes;
    }

    public Integer getUser_vote() {
        return user_vote;
    }

    public void setUser_vote(Integer vote) {
        if (user_vote == null) {
            user_vote = vote;
            total_votes++;
            total_rating = total_rating + user_vote;
        } else {
            total_rating = total_rating - user_vote + vote;
            user_vote = vote;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Restaurant that = (Restaurant) o;

        if (server_id != that.server_id) return false;
        if (ru_name != null ? !ru_name.equals(that.ru_name) : that.ru_name != null) return false;
        if (en_name != null ? !en_name.equals(that.en_name) : that.en_name != null) return false;
        if (additional_ru != null ? !additional_ru.equals(that.additional_ru) : that.additional_ru != null)
            return false;
        if (additional_en != null ? !additional_en.equals(that.additional_en) : that.additional_en != null)
            return false;
        if (picture != null ? !picture.equals(that.picture) : that.picture != null) return false;
        if (vegetarian != null ? !vegetarian.equals(that.vegetarian) : that.vegetarian != null)
            return false;
        if (featured != null ? !featured.equals(that.featured) : that.featured != null)
            return false;
        if (approved != null ? !approved.equals(that.approved) : that.approved != null)
            return false;
        if (cuisines != null ? !cuisines.equals(that.cuisines) : that.cuisines != null)
            return false;
        if (city_id != null ? !city_id.equals(that.city_id) : that.city_id != null) return false;
        if (district_id != null ? !district_id.equals(that.district_id) : that.district_id != null)
            return false;
        if (schedule != null ? !schedule.equals(that.schedule) : that.schedule != null)
            return false;
        if (time1 != null ? !time1.equals(that.time1) : that.time1 != null) return false;
        if (time2 != null ? !time2.equals(that.time2) : that.time2 != null) return false;
        if (contact_name_ru != null ? !contact_name_ru.equals(that.contact_name_ru) : that.contact_name_ru != null)
            return false;
        if (contact_name_en != null ? !contact_name_en.equals(that.contact_name_en) : that.contact_name_en != null)
            return false;
        if (phone != null ? !phone.equals(that.phone) : that.phone != null) return false;
        if (mobile != null ? !mobile.equals(that.mobile) : that.mobile != null) return false;
        if (email1 != null ? !email1.equals(that.email1) : that.email1 != null) return false;
        if (email2 != null ? !email2.equals(that.email2) : that.email2 != null) return false;
        if (contact_email != null ? !contact_email.equals(that.contact_email) : that.contact_email != null)
            return false;
        if (order_phone != null ? !order_phone.equals(that.order_phone) : that.order_phone != null)
            return false;
        if (min_order != null ? !min_order.equals(that.min_order) : that.min_order != null)
            return false;
        if (payment_methods != null ? !payment_methods.equals(that.payment_methods) : that.payment_methods != null)
            return false;
        if (total_rating != null ? !total_rating.equals(that.total_rating) : that.total_rating != null)
            return false;
        if (total_votes != null ? !total_votes.equals(that.total_votes) : that.total_votes != null)
            return false;
        return !(user_vote != null ? !user_vote.equals(that.user_vote) : that.user_vote != null);

    }

    @Override
    public int hashCode() {
        int result = server_id;
        result = 31 * result + (ru_name != null ? ru_name.hashCode() : 0);
        result = 31 * result + (en_name != null ? en_name.hashCode() : 0);
        result = 31 * result + (additional_ru != null ? additional_ru.hashCode() : 0);
        result = 31 * result + (additional_en != null ? additional_en.hashCode() : 0);
        result = 31 * result + (picture != null ? picture.hashCode() : 0);
        result = 31 * result + (vegetarian != null ? vegetarian.hashCode() : 0);
        result = 31 * result + (featured != null ? featured.hashCode() : 0);
        result = 31 * result + (approved != null ? approved.hashCode() : 0);
        result = 31 * result + (cuisines != null ? cuisines.hashCode() : 0);
        result = 31 * result + (city_id != null ? city_id.hashCode() : 0);
        result = 31 * result + (district_id != null ? district_id.hashCode() : 0);
        result = 31 * result + (schedule != null ? schedule.hashCode() : 0);
        result = 31 * result + (time1 != null ? time1.hashCode() : 0);
        result = 31 * result + (time2 != null ? time2.hashCode() : 0);
        result = 31 * result + (contact_name_ru != null ? contact_name_ru.hashCode() : 0);
        result = 31 * result + (contact_name_en != null ? contact_name_en.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (mobile != null ? mobile.hashCode() : 0);
        result = 31 * result + (email1 != null ? email1.hashCode() : 0);
        result = 31 * result + (email2 != null ? email2.hashCode() : 0);
        result = 31 * result + (contact_email != null ? contact_email.hashCode() : 0);
        result = 31 * result + (order_phone != null ? order_phone.hashCode() : 0);
        result = 31 * result + (min_order != null ? min_order.hashCode() : 0);
        result = 31 * result + (payment_methods != null ? payment_methods.hashCode() : 0);
        result = 31 * result + (total_rating != null ? total_rating.hashCode() : 0);
        result = 31 * result + (total_votes != null ? total_votes.hashCode() : 0);
        result = 31 * result + (user_vote != null ? user_vote.hashCode() : 0);
        return result;
    }
}
