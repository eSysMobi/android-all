package mobi.esys.dastarhan.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import mobi.esys.dastarhan.Constants;
import mobi.esys.dastarhan.CurrentRestaurantActivity;
import mobi.esys.dastarhan.FoodActivity;
import mobi.esys.dastarhan.R;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVRestaurantsAdapter extends RecyclerView.Adapter<RVRestaurantsAdapter.RestaurantViewHolder> {
    private Cursor cursor;
    private Context mContext;
    private SQLiteDatabase db;
    private String locale;

    //constructor
    public RVRestaurantsAdapter(DatabaseHelper dbHelper, Context mContext, String locale, int cuisineID) {
        this.mContext = mContext;
        this.locale = locale;
        db = dbHelper.getReadableDatabase();

        Log.d("dtagRecyclerView", "cuisineID = " + String.valueOf(cuisineID));

        String selectQuery;
        if (cuisineID == -42) {
            selectQuery = "SELECT * FROM " + Constants.DB_TABLE_RESTAURANTS;
        } else {
            selectQuery = "SELECT * FROM " + Constants.DB_TABLE_RESTAURANTS + " WHERE cuisines LIKE \"%" + String.valueOf(cuisineID) + "%\"";
        }

        cursor = db.rawQuery(selectQuery, null);
    }

    //preparing ViewHolder
    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView tvRestaurant;
        ImageView ivRestaurant;
        ImageView ivRatingRestaurant;
        int restaraunt_id = 0;
        int restaraunt_rating = 5;

        RestaurantViewHolder(View itemView) {
            super(itemView);
            tvRestaurant = (TextView) itemView.findViewById(R.id.tvRestaurant);
            ivRestaurant = (ImageView) itemView.findViewById(R.id.ivCurrFoodRest);
            ivRatingRestaurant = (ImageView) itemView.findViewById(R.id.ivCurrFoodRatingRest);
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
        cursor.moveToPosition(i);
        viewHolder.restaraunt_id = cursor.getInt(cursor.getColumnIndex("server_id"));
        if (locale.equals("ru")) {
            viewHolder.tvRestaurant.setText(cursor.getString(cursor.getColumnIndex("ru_name")));
        } else {
            viewHolder.tvRestaurant.setText(cursor.getString(cursor.getColumnIndex("en_name")));
        }

        viewHolder.restaraunt_rating = cursor.getInt(cursor.getColumnIndex("total_rating"));
        switch (viewHolder.restaraunt_rating) {
            case 0:
                viewHolder.ivRatingRestaurant.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_0));
                break;
            case 1:
                viewHolder.ivRatingRestaurant.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_1));
                break;
            case 2:
                viewHolder.ivRatingRestaurant.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_2));
                break;
            case 3:
                viewHolder.ivRatingRestaurant.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_3));
                break;
            case 4:
                viewHolder.ivRatingRestaurant.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_4));
                break;
            case 5:
                viewHolder.ivRatingRestaurant.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_5));
                break;
            case 6:
                viewHolder.ivRatingRestaurant.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_6));
                break;
            case 7:
                viewHolder.ivRatingRestaurant.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_7));
                break;
            case 8:
                viewHolder.ivRatingRestaurant.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_8));
                break;
            case 9:
                viewHolder.ivRatingRestaurant.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_9));
                break;
            case 10:
                viewHolder.ivRatingRestaurant.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_10));
                break;
        }
        //viewHolder.ivCuisine.setImageBitmap(...);

        CustomClickListener customClickListener = new CustomClickListener(mContext, viewHolder.restaraunt_id);
        viewHolder.itemView.setOnClickListener(customClickListener);

        CustomLongClickListener customLongClickListener = new CustomLongClickListener(mContext, viewHolder.restaraunt_id);
        viewHolder.itemView.setOnLongClickListener(customLongClickListener);

        if (i == getItemCount() - 1) {
            //cursor.close();
            db.close();
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
            Log.d("dtagRecyclerView", "Click RESTARAUNT in RecyclerView with id = " + id);
            Intent intent = new Intent(mContext, FoodActivity.class);
            intent.putExtra("restID",id);
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
            intent.putExtra("restID",id);
            mContext.startActivity(intent);
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}