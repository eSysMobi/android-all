package mobi.esys.dastarhan.net;


import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ZeyUzh on 10.10.2016.
 */
public interface APIAddress {
    //example http://dastarhan.net/index.php/user_api/cities/format/json
    @GET("/index.php/user_api/cities/format/json")
    Call<JsonObject> getCities();

    //example http://dastarhan.net/index.php/user_api/districts/format/json?city_id=1
    @GET("/index.php/user_api/districts/format/json")
    Call<JsonObject> getCityDistricts(@Query("city_id") long cityID);

    //example http://dastarhan.net/index.php/user_api/districts/format/json
    @GET("/index.php/user_api/districts/format/json")
    Call<JsonObject> getAllDistricts();
}
