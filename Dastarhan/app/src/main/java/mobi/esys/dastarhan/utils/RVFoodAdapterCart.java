package mobi.esys.dastarhan.utils;

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

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mobi.esys.dastarhan.AppComponent;
import mobi.esys.dastarhan.BaseFragment.FragmentNavigation;
import mobi.esys.dastarhan.Constants;
import mobi.esys.dastarhan.CurrentFoodFragment;
import mobi.esys.dastarhan.DastarhanApp;
import mobi.esys.dastarhan.R;
import mobi.esys.dastarhan.database.Cart;
import mobi.esys.dastarhan.database.Food;
import mobi.esys.dastarhan.database.FoodRepository;
import mobi.esys.dastarhan.database.Order;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVFoodAdapterCart extends RecyclerView.Adapter<RVFoodAdapterCart.FoodViewHolder> {
    private final ImageLoader imageLoader = ImageLoader.getInstance();
    private FragmentNavigation navigation;
    private AppComponent component;
    private String locale;
    private RVFoodAdapterCart.Callback callback;
    boolean needRefreshTotalCostEveryTime = false;

    private Map<Integer, Double> ordersInfo;
    private List<Food> foods;
    private List<Order> currOrders;
    private Cart cart;


    //constructor
    public RVFoodAdapterCart(FragmentNavigation navigation, DastarhanApp dastarhanApp, String locale, RVFoodAdapterCart.Callback callback) {
        this.navigation = navigation;
        component = dastarhanApp.appComponent();
        this.locale = locale;
        this.callback = callback;
        ordersInfo = new HashMap<>();
        foods = new ArrayList<>();

        cart = component.cartRepository().get();
        currOrders = component.cartRepository().getCurrentCartOrders();
        if (currOrders == null) {
            currOrders = new ArrayList<>();
        }
        FoodRepository foodRepo = component.foodRepository();
        Log.d("dtagRecyclerView", "Retrieve food from restaurants with IDs");
        if (currOrders.size() > 0) {
            Integer[] orderIDs = new Integer[currOrders.size()];
            for (int i = 0; i < currOrders.size(); i++)
                orderIDs[i] = currOrders.get(i).getId_food();
            foods = foodRepo.getByIds(orderIDs);
            if (!foods.isEmpty()) {
                //we have items in basket, enable AddAddress button
                callback.enableAddAddressButton();
            }
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
        final String ru_name = viewHolder.food.getRu_name();
        final String en_name = viewHolder.food.getEn_name();
        final String ru_descr = viewHolder.food.getRu_descr();
        final String en_descr = viewHolder.food.getEn_descr();
        if (locale.equals("ru")) {
            if (ru_name == null || ru_name.isEmpty()) {
                if (en_name == null || en_name.isEmpty()) {
                    viewHolder.tvFoodNameRV.setText(en_name);
                }
            } else {
                viewHolder.tvFoodNameRV.setText(ru_name);
            }
            if (ru_descr == null || ru_descr.isEmpty()) {
                if (en_descr != null && !en_descr.isEmpty()) {
                    viewHolder.tvFoodRVDescription.setText(en_descr);
                }
            } else {
                viewHolder.tvFoodRVDescription.setText(ru_descr);
            }
        } else {
            if (en_name == null || en_name.isEmpty()) {
                if (ru_name == null || ru_name.isEmpty()) {
                    viewHolder.tvFoodNameRV.setText(ru_name);
                }
            } else {
                viewHolder.tvFoodNameRV.setText(en_name);
            }
            if (en_descr == null || en_descr.isEmpty()) {
                if (ru_descr != null && !ru_descr.isEmpty()) {
                    viewHolder.tvFoodRVDescription.setText(ru_descr);
                }
            } else {
                viewHolder.tvFoodRVDescription.setText(en_descr);
            }
        }

        //set price
        String price = String.valueOf(viewHolder.food.getPrice()) + " р.";
        viewHolder.tvFoodPriceRV.setText(price);

        //set picture
        String smallPictureURL = viewHolder.food.getPictureSmall();
        if (smallPictureURL != null && !smallPictureURL.isEmpty()) {
            imageLoader.displayImage(Constants.API_BASE_URL + smallPictureURL, viewHolder.ivFoodRV);
        }

        //promo
        //TODO check and set promo in RVFoodAdapterMain
        boolean needPromo = false;
        if (needPromo) {
            viewHolder.ivFoodRVPromo.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivFoodRVPromo.setVisibility(View.GONE);
        }

        GoToFullFood goToFullFood = new GoToFullFood(navigation, viewHolder.food.getServer_id());
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
            viewHolder.tvFoodRVCount.setText(String.valueOf(orderThisFood.getCount()));
        }

        //set listeners
        //Button ToCart
        ListenerToCart listenerToCart = new ListenerToCart(viewHolder);
        viewHolder.bFoodRVToCart.setOnClickListener(listenerToCart);
        //Button RemoveOneFromCart
        ListenerRemoveOneFromCart listenerRemoveOneFromCart = new ListenerRemoveOneFromCart(viewHolder);
        viewHolder.bCartRemoveOne.setOnClickListener(listenerRemoveOneFromCart);
        //Button AddOneToCart
        ListenerAddOneToCart listenerAddOneFromCart = new ListenerAddOneToCart(viewHolder);
        viewHolder.bCartAddOne.setOnClickListener(listenerAddOneFromCart);

        //specific

        if (i == getItemCount() - 1) {
            needRefreshTotalCostEveryTime = true;
        }

        if (needRefreshTotalCostEveryTime) {
            updateTotalCost(false);
        }
    }

    private void updateTotalCost(boolean updateDeliveryInfo) {
        double totalCost = 0;
        for (Order order : currOrders) {
            totalCost = totalCost + (order.getPrice() * order.getCount());
            Integer restID = null;
            for(Food food:foods){
                if(food.getServer_id()==order.getId_food()){
                    restID = food.getRes_id();
                    break;
                }
            }
            if(restID!=null){
                if (ordersInfo.containsKey(restID)) {
                    final Double oldSum = ordersInfo.get(restID);
                    ordersInfo.put(restID, oldSum + (order.getPrice() * order.getCount()));
                } else {
                    //create
                    ordersInfo.put(restID, order.getPrice() * order.getCount());
                }
            }
        }

        callback.refreshTotalCost(totalCost, ordersInfo, updateDeliveryInfo);
    }

    private static class GoToFullFood implements View.OnClickListener {
        private int id;
        private FragmentNavigation navigation;

        public GoToFullFood(FragmentNavigation navigation, int id) {
            this.navigation = navigation;
            this.id = id;
        }

        @Override
        public void onClick(View v) {
            Log.d("dtagRecyclerView", "Choose FOOD in RecyclerView with id = " + id);
            CurrentFoodFragment fragment = CurrentFoodFragment.newInstance(id);
            navigation.replaceFragment(fragment, "");
        }
    }

    private class ListenerToCart implements View.OnClickListener {
        private FoodViewHolder viewHolder;

        public ListenerToCart(FoodViewHolder viewHolder) {
            this.viewHolder = viewHolder;
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
            currOrders.add(order);
            updateTotalCost(true);
        }
    }


    private class ListenerRemoveOneFromCart implements View.OnClickListener {
        private FoodViewHolder viewHolder;

        public ListenerRemoveOneFromCart(FoodViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            //remove 1 food from order
            //check is order has 0 food, if has < then remove order
            int targetPosition = -42;
            for (int i = 0; i < currOrders.size(); i++) {
                if (currOrders.get(i).getId_food() == viewHolder.food.getServer_id()) {
                    //update local
                    currOrders.get(i).setCount(currOrders.get(i).getCount() - 1);
                    //update in db
                    component.orderRepository().update(currOrders.get(i));
                    targetPosition = i;
                    break;
                }
            }

            //check if order removes, then change control button
            if (targetPosition != -42) {
                if (currOrders.get(targetPosition).getCount() > 0) {
                    viewHolder.tvFoodRVCount.setText(String.valueOf(currOrders.get(targetPosition).getCount()));
                } else {
                    viewHolder.bFoodRVToCart.setVisibility(View.VISIBLE);
                    viewHolder.bFoodRVAddRemoveFromCart.setVisibility(View.GONE);
                    currOrders.remove(currOrders.get(targetPosition));
                }
                updateTotalCost(true);
            }
        }
    }

    private class ListenerAddOneToCart implements View.OnClickListener {
        private FoodViewHolder viewHolder;

        public ListenerAddOneToCart(FoodViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            //add 1 food to order
            int targetPosition = -42;
            for (int i = 0; i < currOrders.size(); i++) {
                if (currOrders.get(i).getId_food() == viewHolder.food.getServer_id()) {
                    currOrders.get(i).setCount(currOrders.get(i).getCount() + 1);
                    component.orderRepository().update(currOrders.get(i));
                    targetPosition = i;
                    break;
                }
            }

            if (targetPosition != -42) {
                viewHolder.tvFoodRVCount.setText(String.valueOf(currOrders.get(targetPosition).getCount()));
            }
            updateTotalCost(true);
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

    public interface Callback {
        void enableAddAddressButton();

        void refreshTotalCost(double totalCost, Map<Integer, Double> ordersInfo, boolean updateDeliveryInfo);
    }
}