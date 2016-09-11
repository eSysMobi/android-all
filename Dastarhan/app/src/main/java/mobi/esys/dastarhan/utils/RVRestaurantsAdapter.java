package mobi.esys.dastarhan.utils;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


import mobi.esys.dastarhan.CurrentRestaurantFragment;
import mobi.esys.dastarhan.DastarhanApp;
import mobi.esys.dastarhan.FoodFragment;
import mobi.esys.dastarhan.R;
import mobi.esys.dastarhan.database.Cuisine;
import mobi.esys.dastarhan.database.RealmComponent;
import mobi.esys.dastarhan.database.Restaurant;
import mobi.esys.dastarhan.BaseFragment.FragmentNavigation;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVRestaurantsAdapter extends RecyclerView.Adapter<RVRestaurantsAdapter.RestaurantViewHolder> {
    private final String TAG = "dtagRVRestAdapter";

    private FragmentNavigation navigation;
    private String locale;
    private List<Restaurant> restaurants;
    private RealmComponent component;

    //constructor
    public RVRestaurantsAdapter(FragmentNavigation navigation, DastarhanApp dastarhanApp, String locale, int cuisineID) {
        this.navigation = navigation;
        this.locale = locale;
        component = dastarhanApp.realmComponent();

        Log.d("dtagRecyclerView", "cuisineID = " + String.valueOf(cuisineID));

        String selectQuery;
        if (cuisineID == -42) {
            restaurants = component.restaurantRepository().getAll();
        } else {
            restaurants = component.restaurantRepository().getByCuisine(cuisineID);
        }
    }

    //preparing ViewHolder
    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView tvRestaurant;
        ImageView ivRestaurant;
        TextView tvRestaurantCuisines;
        TextView tvMinOrderSum;
        TextView tvAverageTime;
        TextView tvWorkingHours;
        int restaraunt_id = 0;

        RestaurantViewHolder(View itemView) {
            super(itemView);
            tvRestaurant = (TextView) itemView.findViewById(R.id.tvRestaurant);
            ivRestaurant = (ImageView) itemView.findViewById(R.id.ivCurrFoodRest);
            tvRestaurantCuisines = (TextView) itemView.findViewById(R.id.tvRestaurantCuisines);
            tvMinOrderSum = (TextView) itemView.findViewById(R.id.tvMinOrderSum);
            tvAverageTime = (TextView) itemView.findViewById(R.id.tvAverageTime);
            tvWorkingHours = (TextView) itemView.findViewById(R.id.tvWorkingHours);
        }
    }

    @Override
    public RVRestaurantsAdapter.RestaurantViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_restautant_recyclerview, viewGroup, false);
        RestaurantViewHolder restaurantViewHolder = new RestaurantViewHolder(v);
        return restaurantViewHolder;
    }

    @Override
    public void onBindViewHolder(RVRestaurantsAdapter.RestaurantViewHolder viewHolder, int i) {
        Restaurant restaurant = restaurants.get(i);

        viewHolder.restaraunt_id = restaurant.getServer_id();
        String restName = "";
        if (locale.equals("ru")) {
            restName = restaurant.getRu_name();
        } else {
            restName = restaurant.getEn_name();
        }
        viewHolder.tvRestaurant.setText(restName);

        String cuisinesNames = "";
        String rawCuisines = restaurant.getCuisines();
        String[] cuisinesIDs = rawCuisines.split(",");
        for (String cuisineID : cuisinesIDs) {
            try {
                int id = Integer.parseInt(cuisineID.trim());
                Cuisine cuisine = component.cuisineRepository().getById(id);
                if(cuisine!= null) {
                    String name = "";
                    if (locale.equals("ru")) {
                        name = cuisine.getRu_name();
                    } else {
                        name = cuisine.getEn_name();
                    }
                    //add cuisine name to cuisines list
                    if(!cuisinesNames.isEmpty()){
                        cuisinesNames = cuisinesNames.concat(" / ");
                    }
                    cuisinesNames = cuisinesNames.concat(name);
                }
            } catch (Exception e) {
                Log.d(TAG, "Can't set cuisine name to RV in restaurant list");
            }
        }
        //set result cuisines list
        viewHolder.tvRestaurantCuisines.setText(cuisinesNames);

        //viewHolder.tvMinOrderSum.setText(cursor.getInt(cursor.getColumnIndex("min_order")));
        //TODO tvAverageTime
        String time1 = restaurant.getTime1();
        String time2 = restaurant.getTime2();
        String time = time1.substring(0, 5) + "-" + time2.substring(0, 5);
        viewHolder.tvWorkingHours.setText(time);

        //viewHolder.ivCuisine.setImageBitmap(...);

        CustomClickListener customClickListener = new CustomClickListener(navigation, viewHolder.restaraunt_id, restName);
        viewHolder.itemView.setOnClickListener(customClickListener);

        CustomLongClickListener customLongClickListener = new CustomLongClickListener(navigation, viewHolder.restaraunt_id);
        viewHolder.itemView.setOnLongClickListener(customLongClickListener);
    }

    private static class CustomClickListener implements View.OnClickListener {
        private int id;
        private FragmentNavigation navigation;
        String restName;

        public CustomClickListener(FragmentNavigation navigation, int id, String restName) {
            this.navigation = navigation;
            this.id = id;
            this.restName = restName;
        }

        @Override
        public void onClick(View v) {
            Log.d("dtagRecyclerView", "Click RESTARAUNT in RecyclerView with id = " + id);
            FoodFragment fragment = FoodFragment.newInstance(-50,id);
            navigation.replaceFragment(fragment, "Блюда " + restName);
        }
    }

    private static class CustomLongClickListener implements View.OnLongClickListener {
        private int id;
        private FragmentNavigation navigation;

        public CustomLongClickListener(FragmentNavigation navigation, int id) {
            this.navigation = navigation;
            this.id = id;
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d("dtagRecyclerView", "Long click RESTARAUNT in RecyclerView with id = " + id);
            CurrentRestaurantFragment fragment = CurrentRestaurantFragment.newInstance(id);
            navigation.replaceFragment(fragment, "Ресторан");
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}