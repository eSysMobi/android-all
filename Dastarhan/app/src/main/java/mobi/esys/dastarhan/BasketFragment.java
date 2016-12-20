package mobi.esys.dastarhan;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import mobi.esys.dastarhan.database.CartRepository;
import mobi.esys.dastarhan.database.Order;
import mobi.esys.dastarhan.database.Restaurant;
import mobi.esys.dastarhan.database.RestaurantRepository;
import mobi.esys.dastarhan.database.UserInfoRepository;
import mobi.esys.dastarhan.net.APISendOrder;
import mobi.esys.dastarhan.utils.RVFoodAdapterCart;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BasketFragment extends BaseFragment implements RVFoodAdapterCart.Callback, NetworkResult {

    private final String TAG = "dtagBasketActivity";

    @Inject
    RestaurantRepository restaurantRepo;
    @Inject
    CartRepository cartRepository;
    @Inject
    UserInfoRepository userInfoRepository;
    @Inject
    Retrofit retrofit;
    private APISendOrder apiSendOrder;
    private RecyclerView mrvOrders;
    private Button mbBasketAddAddress;
    private Button mbBasketSendOrder;
    private TextView mtvBasketCost;
    private LinearLayout mlBasketDelivery;
    private TextView mtvBasketDeliveryTime;
    private TextView mtvBasketDeliveryCost;
    private TextView mtvBasketDeliveryTotalCost;

    private SharedPreferences prefs;
    private HashMap<Integer, Double> ordersInfo = new HashMap<>();
    private HashMap<Integer, Double> minDeliveryCosts;

    private double costOrder = 0;
    private double costDelivery = 0;

    private boolean isCanSendOrder = true;

    public BasketFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FoodFragment.
     */
    public static BasketFragment newInstance() {
        BasketFragment fragment = new BasketFragment();
        //args
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_basket, container, false);
        ((DastarhanApp) getActivity().getApplication()).appComponent().inject(this);

        mrvOrders = (RecyclerView) view.findViewById(R.id.rvOrders);
        mbBasketAddAddress = (Button) view.findViewById(R.id.bBasketAddAddress);
        mbBasketSendOrder = (Button) view.findViewById(R.id.bBasketSendOrder);
        mtvBasketCost = (TextView) view.findViewById(R.id.tvBasketCost);
        mlBasketDelivery = (LinearLayout) view.findViewById(R.id.lBasketDelivery);
        mtvBasketDeliveryTime = (TextView) view.findViewById(R.id.tvBasketDeliveryTime);
        mtvBasketDeliveryCost = (TextView) view.findViewById(R.id.tvBasketDeliveryCost);
        mtvBasketDeliveryTotalCost = (TextView) view.findViewById(R.id.tvBasketDeliveryTotalCost);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mrvOrders.setLayoutManager(llm);

        prefs = getActivity().getSharedPreferences(Constants.APP_PREF, Application.MODE_PRIVATE);

        apiSendOrder = retrofit.create(APISendOrder.class);

        mbBasketAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prefs.getInt(Constants.PREF_SAVED_USER_ID, -1) == -1
                        || prefs.getString(Constants.PREF_SAVED_LOGIN, "").isEmpty()
                        || prefs.getString(Constants.PREF_SAVED_PASS, "").isEmpty()
                        || prefs.getString(Constants.PREF_SAVED_AUTH_TOKEN, "").isEmpty()) {
                    //authorize
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivityForResult(intent, Constants.REQUEST_CODE_CART_AUTH);
                } else {
                    Log.d(TAG, "User check address");
                    final Intent intent = new Intent(getContext(), AddAddressActivity.class);
                    intent.putExtra(AddAddressActivity.DELIVERY_REST_IDS, ordersInfo);
                    startActivityForResult(intent, Constants.REQUEST_CODE_CART);
                }
            }
        });

        mbBasketSendOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Integer addressID = userInfoRepository.get().getServerAddressID();
                final int userID = prefs.getInt(Constants.PREF_SAVED_USER_ID, -1);
                final String authToken = prefs.getString(Constants.PREF_SAVED_AUTH_TOKEN, "");
                if (checkDeliveryMinCosts() && isCanSendOrder && addressID != null && userID!=-1 && !authToken.isEmpty()) {
                    Log.d(TAG, "All ok we can choose payment method adn send order");
                    AlertDialog.Builder ab = new AlertDialog.Builder(v.getContext());
                    ab.setTitle("Payment method");
                    ab.setMessage("Choose payment method");
                    ab.setPositiveButton("Card", new SendOrderListener(cartRepository, apiSendOrder, userID, authToken, addressID, false, BasketFragment.this));
                    ab.setNeutralButton("Money", new SendOrderListener(cartRepository, apiSendOrder, userID, authToken, addressID, true, BasketFragment.this));
                    ab.setNegativeButton("Cancel", null);
                    ab.setCancelable(true);
                    ab.show();
                }
            }
        });

        return view;
    }

    @Override
    public void onSuccessNetResponse() {
        isCanSendOrder = true;
        cartRepository.closeCart();
        updateList();
    }

    @Override
    public void onFailNetResponse() {
        isCanSendOrder = true;
        Toast.makeText(getContext(),"Can't send order, network error",Toast.LENGTH_SHORT).show();
    }

    private static class SendOrderListener implements DialogInterface.OnClickListener {
        private CartRepository cartRepository;
        private APISendOrder apiSendOrder;
        private int userID;
        private String apiKey;
        private int addressID;
        private boolean moneyPayment;
        private NetworkResult callback;

        public SendOrderListener(CartRepository cartRepository, APISendOrder apiSendOrder, int userID, String apiKey, int addressID, boolean moneyPayment, NetworkResult callback) {
            this.cartRepository = cartRepository;
            this.apiSendOrder = apiSendOrder;
            this.userID = userID;
            this.apiKey = apiKey;
            this.addressID = addressID;
            this.moneyPayment = moneyPayment;
            this.callback = callback;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            final List<Order> currentCartOrders = cartRepository.getCurrentCartOrders();
            Map<String, String> foodOrders = new TreeMap<>();
            for (int i = 0; i < currentCartOrders.size(); i++) {
                //    itemX - food id, где X - 1,2,3,4...N
                //    qtX - количество этой еды
                final Order order = currentCartOrders.get(i);
                foodOrders.put("item" + i, String.valueOf(order.getId_food()));
                foodOrders.put("qt" + i, String.valueOf(order.getCount()));
            }
            String paymentMethod;
            if(moneyPayment){
                paymentMethod = "m";
            } else {
                paymentMethod = "c";
            }
            final Call<ResponseBody> call = apiSendOrder.sendOrder(userID, apiKey, addressID, paymentMethod, foodOrders);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    //TODO
                    callback.onSuccessNetResponse();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    //TODO handle auth error
                    callback.onFailNetResponse();
                }
            });
        }
    }

    private boolean checkDeliveryMinCosts() {
        boolean result = true;
        Pair<Integer, Double> additionalToOrder = null;
        if (minDeliveryCosts == null
                || minDeliveryCosts.isEmpty()
                || ordersInfo == null
                || ordersInfo.isEmpty()
                || ordersInfo.size() != minDeliveryCosts.size()) {
            result = false;
        } else {
            final Set<Map.Entry<Integer, Double>> minDeliveryEntries = minDeliveryCosts.entrySet();
            for (Map.Entry<Integer, Double> minDeliveryEntry : minDeliveryEntries) {
                final Integer key = minDeliveryEntry.getKey();
                if (key != null && ordersInfo.containsKey(key)) {
                    if (ordersInfo.get(key) <= minDeliveryEntry.getValue()) {
                        additionalToOrder = new Pair<>(key, minDeliveryEntry.getValue() - ordersInfo.get(key));
                        result = false;
                        break;
                    }
                } else {
                    result = false;
                    break;
                }
            }
        }
        if (additionalToOrder != null && additionalToOrder.first != null) {
            final Restaurant restaurant = restaurantRepo.getById(additionalToOrder.first);
            if (restaurant != null) {
                final String locale = getResources().getConfiguration().locale.getLanguage();
                String restName = "";
                final String ru_name = restaurant.getRu_name();
                final String en_name = restaurant.getEn_name();
                if (locale.equals("ru")) {
                    if (ru_name == null || ru_name.isEmpty()) {
                        if (en_name == null || en_name.isEmpty()) {
                            restName = en_name;
                        }
                    } else {
                        restName = ru_name;
                    }
                } else {
                    if (en_name == null || en_name.isEmpty()) {
                        if (ru_name == null || ru_name.isEmpty()) {
                            restName = ru_name;
                        }
                    } else {
                        restName = en_name;
                    }
                }
                String message = getContext().getString(R.string.need_add_for_delivery1) + restName + getContext().getString(R.string.need_add_for_delivery2) + additionalToOrder.second;
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        }
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_CART && resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(AddAddressActivity.DELIVERY_MIN_ORDER)
                    && data.hasExtra(AddAddressActivity.DELIVERY_TIME)
                    && data.hasExtra(AddAddressActivity.DELIVERY_COST)) {
                costDelivery = data.getDoubleExtra(AddAddressActivity.DELIVERY_COST, 0);
                mtvBasketDeliveryTime.setText(data.getStringExtra(AddAddressActivity.DELIVERY_TIME));
                final String showedDeliveryCost = String.valueOf(costDelivery) + " " + getResources().getString(R.string.currency);
                mtvBasketDeliveryCost.setText(showedDeliveryCost);
                final String showedTotalCost = String.valueOf(costDelivery + costOrder) + " " + getResources().getString(R.string.currency);
                mtvBasketDeliveryTotalCost.setText(showedTotalCost);

                mlBasketDelivery.setVisibility(View.VISIBLE);
                minDeliveryCosts = (HashMap<Integer, Double>) data.getSerializableExtra(AddAddressActivity.DELIVERY_MIN_ORDER);
                if (checkDeliveryMinCosts()) {
                    mbBasketSendOrder.setEnabled(true);
                }
            }
        }
        if (requestCode == Constants.REQUEST_CODE_CART_AUTH && resultCode == Activity.RESULT_OK) {
            if (prefs.getInt(Constants.PREF_SAVED_USER_ID, -1) != -1
                    && !prefs.getString(Constants.PREF_SAVED_LOGIN, "").isEmpty()
                    && !prefs.getString(Constants.PREF_SAVED_PASS, "").isEmpty()
                    && !prefs.getString(Constants.PREF_SAVED_AUTH_TOKEN, "").isEmpty()) {
                Log.d(TAG, "User check address after manual authorizatoin");
                final Intent intent = new Intent(getContext(), AddAddressActivity.class);
                intent.putExtra(AddAddressActivity.DELIVERY_REST_IDS, ordersInfo);
                startActivityForResult(intent, Constants.REQUEST_CODE_CART);
            }
        }
    }

    @Override
    public void onResume() {
        updateList();
        super.onResume();
    }

    protected void updateList() {
        Log.d(TAG, "Refresh RecyclerView");
        String locale = getContext().getResources().getConfiguration().locale.getLanguage();
        RVFoodAdapterCart adapter = new RVFoodAdapterCart(mFragmentNavigation, (DastarhanApp) getActivity().getApplication(), locale, this);
        if (mrvOrders.getAdapter() == null) {
            Log.d(TAG, "New adapter in mrvOrders");
            mrvOrders.setAdapter(adapter);
        } else {
            Log.d(TAG, "Swap adapter in mrvOrders");
            mrvOrders.swapAdapter(adapter, true);
        }
    }

    @Override
    public void enableAddAddressButton() {
        //we have items in basket, enable AddAddress button
        mbBasketAddAddress.setEnabled(true);
    }

    @Override
    public void refreshTotalCost(double сost, Map<Integer, Double> ordersInfo, boolean updateDeliveryInfo) {
        //refresh total cost
        Log.d(TAG, "Refresh total cost");
        costOrder = сost;
        String text = getResources().getString(R.string.cost) + " " + сost + " " + getResources().getString(R.string.currency);
        mtvBasketCost.setText(text);
        this.ordersInfo.clear();
        this.ordersInfo.putAll(ordersInfo);
        if (updateDeliveryInfo) {
            //hide delivery info
            mbBasketSendOrder.setEnabled(false);
            mlBasketDelivery.setVisibility(View.GONE);
        }
    }
}
