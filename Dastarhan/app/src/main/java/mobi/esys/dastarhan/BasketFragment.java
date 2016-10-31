package mobi.esys.dastarhan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import mobi.esys.dastarhan.utils.RVFoodAdapterCart;

public class BasketFragment extends BaseFragment implements RVFoodAdapterCart.Callback {

    private final String TAG = "dtagBasketActivity";

    private RecyclerView mrvOrders;
    private Button mbBasketAddAddress;
    private Button mbBasketSendOrder;
    private TextView mtvBasketTotalCost;

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
        mtvBasketTotalCost = (TextView) view.findViewById(R.id.tvBasketTotalCost);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mrvOrders.setLayoutManager(llm);

        mbBasketAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "User check address");
                startActivityForResult(new Intent(getContext(), AddAddressActivity.class), Constants.REQUEST_CODE_CART);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Constants.REQUEST_CODE_CART){
            if(resultCode== Activity.RESULT_OK){
                mbBasketSendOrder.setEnabled(true);
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
    public void enableAddAdressButton() {
        //we have items in basket, enable AddAddress button
        mbBasketAddAddress.setEnabled(true);
    }

    @Override
    public void refreshTotalCost(double totalCost) {
        //refresh total cost
        Log.d(TAG, "Refresh total cost");
        String text = getResources().getString(R.string.total_cost) + " " + totalCost + " " + getResources().getString(R.string.currency);
        mtvBasketTotalCost.setText(text);
    }
}
