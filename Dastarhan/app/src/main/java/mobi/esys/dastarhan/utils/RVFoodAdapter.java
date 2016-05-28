package mobi.esys.dastarhan.utils;

import android.content.ContentValues;
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
import mobi.esys.dastarhan.CurrentFoodActivity;
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
    private DatabaseHelper dbHelper;

    //constructor
    public RVFoodAdapter(DatabaseHelper dbHelper, Context mContext, String locale, boolean needOnlyFavorites, int restID) {
        this.mContext = mContext;
        this.locale = locale;
        this.dbHelper = dbHelper;;
        db = dbHelper.getReadableDatabase();

        String selectQuery;
        if (!needOnlyFavorites) {
            Log.d("dtagRecyclerView", "restID = " + String.valueOf(restID));

            if (restID == -42) {
                selectQuery = "SELECT * FROM " + Constants.DB_TABLE_FOOD;
            } else {
                selectQuery = "SELECT * FROM " + Constants.DB_TABLE_FOOD + " WHERE res_id = " + restID;
            }
        } else {
            Log.d("dtagRecyclerView", "Need only favorites ");
            selectQuery = "SELECT * FROM " + Constants.DB_TABLE_FOOD + " WHERE favorite = " + 1;
        }

        cursor = db.rawQuery(selectQuery, null);
    }

    //preparing ViewHolder
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodNameRV;
        TextView tvFoodPriceRV;
        ImageView ivFoodRV;
        ImageView ivFoodRVFav;

        int food_id = 0;
        int res_id = 0;
        int cat_id = 0;
        String ru_name = "";
        String en_name = "";
        String picture = "";
        String ru_descr = "";
        String en_descr = "";
        double price = 0;
        int min_amount = 0;
        String units = "";
        int ordered = 0;
        int offer = 0;
        int vegetarian = 0;
        int favorite = 0;
        int featured = 0;

        FoodViewHolder(View itemView) {
            super(itemView);
            tvFoodNameRV = (TextView) itemView.findViewById(R.id.tvFoodNameRV);
            tvFoodPriceRV = (TextView) itemView.findViewById(R.id.tvFoodPriceRV);
            ivFoodRV = (ImageView) itemView.findViewById(R.id.ivFoodRV);
            ivFoodRVFav = (ImageView) itemView.findViewById(R.id.ivFoodRVFav);
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
        viewHolder.ru_name = cursor.getString(cursor.getColumnIndex("ru_name"));
        viewHolder.en_name = cursor.getString(cursor.getColumnIndex("en_name"));
        if (locale.equals("ru")) {
            viewHolder.tvFoodNameRV.setText(viewHolder.ru_name);
        } else {
            viewHolder.tvFoodNameRV.setText(viewHolder.en_name);
        }
        viewHolder.price = cursor.getDouble(cursor.getColumnIndex("price"));
        viewHolder.tvFoodPriceRV.setText(String.valueOf(viewHolder.price));
        //viewHolder.ivCuisine.setImageBitmap(...);

        viewHolder.res_id = cursor.getInt(cursor.getColumnIndexOrThrow("res_id"));
        viewHolder.cat_id = cursor.getInt(cursor.getColumnIndexOrThrow("cat_id"));
        viewHolder.picture = cursor.getString(cursor.getColumnIndexOrThrow("picture"));
        viewHolder.ru_descr = cursor.getString(cursor.getColumnIndexOrThrow("ru_descr"));
        viewHolder.en_descr = cursor.getString(cursor.getColumnIndexOrThrow("en_descr"));
        viewHolder.min_amount = cursor.getInt(cursor.getColumnIndexOrThrow("min_amount"));
        viewHolder.units = cursor.getString(cursor.getColumnIndexOrThrow("units"));
        viewHolder.ordered = cursor.getInt(cursor.getColumnIndexOrThrow("ordered"));
        viewHolder.offer = cursor.getInt(cursor.getColumnIndexOrThrow("offer"));
        viewHolder.vegetarian = cursor.getInt(cursor.getColumnIndexOrThrow("vegetarian"));
        viewHolder.featured = cursor.getInt(cursor.getColumnIndexOrThrow("featured"));

        viewHolder.favorite = cursor.getInt(cursor.getColumnIndex("favorite"));
        if (viewHolder.favorite!=1){
            viewHolder.ivFoodRVFav.setVisibility(View.GONE);
        } else {
            viewHolder.ivFoodRVFav.setVisibility(View.VISIBLE);
        }

        CustomClickListener customClickListener = new CustomClickListener(mContext, viewHolder.food_id);
        viewHolder.itemView.setOnClickListener(customClickListener);

        CustomLongClickListener customLongClickListener = new CustomLongClickListener(cursor, dbHelper, viewHolder);
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
            Log.d("dtagRecyclerView", "Choose FOOD in RecyclerView with id = " + id);
            Intent intent = new Intent(mContext, CurrentFoodActivity.class);
            intent.putExtra("currentFoodID", id);
            mContext.startActivity(intent);
        }
    }

    private static class CustomLongClickListener implements View.OnLongClickListener{
        Cursor cursor;
        DatabaseHelper dbHelper;
        FoodViewHolder viewHolder;

        public CustomLongClickListener(Cursor cursor, DatabaseHelper dbHelper, FoodViewHolder viewHolder){
            this.cursor = cursor;
            this.dbHelper = dbHelper;
            this.viewHolder = viewHolder;
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d("dtagRecyclerView", "Start update fav");
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("server_id", viewHolder.food_id);
            cv.put("res_id", viewHolder.res_id);
            cv.put("cat_id", viewHolder.cat_id);
            cv.put("ru_name", viewHolder.ru_name);
            cv.put("en_name", viewHolder.en_name);
            cv.put("picture", viewHolder.picture);
            cv.put("ru_descr", viewHolder.ru_descr);
            cv.put("en_descr", viewHolder.en_descr);
            cv.put("price", viewHolder.price);
            cv.put("min_amount", viewHolder.min_amount);
            cv.put("units", viewHolder.units);
            cv.put("ordered", viewHolder.ordered);
            cv.put("offer", viewHolder.offer);
            cv.put("vegetarian", viewHolder.vegetarian);
            if (viewHolder.favorite!=1){
                //set fav to 1
                viewHolder.favorite = 1;
                cv.put("favorite", viewHolder.favorite);
                viewHolder.ivFoodRVFav.setVisibility(View.VISIBLE);
            } else {
                //set fa to 0
                viewHolder.favorite = 0;
                cv.put("favorite", viewHolder.favorite);
                viewHolder.ivFoodRVFav.setVisibility(View.GONE);
            }
            cv.put("featured", viewHolder.featured);
            int result = db.update(Constants.DB_TABLE_FOOD, cv, "server_id=" + viewHolder.food_id + " and res_id=" + viewHolder.res_id, null);
            Log.d("dtagRecyclerView", "Fav updated = " + result);
            db.close();
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