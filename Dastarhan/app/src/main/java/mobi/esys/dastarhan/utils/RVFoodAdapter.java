package mobi.esys.dastarhan.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.FrameLayout;
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
import mobi.esys.dastarhan.database.Cart;
import mobi.esys.dastarhan.database.Food;
import mobi.esys.dastarhan.database.FoodRepository;
import mobi.esys.dastarhan.database.Order;
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

    private List<Food> foods;
    private List<Order> currOrders;
    private Cart cart;


    //constructor
    public RVFoodAdapter(Context mContext, DastarhanApp dastarhanApp, String locale, byte action, Integer[] restIDs, Handler handler) {
        this.mContext = mContext;
        component = dastarhanApp.realmComponent();
        this.locale = locale;
        this.action = action;
        this.handler = handler;
        changeElement = new HashSet<>();
        foods = new ArrayList<>();
        prefs = mContext.getApplicationContext().getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE);

        cart = component.cartRepository().get();
        currOrders = component.cartRepository().getCurrentCartOrders();
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
                Log.d("dtagRecyclerView", "Retrieve food from restaurants with IDs");
                if (currOrders.size() > 0) {
                    Integer[] orderIDs = new Integer[currOrders.size()];
                    for (int i = 0; i < currOrders.size(); i++)
                        orderIDs[i] = currOrders.get(i).getId_food();
                    foods = foodRepo.getByIds(orderIDs);
                }
                break;
            default:
                Log.d("dtagRecyclerView", "(DEFAULT) Retrieve favorite food");
                foods = foodRepo.getByFavorite();
                break;
        }
    }

    //preparing ViewHolder
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodNameRV;
        TextView tvFoodPriceRV;
        ImageView ivFoodRV;
        ImageView ivFoodRVFav;
        ImageView ivFoodRVPromo;
        TextView tvFoodRVDescription;

        Button bFoodRVToCart;

        LinearLayout bFoodRVAddRemoveFromCart;
        TextView tvFoodRVCount;
        FrameLayout bCartRemoveOne;
        FrameLayout bCartAddOne;

        Food food;

        FoodViewHolder(View itemView) {
            super(itemView);
            tvFoodNameRV = (TextView) itemView.findViewById(R.id.tvFoodNameRV);
            tvFoodPriceRV = (TextView) itemView.findViewById(R.id.tvFoodPriceRV);
            ivFoodRV = (ImageView) itemView.findViewById(R.id.ivFoodRV);
            ivFoodRVFav = (ImageView) itemView.findViewById(R.id.ivFoodRVFav);
            ivFoodRVPromo = (ImageView) itemView.findViewById(R.id.ivFoodRVPromo);
            tvFoodRVDescription = (TextView) itemView.findViewById(R.id.tvFoodRVDescription);

            bFoodRVToCart = (Button) itemView.findViewById(R.id.bFoodRVToCart);

            bFoodRVAddRemoveFromCart = (LinearLayout) itemView.findViewById(R.id.bFoodRVAddRemoveFromCart);
            tvFoodRVCount = (TextView) itemView.findViewById(R.id.tvFoodRVCount);
            bCartRemoveOne = (FrameLayout) itemView.findViewById(R.id.bCartRemoveOne);
            bCartAddOne = (FrameLayout) itemView.findViewById(R.id.bCartAddOne);
        }
    }

    @Override
    public FoodViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_food_recyclerview, viewGroup, false);
        return new FoodViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FoodViewHolder viewHolder, int i) {
        viewHolder.food = foods.get(i);

        if (viewHolder.food.isFavorite()) {
            viewHolder.ivFoodRVFav.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivFoodRVFav.setVisibility(View.GONE);
        }

        //set name
        //set description
        if (locale.equals("ru")) {
            viewHolder.tvFoodNameRV.setText(viewHolder.food.getRu_name());
            viewHolder.tvFoodRVDescription.setText(viewHolder.food.getRu_descr());
        } else {
            viewHolder.tvFoodNameRV.setText(viewHolder.food.getEn_name());
            viewHolder.tvFoodRVDescription.setText(viewHolder.food.getEn_descr());
        }

        //set price
        viewHolder.tvFoodPriceRV.setText(String.valueOf(viewHolder.food.getPrice()) + " р.");

        //TODO set image
        //viewHolder.bFoodRVToCart.


        //promo
        //TODO check and set promo in RVFoodAdapter
        boolean needPromo = false;
        if (needPromo) {
            viewHolder.ivFoodRVPromo.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivFoodRVPromo.setVisibility(View.GONE);
        }

        GoToFullFood goToFullFood = new GoToFullFood(mContext, viewHolder.food.getServer_id());
        viewHolder.itemView.setOnClickListener(goToFullFood);

        //set type of buttons
        Order orderThisFood = null;
        for (int j = 0; j < currOrders.size(); j++) {
            if (currOrders.get(j).getId_food() == viewHolder.food.getServer_id()) {
                orderThisFood = currOrders.get(j);
                break;
            }
        }
        if (orderThisFood == null) {
            viewHolder.bFoodRVAddRemoveFromCart.setVisibility(View.GONE);
            viewHolder.bFoodRVToCart.setVisibility(View.VISIBLE);
        } else {
            viewHolder.bFoodRVToCart.setVisibility(View.GONE);
            viewHolder.bFoodRVAddRemoveFromCart.setVisibility(View.VISIBLE);
            viewHolder.tvFoodRVCount.setText(orderThisFood.getCount());
        }

        //set listeners
        //ListenerToCart
        ListenerToCart listenerToCart = new ListenerToCart(cart, viewHolder, component);
        viewHolder.bFoodRVToCart.setOnClickListener(listenerToCart);
        //ListenerRemoveFromCart
        //TODO
        //ListenerAddToCart
        //TODO

        //specific

        //ACTION_GET_FOOD_CURR_ORDERED
        //TODO set notice
        //TODO set total cost
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

        //ACTION_GET_FOOD_FAVORITE
        if (action == Constants.ACTION_GET_FOOD_FAVORITE) {
            AddRemoveFav addRemoveFav = new AddRemoveFav(viewHolder, i, action, handler, changeElement);
            viewHolder.itemView.setOnLongClickListener(addRemoveFav);
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

    private static class ListenerToCart implements View.OnClickListener {
        private Cart cart;
        private FoodViewHolder viewHolder;
        private RealmComponent component;

        public ListenerToCart(Cart cart, FoodViewHolder viewHolder, RealmComponent component) {
            this.cart = cart;
            this.viewHolder = viewHolder;
            this.component = component;
        }

        @Override
        public void onClick(View v) {
            Log.d("dtagRecyclerView", "Add to cart = " + viewHolder.food.getServer_id());
            //save 1 food to order
            Order order = new Order(cart.getCurrentOrderID(), viewHolder.food.getServer_id(), 1, viewHolder.food.getPrice());
            component.orderRepository().add(order);
            //show order control buttons
            viewHolder.bFoodRVToCart.setVisibility(View.GONE);
            viewHolder.bFoodRVAddRemoveFromCart.setVisibility(View.VISIBLE);
            viewHolder.tvFoodRVCount.setText("1");
        }
    }


    private static class ListenerRemoveFromCart implements View.OnClickListener {
        private Cart cart;
        private FoodViewHolder viewHolder;
        private RealmComponent component;

        public ListenerRemoveFromCart(Cart cart, FoodViewHolder viewHolder, RealmComponent component) {
            this.cart = cart;
            this.viewHolder = viewHolder;
            this.component = component;
        }

        @Override
        public void onClick(View v) {
            //remove 1 food from order
            //TODO
            //check is order has 0 food, if has< then remove order
            //TODO
            //check if order removes, then change control button
            //TODO

            //update list
            //handler.sendEmptyMessage(42);
        }
    }


    private static class AddRemoveFav implements View.OnLongClickListener {
        DatabaseHelper dbHelper;
        FoodViewHolder viewHolder;
        int elementNum;
        byte action;
        Set<Integer> changeElement;


        public AddRemoveFav(DatabaseHelper dbHelper, FoodViewHolder viewHolder, int elementNum, byte action, Handler handler, Set<Integer> changeElement) {
            this.dbHelper = dbHelper;
            this.viewHolder = viewHolder;
            this.action = action;
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
        return foods.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}