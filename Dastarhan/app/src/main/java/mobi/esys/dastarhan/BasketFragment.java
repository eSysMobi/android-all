package mobi.esys.dastarhan;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import mobi.esys.dastarhan.utils.RVFoodAdapterCart;

public class BasketFragment extends BaseFragment implements RVFoodAdapterCart.Callback {

    private final String TAG = "dtagBasketActivity";

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

    private double costOrder = 0;
    private double costDelivery = 0;

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
                Toast.makeText(v.getContext(), "Coming soon", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_CART && resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(AddAddressActivity.DELIVERY_MIN_ORDER)
                    && data.hasExtra(AddAddressActivity.DELIVERY_TIME)
                    && data.hasExtra(AddAddressActivity.DELIVERY_COST)) {
                //TODO check min cost here

                mlBasketDelivery.setVisibility(View.VISIBLE);
                mbBasketSendOrder.setEnabled(true);
                costDelivery = data.getDoubleExtra(AddAddressActivity.DELIVERY_COST, 0);
                mtvBasketDeliveryTime.setText(data.getStringExtra(AddAddressActivity.DELIVERY_TIME));
                final String showedDeliveryCost = String.valueOf(costDelivery) + " " + getResources().getString(R.string.currency);
                mtvBasketDeliveryCost.setText(showedDeliveryCost);
                final String showedTotalCost = String.valueOf(costDelivery+costOrder) + " " + getResources().getString(R.string.currency);
                mtvBasketDeliveryTotalCost.setText(showedTotalCost);
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
        if(updateDeliveryInfo){
            //hide delivery info
            mbBasketSendOrder.setEnabled(false);
            mlBasketDelivery.setVisibility(View.GONE);
        }
    }
}
