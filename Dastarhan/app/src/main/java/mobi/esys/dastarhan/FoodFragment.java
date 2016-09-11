package mobi.esys.dastarhan;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.dastarhan.database.Restaurant;
import mobi.esys.dastarhan.database.RestaurantRepository;
import mobi.esys.dastarhan.tasks.GetFood;
import mobi.esys.dastarhan.utils.FoodCheckElement;
import mobi.esys.dastarhan.utils.RVFoodAdapterMain;

public class FoodFragment extends BaseFragment {

    private final String TAG = "dtagFoodActivity";
    private DastarhanApp dastarhanApp;

    private RecyclerView mrvFood;
    private ProgressBar mpbFood;
    private Handler handlerFood;

    private static final String ARG_CUISINE = "cuisine_id";
    private static final String ARG_RESTAURANT = "restaurant_id";
    private int cuisineID;
    private Integer[] restaurantsID = null;

    public FoodFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FoodFragment.
     */
    public static FoodFragment newInstance(int cuisineID, int restID) {
        FoodFragment fragment = new FoodFragment();
        //args
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_CUISINE, cuisineID);
        bundle.putInt(ARG_RESTAURANT, restID);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_food, container, false);
        Bundle bundle = getArguments();
        cuisineID = bundle.getInt(ARG_CUISINE, -42);
        restaurantsID = new Integer[1];
        restaurantsID[0] = bundle.getInt(ARG_RESTAURANT, -50);
        Log.d(TAG, "Cuisine ID from intent = " + cuisineID);
        Log.d(TAG, "Restaurant ID from intent = " + restaurantsID[0]);

        dastarhanApp = (DastarhanApp) getActivity().getApplication();

        mrvFood = (RecyclerView) view.findViewById(R.id.rvFood);
        mpbFood = (ProgressBar) view.findViewById(R.id.pbFood);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mrvFood.setLayoutManager(llm);

        handlerFood = new HandleFood();

        return view;
    }

    @Override
    public void onResume() {
        if (cuisineID != -50) {
            //get food from cuisine
            getFoodFromRestaurants();
        } else {
            //if we have that restaurant
            if (restaurantsID[0] != -50) {
                boolean needDownFoodFormServer = false;
                long currTime = System.currentTimeMillis();

                //check last downloaded time
                if (currTime > dastarhanApp.getCheckedFood().get(0).getTimeCheck() + Constants.FOOD_CHECKING_INTERVAL) {
                    for (FoodCheckElement foodCheckElement : dastarhanApp.getCheckedFood()) {
                        if (restaurantsID[0].equals(foodCheckElement.getRestID())
                                && currTime > foodCheckElement.getTimeCheck() + Constants.FOOD_CHECKING_INTERVAL) {
                            needDownFoodFormServer = true;
                            break;
                        }
                    }
                }

                if (needDownFoodFormServer) {
                    //get food from restaurant
                    GetFood gf = new GetFood(dastarhanApp, handlerFood, restaurantsID);
                    gf.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                } else {
                    Log.d(TAG, "Not need download food, time is not expired");
                    updateFood();
                }
            }
        }
        super.onResume();
    }

    private class HandleFood extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.CALLBACK_GET_FOOD_SUCCESS) {  //all ok
                Log.d(TAG, "Food data received");
                //update down time
                if (cuisineID == -42) {
                    //update first element "All restaurants"
                    dastarhanApp.getCheckedFood().get(0).setTimeCheck(System.currentTimeMillis());
                } else {
                    for (int i = 0; i < dastarhanApp.getCheckedFood().size(); i++) {
                        for (Integer restaurantID : restaurantsID) {
                            if (restaurantID.equals(dastarhanApp.getCheckedFood().get(i).getRestID())) {
                                dastarhanApp.getCheckedFood().get(i).setTimeCheck(System.currentTimeMillis());
                                break;
                            }
                        }
                    }
                }
                //update RecycleVew
                updateFood();
            }
            if (msg.what == Constants.CALLBACK_GET_FOOD_FAIL) {  //not ok
                Log.d(TAG, "Food data NOT receive");
                //update RecycleVew
                updateFood();
            }
            if (msg.what == Constants.CALLBACK_GET_FOOD_SHOW_PROGRESS_BAR || msg.what == Constants.CALLBACK_GET_RESTAURANTS_SHOW_PROGRESS_BAR) {  //show progress bar
                mrvFood.setVisibility(View.GONE);
                mpbFood.setVisibility(View.VISIBLE);
            }
            if (msg.what == Constants.CALLBACK_GET_RESTAURANTS_SUCCESS) {  //all ok
                Log.d(TAG, "Restaurants data received");
                //update RecycleVew
                getFoodFromRestaurants();
            }
            if (msg.what == Constants.CALLBACK_GET_RESTAURANTS_FAIL) {  //not ok
                Log.d(TAG, "Restaurants data NOT receive");
                //update RecycleVew
                getFoodFromRestaurants();
            }
            super.handleMessage(msg);
        }
    }

    private void getFoodFromRestaurants() {
        //get restaurants ID from cuisines
        RestaurantRepository restaurantRepo = dastarhanApp.component.restaurantRepository();
        List<Restaurant> restaurants;
        if (cuisineID == -42) {
            restaurants = restaurantRepo.getAll();
        } else {
            restaurants = restaurantRepo.getByCuisine(cuisineID);
        }
        if (restaurants.size() > 0) {
            List<Integer> restIDs = new ArrayList<>();
            for (Restaurant restaurant : restaurants) {
                restIDs.add(restaurant.getServer_id());
            }
            restaurantsID = restIDs.toArray(new Integer[restIDs.size()]);
        }

        //check last downloaded time
        boolean needDownFoodFromServer = false;
        long currTime = System.currentTimeMillis();

        if (currTime > dastarhanApp.getCheckedFood().get(0).getTimeCheck() + Constants.FOOD_CHECKING_INTERVAL) {
            //checking first element "All restaurants"
            for (FoodCheckElement foodCheckElement : dastarhanApp.getCheckedFood()) {
                for (Integer restaurantID : restaurantsID) {
                    if (restaurantID.equals(foodCheckElement.getRestID())
                            && currTime > foodCheckElement.getTimeCheck() + Constants.FOOD_CHECKING_INTERVAL) {
                        needDownFoodFromServer = true;
                        break;
                    }
                }
            }
        }

        if (needDownFoodFromServer) {
            if (restaurantsID.length > 0) {
                //get food from restaurant
                GetFood gf = new GetFood(dastarhanApp, handlerFood, restaurantsID);
                gf.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            } else {
                Log.d(TAG, "We have no restaurants with this cuisines");
                mpbFood.setVisibility(View.GONE);
                mrvFood.setVisibility(View.VISIBLE);
            }
        } else {
            Log.d(TAG, "Not need download food, time is not expired");
            updateFood();
        }
    }


    private void updateFood() {
        String locale = getContext().getResources().getConfiguration().locale.getLanguage();
        RVFoodAdapterMain adapter = new RVFoodAdapterMain(mFragmentNavigation, dastarhanApp, locale, restaurantsID);
        if (mrvFood.getAdapter() == null) {
            Log.d(TAG, "New adapter in mrvFood");
            mrvFood.setAdapter(adapter);
        } else {
            Log.d(TAG, "Swap adapter in mrvFood");
            mrvFood.swapAdapter(adapter, true);
        }

        mpbFood.setVisibility(View.GONE);
        mrvFood.setVisibility(View.VISIBLE);
    }
}
