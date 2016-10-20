package mobi.esys.dastarhan.net;


import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ZeyUzh on 10.10.2016.
 */
public interface APIAuthorize {
    // http://dastarhan.net/index.php/user_api/auth/format/json?email=qwe@asd.com&pass=753

    @GET("/index.php/user_api/auth/format/json")
    Call<JsonObject> auth(@Query("email") String email, @Query("pass") String pass);

}
