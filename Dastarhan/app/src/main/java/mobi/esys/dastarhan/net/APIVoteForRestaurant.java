package mobi.esys.dastarhan.net;


import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ZeyUzh on 10.10.2016.
 */
public interface APIVoteForRestaurant {
    //index.php/user_api/voteforrest/format/json?id=77&apikey=eAJpOu7Jc63g732Aj0W8&res=15&vote=1

    @GET("index.php/user_api/voteforrest/format/json")
    Call<JsonObject> vote(@Query("id") long id, @Query("apikey") String apikey, @Query("res") long restID, @Query("vote") int vote);

}
