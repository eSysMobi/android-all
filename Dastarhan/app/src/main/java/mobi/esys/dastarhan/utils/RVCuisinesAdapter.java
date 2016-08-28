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

import mobi.esys.dastarhan.DastarhanApp;
import mobi.esys.dastarhan.FoodActivity;
import mobi.esys.dastarhan.R;
import mobi.esys.dastarhan.Restaurants;
import mobi.esys.dastarhan.database.Cuisine;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVCuisinesAdapter extends RecyclerView.Adapter<RVCuisinesAdapter.CuisinesViewHolder> {
    private Context mContext;
    private String locale;
    private List<Cuisine> cuisines;

    //constructor
    public RVCuisinesAdapter(Context mContext, DastarhanApp dastarhanApp, String locale) {
        this.mContext = mContext;
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
                    Intent intent = new Intent(mContext, FoodActivity.class);
                    intent.putExtra("cuisineID", -42);
                    intent.putExtra("restID", -50);
                    mContext.startActivity(intent);
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d("dTagRecyclerView", "Choose ALL CUISINES in RecyclerView and go to Restaurants");
                    Intent intent = new Intent(mContext, Restaurants.class);
                    intent.putExtra("restID", -42);
                    mContext.startActivity(intent);
                    return true;
                }
            });

        } else {
            Cuisine cuisine = cuisines.get(i-1);
            viewHolder.cuisine_id = cuisine.getServer_id();
            if (locale.equals("ru")) {
                viewHolder.tvCuisine.setText(cuisine.getRu_name());
            } else {
                viewHolder.tvCuisine.setText(cuisine.getEn_name());
            }
            //viewHolder.ivCuisine.setImageBitmap(...);

            CustomClickListener customClickListener = new CustomClickListener(mContext, viewHolder.cuisine_id);
            viewHolder.itemView.setOnClickListener(customClickListener);

            CustomLongClickListener customLongClickListener = new CustomLongClickListener(mContext, viewHolder.cuisine_id);
            viewHolder.itemView.setOnLongClickListener(customLongClickListener);
        }
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
            Log.d("dTagRecyclerView", "Choose CUISINE in RecyclerView with id = " + id + " Go to food");
            Intent intent = new Intent(mContext, FoodActivity.class);
            intent.putExtra("cuisineID", id);
            intent.putExtra("restID", -50);
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
            Log.d("dTagRecyclerView", "Choose CUISINE in RecyclerView with id = " + id);
            Intent intent = new Intent(mContext, Restaurants.class);
            intent.putExtra("cuisineID", id);
            mContext.startActivity(intent);
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