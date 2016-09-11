package mobi.esys.dastarhan.utils;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import mobi.esys.dastarhan.BaseFragment.FragmentNavigation;
import mobi.esys.dastarhan.DastarhanApp;
import mobi.esys.dastarhan.FoodFragment;
import mobi.esys.dastarhan.R;
import mobi.esys.dastarhan.Restaurants;
import mobi.esys.dastarhan.database.Cuisine;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVCuisinesAdapter extends RecyclerView.Adapter<RVCuisinesAdapter.CuisinesViewHolder> {
    private String locale;
    private List<Cuisine> cuisines;
    private FragmentNavigation navigation;

    //constructor
    public RVCuisinesAdapter(FragmentNavigation navigation, DastarhanApp dastarhanApp, String locale) {
        this.navigation = navigation;
        this.locale = locale;
        cuisines = dastarhanApp.realmComponent().cuisineRepository().getAll();
    }

    //preparing ViewHolder
    public static class CuisinesViewHolder extends RecyclerView.ViewHolder {
        TextView tvCuisine;
        ImageView ivCuisine;
        int cuisine_id = 0;

        CuisinesViewHolder(View itemView) {
            super(itemView);
            tvCuisine = (TextView) itemView.findViewById(R.id.tvCuisine);
            ivCuisine = (ImageView) itemView.findViewById(R.id.ivCuisine);
        }
    }

    @Override
    public RVCuisinesAdapter.CuisinesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_cuisine_recyclerview, viewGroup, false);
        CuisinesViewHolder cuisinesViewHolder = new CuisinesViewHolder(v);
        return cuisinesViewHolder;
    }

    @Override
    public void onBindViewHolder(RVCuisinesAdapter.CuisinesViewHolder viewHolder, int i) {
        if (i == 0) {
            viewHolder.tvCuisine.setText(R.string.all_cuisines);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("dTagRecyclerView", "Choose ALL CUISINES in RecyclerView and go to Food");
                    FoodFragment fragment = FoodFragment.newInstance(-42, -50);
                    navigation.replaceFragment(fragment, "Все блюда");
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d("dTagRecyclerView", "Choose ALL CUISINES in RecyclerView and go to Restaurants");
                    Restaurants fragment = Restaurants.newInstance(-42);
                    navigation.replaceFragment(fragment, "Все зестораны");
                    return true;
                }
            });

        } else {
            Cuisine cuisine = cuisines.get(i - 1);
            viewHolder.cuisine_id = cuisine.getServer_id();
            String cuisineName = "";
            if (locale.equals("ru")) {
                cuisineName = cuisine.getRu_name();
            } else {
                cuisineName = cuisine.getEn_name();
            }
            viewHolder.tvCuisine.setText(cuisineName);

            //viewHolder.ivCuisine.setImageBitmap(...);

            CustomClickListener customClickListener = new CustomClickListener(navigation, viewHolder.cuisine_id, cuisineName);
            viewHolder.itemView.setOnClickListener(customClickListener);

            CustomLongClickListener customLongClickListener = new CustomLongClickListener(navigation, viewHolder.cuisine_id);
            viewHolder.itemView.setOnLongClickListener(customLongClickListener);
        }
    }

    private static class CustomClickListener implements View.OnClickListener {
        private int id;
        private FragmentNavigation navigation;
        String cuisineName;

        public CustomClickListener(FragmentNavigation navigation, int id, String cuisineName) {
            this.navigation = navigation;
            this.id = id;
            this.cuisineName = cuisineName;
        }

        @Override
        public void onClick(View v) {
            Log.d("dTagRecyclerView", "Choose CUISINE in RecyclerView with id = " + id + " Go to food");
            FoodFragment fragment = FoodFragment.newInstance(id, -50);
            navigation.replaceFragment(fragment, cuisineName);
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
            Log.d("dTagRecyclerView", "Choose CUISINE in RecyclerView with id = " + id);
            Restaurants fragment = Restaurants.newInstance(id);
            navigation.replaceFragment(fragment, "Ресторан");
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return cuisines.size() + 1;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}