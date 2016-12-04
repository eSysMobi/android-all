package mobi.esys.dastarhan.net;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ZeyUzh on 10.10.2016.
 */
public interface APIDelivery {
    //get all addresses of user
    //example http://dastarhan.net/index.php/view/json_estimate_delivery?addr=107&res_id=15
    @GET("/index.php/view/json_estimate_delivery")
    Call<ResponseBody> getDeliveryInfoFromRestaurant(@Query("addr") int addresID,
                                               @Query("res_id") int restaurantID);
}
