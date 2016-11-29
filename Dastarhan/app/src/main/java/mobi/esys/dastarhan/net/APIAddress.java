package mobi.esys.dastarhan.net;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ZeyUzh on 10.10.2016.
 */
public interface APIAddress {
    //get all cities
    //example http://dastarhan.net/index.php/user_api/cities/format/json
    @GET("/index.php/user_api/cities/format/json")
    Call<JsonArray> getCities();

    //get all districts of the city
    //example http://dastarhan.net/index.php/user_api/districts/format/json?city_id=1
    @GET("/index.php/user_api/districts/format/json")
    Call<JsonObject> getCityDistricts(@Query("city_id") long cityID);

    //get all districts
    //example http://dastarhan.net/index.php/user_api/districts/format/json
    @GET("/index.php/user_api/districts/format/json")
    Call<JsonObject> getAllDistricts();

    //get all addresses of user
    //example http://dastarhan.net/index.php/user_api/addresses/format/json?id=59&apikey=ySdzeYpL3819VM2w7V34
    @GET("/index.php/user_api/addresses/format/json")
    Call<JsonObject> getAllUserAddresses(@Query("id") long userID,
                                         @Query("apikey") String apiToken);

    //send new address to server
    //example http://dastarhan.net/index.php/user_api/addaddr/format/json?id=80&apikey=84OU10676gp7qq0a2I51&addr={son_address}&city_id=1&district_id=3
    @GET("/index.php/user_api/addaddr/format/json")
    Call<JsonObject> getAddNewUserAddress(@Query("id") int userID,
                                          @Query("apikey") String apiToken,
                                          @Query("addr") String address,
                                          @Query("city_id") int cityID,
                                          @Query("district_id") int districtID);
}
