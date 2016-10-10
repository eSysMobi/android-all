package mobi.esys.dastarhan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import java.io.IOException;

import javax.inject.Inject;

import mobi.esys.dastarhan.database.Restaurant;
import mobi.esys.dastarhan.database.RestaurantRepository;
import mobi.esys.dastarhan.net.APIVoteForRestaurant;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CurrentRestaurantFragment extends BaseFragment {

    private final String TAG = "dtagCurrRestActivity";
    private SharedPreferences prefs;

    private static final String ARG_RESTAURANT = "restaurant_id";

    private TextView mtvCurrRestName;
    private SimpleRatingBar mCurrRestRating;
    private ImageView mivCurrRestImage;
    private ImageView mivCurrRestVegan;
    private FrameLayout mflCurrRestInfo;
    private TextView tvCurrRestRecommendationCount;

    @Inject
    RestaurantRepository restRepo;
    @Inject
    Retrofit retrofit;
    private APIVoteForRestaurant apiVote;
    private Restaurant restaurant;

    public CurrentRestaurantFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CurrentRestaurantFragment.
     */
    public static CurrentRestaurantFragment newInstance(int restID) {
        CurrentRestaurantFragment fragment = new CurrentRestaurantFragment();
        //args
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_RESTAURANT, restID);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_current_restaurant, container, false);

        Bundle bundle = getArguments();
        int restID = bundle.getInt(ARG_RESTAURANT, -42);
        Log.d(TAG, "Start getting info from DB about restaurant with id " + restID);

        ((DastarhanApp) getActivity().getApplication()).appComponent().inject(this);

        apiVote = retrofit.create(APIVoteForRestaurant.class);

        mtvCurrRestName = (TextView) view.findViewById(R.id.tvCurrRestName);
        mCurrRestRating = (SimpleRatingBar) view.findViewById(R.id.сurrRestRating);
        mivCurrRestImage = (ImageView) view.findViewById(R.id.ivCurrRestImage);
        mivCurrRestVegan = (ImageView) view.findViewById(R.id.ivCurrRestVegan);
        mflCurrRestInfo = (FrameLayout) view.findViewById(R.id.flCurrRestInfo);
        FrameLayout mflCurrRestRating = (FrameLayout) view.findViewById(R.id.flCurrRestRating);
        tvCurrRestRecommendationCount = (TextView) view.findViewById(R.id.tvCurrRestRecomendationCount);

        prefs = getActivity().getApplicationContext().getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE);

        restaurant = restRepo.getById(restID);
        if (restaurant != null) {
            String locale = getContext().getResources().getConfiguration().locale.getLanguage();
            TextView mtvCurrRestDesrc = (TextView) view.findViewById(R.id.tvCurrRestDesrc);

            if (locale.equals("ru")) {
                mtvCurrRestName.setText(restaurant.getRu_name());
                mtvCurrRestDesrc.setText(restaurant.getAdditional_ru());
            } else {
                mtvCurrRestName.setText(restaurant.getEn_name());
                mtvCurrRestDesrc.setText(restaurant.getAdditional_en());
            }

            updateRating();
            mflCurrRestRating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (prefs.getString(Constants.PREF_SAVED_LOGIN, "").isEmpty()) {
                        //authorize
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivityForResult(intent, Constants.REQUEST_CODE_VOTE_REST);
                    } else {
                        showRateDialog();
                    }
                }
            });

            if (restaurant.getVegetarian() == 1) {
                mivCurrRestVegan.setVisibility(View.GONE);
            }

            //ste "open hours"
            TextView mtvCurrRestTime = (TextView) view.findViewById(R.id.tvCurrRestTime);
            String openHours = "";
            if (restaurant.getTime1().length() > 5) {
                openHours = restaurant.getTime1().substring(0, 5) + " - ";
            } else {
                openHours = restaurant.getTime1() + " - ";
            }
            if (restaurant.getTime2().length() > 5) {
                openHours = openHours + restaurant.getTime2().substring(0, 5);
            } else {
                openHours = openHours + restaurant.getTime2();
            }
            mtvCurrRestTime.setText(openHours);

            //set schedule
            TextView mtvCurrRestSchedule = (TextView) view.findViewById(R.id.tvCurrRestSchedule);
            String schedule = restaurant.getSchedule();
            String setSchedule = "";
            if (schedule.contains("1")) setSchedule = setSchedule + "Mon, ";
            if (schedule.contains("2")) setSchedule = setSchedule + "Tue, ";
            if (schedule.contains("3")) setSchedule = setSchedule + "Wed, ";
            if (schedule.contains("4")) setSchedule = setSchedule + "Thu, ";
            if (schedule.contains("5")) setSchedule = setSchedule + "Fri, ";
            if (schedule.contains("6")) setSchedule = setSchedule + "Sat, ";
            if (schedule.contains("7")) setSchedule = setSchedule + "Sun, ";
            if (setSchedule.endsWith(", "))
                setSchedule = setSchedule.substring(0, setSchedule.length() - 2);
            mtvCurrRestSchedule.setText(setSchedule);

            //set phone
            TextView mtvCurrRestTel = (TextView) view.findViewById(R.id.tvCurrRestTel);
            if (!restaurant.getPhone().isEmpty() && !restaurant.getMobile().isEmpty()) {
                String setPhone = restaurant.getPhone();
                setPhone = setPhone + ", " + restaurant.getMobile();
                if (setPhone.endsWith(", "))
                    setPhone = setPhone.substring(0, setPhone.length() - 2);
                mtvCurrRestTel.setText(setPhone);
            } else {
                mtvCurrRestTel.setVisibility(View.GONE);
                TextView mtvCurrRestInfoTel = (TextView) view.findViewById(R.id.tvCurrRestInfoTel);
                mtvCurrRestInfoTel.setVisibility(View.GONE);
            }


            TextView mtvCurrRestTelOrder = (TextView) view.findViewById(R.id.tvCurrRestTelOrder);
            if (!restaurant.getOrder_phone().isEmpty()) {
                mtvCurrRestTelOrder.setText(restaurant.getOrder_phone());
            } else {
                mtvCurrRestTelOrder.setVisibility(View.GONE);
                TextView mtvCurrRestInfoTelOrder = (TextView) view.findViewById(R.id.tvCurrRestInfoTelOrder);
                mtvCurrRestInfoTelOrder.setVisibility(View.GONE);
            }

            TextView mtvCurrRestEmail = (TextView) view.findViewById(R.id.tvCurrRestEmail);
            if (!restaurant.getEmail1().isEmpty() && !restaurant.getEmail2().isEmpty()) {
                mtvCurrRestEmail.setText(restaurant.getEmail1() + " " + restaurant.getEmail2());
            } else {
                mtvCurrRestEmail.setVisibility(View.GONE);
                TextView mtvCurrRestInfoEmail = (TextView) view.findViewById(R.id.tvCurrRestInfoEmail);
                mtvCurrRestInfoEmail.setVisibility(View.GONE);
            }

            TextView mtvCurrRestPayment = (TextView) view.findViewById(R.id.tvCurrRestPayment);
            mtvCurrRestPayment.setText(restaurant.getPayment_methods());
        } else {
            hideNotFoundData();
            Log.e(TAG, "No restaurant with this ID ");
        }

        return view;
    }

    private void updateRating() {
        tvCurrRestRecommendationCount.setText(String.valueOf(restaurant.getTotal_votes()));

        int rate = 0;
        if (restaurant.getTotal_votes() > 0) {
            rate = restaurant.getTotal_rating() / restaurant.getTotal_votes();
        }
        if (rate > 5) {
            rate = 5;
        }
        mCurrRestRating.setRating(rate);
    }

    private void showRateDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alertdialog_rate_rest, null);
        dialogBuilder.setView(dialogView);

        final SimpleRatingBar rating = (SimpleRatingBar) dialogView.findViewById(R.id.сurrRestRatingAD);

        dialogBuilder.setTitle(R.string.please_rate_restaurant);
        dialogBuilder.setPositiveButton(R.string.vote, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //send rating
                Log.d(TAG, "Sending vote to server. User rating is " + rating.getRating());
                final int rate = (int) rating.getRating();
                String apiKey = prefs.getString(Constants.PREF_SAVED_AUTH_TOKEN, "");
                int userID = prefs.getInt(Constants.PREF_SAVED_USER_ID, -1);
                //send vote
                Call<JsonObject> apiCall = apiVote.vote(userID, apiKey, restaurant.getServer_id(), rate);
                apiCall.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.code() == 200) {
                            //all ok, store rating
                            restaurant = restRepo.voteForRestaurant(restaurant.getServer_id(), rate);
                            //update info on screen
                            updateRating();
                        }
                        if (response.code() == 404 && response.body().toString().equals("{\"error\":\"No data\"}")) {
                            //TODO need new token
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        //may be no inet
                        if (t instanceof IOException) {
                            Toast.makeText(CurrentRestaurantFragment.this.getContext(), R.string.no_inet, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //close
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


    private void hideNotFoundData() {
        TextView mtvCurrRestNotFound = (TextView) getView().findViewById(R.id.tvCurrRestNotFound);
        if (mtvCurrRestNotFound != null) {
            mtvCurrRestNotFound.setVisibility(View.VISIBLE);
        }
        mtvCurrRestName.setVisibility(View.GONE);
        mCurrRestRating.setVisibility(View.GONE);
        mivCurrRestImage.setVisibility(View.GONE);
        mivCurrRestVegan.setVisibility(View.GONE);
        mflCurrRestInfo.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Login in vote for restaurant requestCode " + requestCode + " resultCode " + resultCode);
        if (requestCode == Constants.REQUEST_CODE_VOTE_REST) {
            if (resultCode == Activity.RESULT_OK) {
                showRateDialog();
            }
        }
    }
}
