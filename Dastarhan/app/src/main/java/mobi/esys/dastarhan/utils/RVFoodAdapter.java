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
import mobi.esys.dastarhan.R;
import mobi.esys.dastarhan.Restaurants;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVFoodAdapter extends RecyclerView.Adapter<RVFoodAdapter.FoodViewHolder> {
    private Cursor cursor;
    private Context mContext;
    private SQLiteDatabase db;
    private String locale;
    private int restID;

    //constructor
    public RVFoodAdapter(DatabaseHelper dbHelper, Context mContext, String locale, int restID) {
        this.mContext = mContext;
        this.locale = locale;
        this.restID = restID;
        db = dbHelper.getReadableDatabase();
        //cursor = db.query(Constants.DB_TABLE_FOOD, null, null, null, null, null, null);

        Log.d("dtagRecyclerView", "restID = " + String.valueOf(restID));

        String selectQuery;
        if (restID == -42) {
            selectQuery = "SELECT * FROM " + Constants.DB_TABLE_FOOD;
        } else {
            selectQuery = "SELECT * FROM " + Constants.DB_TABLE_FOOD + " WHERE res_id = " + restID;
        }

        cursor = db.rawQuery(selectQuery, null);
    }

    //preparing ViewHolder
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodNameRV;
        TextView tvFoodPriceRV;
        ImageView ivFoodRV;
        int food_id = 0;

        FoodViewHolder(View itemView) {
            super(itemView);
            tvFoodNameRV = (TextView) itemView.findViewById(R.id.tvFoodNameRV);
            tvFoodPriceRV = (TextView) itemView.findViewById(R.id.tvFoodPriceRV);
            ivFoodRV = (ImageView) itemView.findViewById(R.id.ivFoodRV);
        }
    }

    @Override
    public FoodViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_food_recyclerview, viewGroup, false);
        FoodViewHolder foodViewHolder = new FoodViewHolder(v);
        return foodViewHolder;
    }

    @Override
    public void onBindViewHolder(FoodViewHolder viewHolder, int i) {
        cursor.moveToPosition(i);
        viewHolder.food_id = cursor.getInt(cursor.getColumnIndex("server_id"));
        if (locale.equals("ru")) {
            viewHolder.tvFoodNameRV.setText(cursor.getString(cursor.getColumnIndex("ru_name")));
        } else {
            viewHolder.tvFoodNameRV.setText(cursor.getString(cursor.getColumnIndex("en_name")));
        }
        double price = cursor.getDouble(cursor.getColumnIndex("price"));
        viewHolder.tvFoodPriceRV.setText(String.valueOf(price));
        //viewHolder.ivCuisine.setImageBitmap(...);

        CustomClickListener customClickListener = new CustomClickListener(mContext, viewHolder.food_id);
        viewHolder.itemView.setOnClickListener(customClickListener);


        if (i == getItemCount() - 1) {
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
            Log.d("dtagRecyclerView", "Choose FOOD in RecyclerView with id = " + id);
//            Intent intent = new Intent(mContext, Restaurants.class);
//            intent.putExtra("cuisineID",id);
//            mContext.startActivity(intent);
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