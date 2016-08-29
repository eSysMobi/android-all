package mobi.esys.dastarhan.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mobi.esys.dastarhan.Constants;
import mobi.esys.dastarhan.CurrentFoodActivity;
import mobi.esys.dastarhan.DastarhanApp;
import mobi.esys.dastarhan.R;
import mobi.esys.dastarhan.database.Food;
import mobi.esys.dastarhan.database.FoodRepository;
import mobi.esys.dastarhan.database.RealmComponent;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVFoodAdapter extends RecyclerView.Adapter<RVFoodAdapter.FoodViewHolder> {
    private Context mContext;
    private RealmComponent component;
    private String locale;
    private byte action;
    private SharedPreferences prefs;
    private Handler handler;
    private double totalCost = 0;
    private Set<Integer> changeElement;
    private List<Boolean> needPromo;

    private List<Food> foods;


    //constructor
    public RVFoodAdapter(Context mContext, DastarhanApp dastarhanApp, String locale, byte action, Integer[] restIDs, Handler handler) {
        this.mContext = mContext;
        component = dastarhanApp.realmComponent();
        this.locale = locale;
        this.action = action;
        this.handler = handler;
        changeElement = new HashSet<>();
        needPromo = new ArrayList<>();
        prefs = mContext.getApplicationContext().getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE);

        FoodRepository foodRepo = component.foodRepository();
        switch (action) {
            case Constants.ACTION_GET_FOOD_FAVORITE:
                Log.d("dtagRecyclerView", "Retrieve favorite food");
                foods = foodRepo.getByFavorite();
                break;
            case Constants.ACTION_GET_FOOD_FROM_RESTAURANTS:

                if (restIDs[0] == -42) {
                    Log.d("dtagRecyclerView", "Retrieve food from all restaurants");
                    foods = foodRepo.getAll();
                } else {
                    Log.d("dtagRecyclerView", "Retrieve food from restaurants with IDs");
                    foods = foodRepo.getByRestaurantIDs(restIDs);
                }
                break;
            case Constants.ACTION_GET_FOOD_CURR_ORDERED:
                int numOrder = prefs.getInt(Constants.PREF_CURR_NUM_ORDER, 1);
                boolean executed = prefs.getBoolean(Constants.PREF_CURR_ORDER_EXECUTED, false);
                if (executed) {
                    numOrder = -1;
                }
                Log.d("dtagRecyclerView", "Retrieve ordered food with order num = " + numOrder);
//                SELECT
//                    а.имя_столбца1_таблицы_1,
//                    а.имя_столбца2_таблицы_1,
//                    б.имя_столбца1_таблицы_2,
//                    б.имя_столбца2_таблицы_2
//                FROM
//                    имя_таблицы_1 а, имя_таблицы_2 б
//                WHERE
//                    а.имя_столбца_по_которому_объединяем =
//                    б.имя_столбца_по_которому_объединяем;

                selectQuery =
                        "SELECT "
                                + "a.server_id as server_id, "
                                + "a.ru_name as ru_name, "
                                + "a.en_name as en_name, "
                                //+ "a.price as price, "
                                + "a.res_id as res_id, "
                                + "a.cat_id as cat_id, "
                                + "a.picture as picture, "
                                + "a.ru_descr as ru_descr, "
                                + "a.en_descr as en_descr, "
                                + "a.min_amount as min_amount, "
                                + "a.units as units, "
                                + "a.ordered as ordered, "
                                + "a.offer as offer, "
                                + "a.vegetarian as vegetarian, "
                                + "a.featured as featured, "
                                //+ "a.in_order as in_order, "
                                + "b.id_order as in_order, "
                                + "a.favorite as favorite, "
                                + "b.count as count, "
                                + "b.price as price, "
                                + "b.notice as notice "
                                + "FROM "
                                + Constants.DB_TABLE_FOOD + " a, "
                                + Constants.DB_TABLE_ORDERS + " b "
                                + "WHERE "
                                + "a.server_id = b.id_food "
                                + "AND "
                                + "b.id_order = " + numOrder;
                Log.d("dtagRecyclerView", "Query = " + selectQuery);
                break;
            default:
                selectQuery = "SELECT * FROM " + Constants.DB_TABLE_FOOD + " WHERE favorite = " + 1;
                break;
        }

        cursor = db.rawQuery(selectQuery, null);
    }

    //preparing ViewHolder
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodNameRV;
        TextView tvFoodPriceRV;
        ImageView ivFoodRV;
        ImageView ivFoodRVFav;
        ImageView ivFoodRVPromo;
        TextView tvFoodRVDescription;
        TextView tvFoodRVCount;
        Button bFoodRVToCart;
        LinearLayout bFoodRVAddRemoveFromCart;

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
        int in_order = 0;
        int count = 0;

        FoodViewHolder(View itemView) {
            super(itemView);
            tvFoodNameRV = (TextView) itemView.findViewById(R.id.tvFoodNameRV);
            tvFoodPriceRV = (TextView) itemView.findViewById(R.id.tvFoodPriceRV);
            ivFoodRV = (ImageView) itemView.findViewById(R.id.ivFoodRV);
            ivFoodRVFav = (ImageView) itemView.findViewById(R.id.ivFoodRVFav);
            ivFoodRVPromo = (ImageView) itemView.findViewById(R.id.ivFoodRVPromo);
            tvFoodRVDescription = (TextView) itemView.findViewById(R.id.tvFoodRVDescription);

            tvFoodRVCount = (TextView) itemView.findViewById(R.id.tvFoodRVCount);
            bFoodRVToCart = (Button) itemView.findViewById(R.id.bFoodRVToCart);
            bFoodRVAddRemoveFromCart = (LinearLayout) itemView.findViewById(R.id.bFoodRVAddRemoveFromCart);
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
        viewHolder.price = cursor.getDouble(cursor.getColumnIndex("price"));
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
        viewHolder.in_order = cursor.getInt(cursor.getColumnIndexOrThrow("in_order"));
        viewHolder.favorite = cursor.getInt(cursor.getColumnIndex("favorite"));

        //viewHolder.ivCuisine.setImageBitmap(...);

        if (changeElement.contains(i)) {
            if (viewHolder.favorite != 1) {
                viewHolder.favorite = 1;
            } else {
                viewHolder.favorite = 0;
            }
        }

        if (viewHolder.favorite != 1) {
            viewHolder.ivFoodRVFav.setVisibility(View.GONE);
        } else {
            viewHolder.ivFoodRVFav.setVisibility(View.VISIBLE);
        }

        int numOrder = prefs.getInt(Constants.PREF_CURR_NUM_ORDER, 1);
        if (viewHolder.in_order == numOrder) {
            viewHolder.bFoodRVToCart.setVisibility(View.GONE);
            viewHolder.bFoodRVAddRemoveFromCart.setVisibility(View.VISIBLE);
        } else {
            viewHolder.bFoodRVAddRemoveFromCart.setVisibility(View.GONE);
            viewHolder.bFoodRVToCart.setVisibility(View.VISIBLE);
        }

        if (action == Constants.ACTION_GET_FOOD_CURR_ORDERED) {
            viewHolder.count = cursor.getInt(cursor.getColumnIndex("count"));
            //set name
            if (locale.equals("ru")) {
                viewHolder.tvFoodNameRV.setText(viewHolder.ru_name);
                viewHolder.tvFoodRVDescription.setText(viewHolder.ru_descr);
            } else {
                viewHolder.tvFoodNameRV.setText(viewHolder.en_name);
                viewHolder.tvFoodRVDescription.setText(viewHolder.en_descr);
            }
            viewHolder.tvFoodRVCount.setText(String.valueOf(viewHolder.count));

            //set price
            viewHolder.tvFoodPriceRV.setText(String.valueOf(viewHolder.price * viewHolder.count) + " р.");
        } else {
            //set name
            if (locale.equals("ru")) {
                viewHolder.tvFoodNameRV.setText(viewHolder.ru_name);
                viewHolder.tvFoodRVDescription.setText(viewHolder.ru_descr);
            } else {
                viewHolder.tvFoodNameRV.setText(viewHolder.en_name);
                viewHolder.tvFoodRVDescription.setText(viewHolder.en_descr);
            }

            //set price
            viewHolder.tvFoodPriceRV.setText(String.valueOf(viewHolder.price) + " р.");
        }

        //promo
        if (needPromo.size() <= i) {
            String selectQuery_promo = "SELECT * "
                    + "FROM "
                    + Constants.DB_TABLE_PROMO
                    + " WHERE "
                    + "(condition = 2 OR condition = 3)"
                    + "AND condition_par LIKE \"%" + String.valueOf(viewHolder.food_id) + "%\"";
            Cursor cursor_promo = db.rawQuery(selectQuery_promo, null);
            if (cursor_promo.getCount() > 0) {
                needPromo.add(true);
            } else {
                needPromo.add(false);
            }
            cursor_promo.close();
        }
        if (needPromo.get(i)) {
            viewHolder.ivFoodRVPromo.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivFoodRVPromo.setVisibility(View.GONE);
        }

        GoToFullFood goToFullFood = new GoToFullFood(mContext, viewHolder.food_id);
        viewHolder.itemView.setOnClickListener(goToFullFood);

        AddRemoveFav addRemoveFav = new AddRemoveFav(dbHelper, viewHolder, i, action, handler, changeElement);
        viewHolder.itemView.setOnLongClickListener(addRemoveFav);

        //TODO
        //viewHolder.bFoodRVToCart.

        if (i == getItemCount() - 1) {
            //cursor.close();
            db.close();
        }

        if (action == Constants.ACTION_GET_FOOD_CURR_ORDERED) {
            totalCost = totalCost + viewHolder.price * viewHolder.count;
            if (i == getItemCount() - 1) {
                Message message = Message.obtain();
                message.setTarget(handler);
                message.what = 43;
                Bundle bundle = new Bundle();
                bundle.putDouble("totalCost", totalCost);
                message.setData(bundle);
                message.sendToTarget();
            }
        }
    }

    private static class DeleteFromCart implements View.OnClickListener {
        DatabaseHelper dbHelper;
        FoodViewHolder viewHolder;
        int elementNum;
        byte action;
        Handler handler;
        Set<Integer> changeElement;


        public DeleteFromCart(DatabaseHelper dbHelper, FoodViewHolder viewHolder, int elementNum, byte action, Handler handler, Set<Integer> changeElement) {
            this.dbHelper = dbHelper;
            this.viewHolder = viewHolder;
            this.action = action;
            this.handler = handler;
            this.elementNum = elementNum;
            this.changeElement = changeElement;
        }

        @Override
        public void onClick(View v) {
            if (viewHolder.in_order > 0) {
                Log.d("dtagRecyclerView", "Delete food from cart");
                int numOrder = viewHolder.in_order;

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
                cv.put("favorite", viewHolder.favorite);
                cv.put("featured", viewHolder.featured);
                cv.put("in_order", 0);
                int result = db.update(Constants.DB_TABLE_FOOD, cv, "server_id=" + viewHolder.food_id + " and res_id=" + viewHolder.res_id, null);
                Log.d("dtagRecyclerView", "Food deleted from current order = " + result);

                int rowID = db.delete(Constants.DB_TABLE_ORDERS, "id_order = " + numOrder + " AND id_food= " + viewHolder.food_id, null);
                Log.d("dtagRecyclerView", "row order deleted, ID = " + rowID);
                db.close();

                //viewHolder.ivFoodRVCart.setVisibility(View.GONE);
                viewHolder.in_order = 0;

                //update list
                handler.sendEmptyMessage(42);
            }
        }
    }

    private static class GoToFullFood implements View.OnClickListener {
        private int id;
        private Context mContext;

        public GoToFullFood(Context mContext, int id) {
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

    private static class AddRemoveFav implements View.OnLongClickListener {
        DatabaseHelper dbHelper;
        FoodViewHolder viewHolder;
        int elementNum;
        byte action;
        Handler handler;
        Set<Integer> changeElement;


        public AddRemoveFav(DatabaseHelper dbHelper, FoodViewHolder viewHolder, int elementNum, byte action, Handler handler, Set<Integer> changeElement) {
            this.dbHelper = dbHelper;
            this.viewHolder = viewHolder;
            this.action = action;
            this.handler = handler;
            this.elementNum = elementNum;
            this.changeElement = changeElement;
        }

        @Override
        public boolean onLongClick(View v) {
            if (action == Constants.ACTION_GET_FOOD_CURR_ORDERED) {
                //nothing
            } else {
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
                if (viewHolder.favorite != 1) {
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
                cv.put("in_order", viewHolder.in_order);
                int result = db.update(Constants.DB_TABLE_FOOD, cv, "server_id=" + viewHolder.food_id + " and res_id=" + viewHolder.res_id, null);
                Log.d("dtagRecyclerView", "Fav updated = " + result);
                db.close();
                if (changeElement.contains(elementNum)) {
                    changeElement.remove(elementNum);
                } else {
                    changeElement.add(elementNum);
                }
            }
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