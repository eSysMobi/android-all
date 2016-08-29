package mobi.esys.dastarhan.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.dastarhan.CurrentRestaurantActivity;
import mobi.esys.dastarhan.DastarhanApp;
import mobi.esys.dastarhan.FoodActivity;
import mobi.esys.dastarhan.R;
import mobi.esys.dastarhan.database.Cuisine;
import mobi.esys.dastarhan.database.RealmComponent;
import mobi.esys.dastarhan.database.Restaurant;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVRestaurantsAdapter extends RecyclerView.Adapter<RVRestaurantsAdapter.RestaurantViewHolder> {
    private final String TAG = "dtagRVRestAdapter";

    private Context mContext;
    private String locale;
    private List<Restaurant> restaurants;
    private RealmComponent component;

    //constructor
    public RVRestaurantsAdapter(Context mContext, DastarhanApp dastarhanApp, String locale, int cuisineID) {
        this.mContext = mContext;
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
        if (locale.equals("ru")) {
            viewHolder.tvRestaurant.setText(restaurant.getRu_name());
        } else {
            viewHolder.tvRestaurant.setText(restaurant.getEn_name());
        }

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

        CustomClickListener customClickListener = new CustomClickListener(mContext, viewHolder.restaraunt_id);
        viewHolder.itemView.setOnClickListener(customClickListener);

        CustomLongClickListener customLongClickListener = new CustomLongClickListener(mContext, viewHolder.restaraunt_id);
        viewHolder.itemView.setOnLongClickListener(customLongClickListener);
    }

    private static class CustomClickListener implements View.OnClickListener {
        private int id;
        private Context mContext;

        public CustomClickListener(Context mContext, int id) {
            this.mContext = mContext;
            this.id = id;
        }

        @Override
        public void onClick(View v) {
            Log.d("dtagRecyclerView", "Click RESTARAUNT in RecyclerView with id = " + id);
            Intent intent = new Intent(mContext, FoodActivity.class);
            intent.putExtra("restID", id);
            intent.putExtra("cuisineID", -50);
            mContext.startActivity(intent);
        }
    }

    private static class CustomLongClickListener implements View.OnLongClickListener {
        private int id;
        private Context mContext;

        public CustomLongClickListener(Context mContext, int id) {
            this.mContext = mContext;
            this.id = id;
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d("dtagRecyclerView", "Long click RESTARAUNT in RecyclerView with id = " + id);

            Intent intent = new Intent(mContext, CurrentRestaurantActivity.class);
            intent.putExtra("restID", id);
            mContext.startActivity(intent);
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