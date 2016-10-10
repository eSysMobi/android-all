package mobi.esys.dastarhan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import mobi.esys.dastarhan.database.CommonOperation;
import mobi.esys.dastarhan.database.Food;
import mobi.esys.dastarhan.database.FoodRepository;
import mobi.esys.dastarhan.database.Order;

public class CurrentFoodFragment extends BaseFragment {

    private final String TAG = "dtagCurrentFood";
    private AppComponent component;
    private FoodRepository foodRepository;
    private Food food;

    private transient SharedPreferences prefs;

    private boolean canOrdered = true;
    private TextView mtvCurrFoodPrice;
    private TextView mtvCurrFoodName;
    private TextView mtvCurrFoodDescr;
    private Button mbCurrFoodAddShopping;
    private ImageView mivCurrFoodFavorite;

    private ImageView mivCurrFoodVegan;

    private static final String ARG_FOOD = "food_id";

    public CurrentFoodFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CurrentFoodFragment.
     */
    public static CurrentFoodFragment newInstance(int foodID) {
        CurrentFoodFragment fragment = new CurrentFoodFragment();
        //args
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_FOOD, foodID);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_current_food, container, false);
        Bundle bundle = getArguments();
        int currentFoodID = bundle.getInt(ARG_FOOD, -42);


        component = ((DastarhanApp) getActivity().getApplication()).appComponent();
        foodRepository = component.foodRepository();

        Log.d(TAG, "Choose FOOD ID from intent = " + currentFoodID);
        food = foodRepository.getById(currentFoodID);

        mtvCurrFoodPrice = (TextView) view.findViewById(R.id.tvCurrFoodPrice);
        mtvCurrFoodName = (TextView) view.findViewById(R.id.tvCurrFoodName);
        mtvCurrFoodDescr = (TextView) view.findViewById(R.id.tvCurrFoodDescr);
        mivCurrFoodFavorite = (ImageView) view.findViewById(R.id.ivCurrFoodFavorite);
        mivCurrFoodVegan = (ImageView) view.findViewById(R.id.ivCurrFoodVegan);
        mbCurrFoodAddShopping = (Button) view.findViewById(R.id.bCurrFoodAddShopping);

        //click favorite
        mivCurrFoodFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (food.isFavorite()) {
                    food.setFavorite(false);
                    mivCurrFoodFavorite.setImageDrawable(getResources().getDrawable(R.drawable.favorite_empty));
                } else {
                    food.setFavorite(true);
                    mivCurrFoodFavorite.setImageDrawable(getResources().getDrawable(R.drawable.favorite_full));
                }

                //update fav in DB
                foodRepository.updateFavorites(food.getServer_id(), food.isFavorite());
            }
        });

        //click order
        mbCurrFoodAddShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canOrdered) {
                    canOrdered = false;

                    CommonOperation.createOrder(component, food);

                    mbCurrFoodAddShopping.setText(R.string.cant_order);
                    mbCurrFoodAddShopping.setBackground(getResources().getDrawable(R.drawable.button_to_basket_selector));
                    Toast.makeText(getContext(), "Added to shopping list", Toast.LENGTH_SHORT).show();

                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        if (food != null) {
            //set data from DB to view

            String locale = getContext().getResources().getConfiguration().locale.getLanguage();

            List<Order> orders = component.cartRepository().getCurrentCartOrders();
            for (Order order : orders) {
                if (order.getId_food() == food.getServer_id()) {
                    canOrdered = false;
                }
            }

            if (canOrdered) {
                mbCurrFoodAddShopping.setText(R.string.to_order);
            } else {
                mbCurrFoodAddShopping.setText(R.string.cant_order);
            }

            if (locale.equals("ru")) {
                mtvCurrFoodName.setText(food.getRu_name());
            } else {
                mtvCurrFoodName.setText(food.getEn_name());
            }

            String priceString = String.valueOf(food.getPrice());
            priceString += " " + Constants.CURRENCY_VERY_SHORT;
            mtvCurrFoodPrice.setText(priceString);

            if (locale.equals("ru")) {
                mtvCurrFoodDescr.setText(food.getRu_descr());
            } else {
                mtvCurrFoodDescr.setText(food.getEn_descr());
            }

            //favorite
            if (food.isFavorite()) {
                mivCurrFoodFavorite.setImageDrawable(getResources().getDrawable(R.drawable.favorite_full));
            }

            if (food.isVegetarian()) {
                mivCurrFoodVegan.setVisibility(View.VISIBLE);
            } else {
                mivCurrFoodVegan.setVisibility(View.GONE);
            }
        } else {
            LinearLayout mllCurrentFood = (LinearLayout) getView().findViewById(R.id.llCurrentFood);
            if (mllCurrentFood != null) {
                mllCurrentFood.setVisibility(View.GONE);
            }
            FrameLayout mflCurrentFood = (FrameLayout) getView().findViewById(R.id.flCurrentFood);
            if (mflCurrentFood != null) {
                mflCurrentFood.setVisibility(View.GONE);
            }
            TextView mtvCurrFoodNotFound = (TextView) getView().findViewById(R.id.tvCurrFoodNotFound);
            if (mtvCurrFoodNotFound != null) {
                mtvCurrFoodNotFound.setVisibility(View.VISIBLE);
            }
        }
        super.onResume();
    }
}
