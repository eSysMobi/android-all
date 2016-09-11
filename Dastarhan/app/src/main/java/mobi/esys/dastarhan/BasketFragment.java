package mobi.esys.dastarhan;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import mobi.esys.dastarhan.utils.RVFoodAdapterCart;

public class BasketFragment extends BaseFragment {

    private final String TAG = "dtagBasketActivity";
    private final static int PERMISSION_REQUEST_CODE = 334;

    private Handler handler;

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

        handler = new BasketHandler();

        mrvOrders = (RecyclerView) view.findViewById(R.id.rvOrders);
        mbBasketAddAddress = (Button) view.findViewById(R.id.bBasketAddAddress);
        mbBasketSendOrder = (Button) view.findViewById(R.id.bBasketSendOrder);
        mtvBasketTotalCost = (TextView) view.findViewById(R.id.tvBasketTotalCost);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mrvOrders.setLayoutManager(llm);

        //TODO set notice

        mbBasketAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Add address");
                Intent intent = new Intent(getContext(), AddAddressActivity.class);
                startActivityForResult(intent, 88);
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            } else {
                mbBasketAddAddress.setEnabled(true);
            }
        } else {
            mbBasketAddAddress.setEnabled(true);
        }

        return view;
    }

    @Override
    public void onResume() {
        updateList();
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode " + requestCode + " resultCode " + resultCode);
        if (requestCode == 88 && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "all ok");
            mbBasketSendOrder.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mbBasketAddAddress.setEnabled(true);
                }
                break;
            }
        }
    }

    protected void updateList() {
        Log.d(TAG, "Refresh RecyclerView");
        String locale = getContext().getResources().getConfiguration().locale.getLanguage();
        RVFoodAdapterCart adapter = new RVFoodAdapterCart(mFragmentNavigation, (DastarhanApp) getActivity().getApplication(), locale, handler);
        if (mrvOrders.getAdapter() == null) {
            Log.d(TAG, "New adapter in mrvOrders");
            mrvOrders.setAdapter(adapter);
        } else {
            Log.d(TAG, "Swap adapter in mrvOrders");
            mrvOrders.swapAdapter(adapter, true);
        }
    }

    private class BasketHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 42:
                    //refresh list
                    updateList();
                    break;
                case 43:
                    //refresh total cost
                    Log.d(TAG, "Refresh total cost");
                    Bundle bundle = message.getData();
                    double totalCost = bundle.getDouble("totalCost", 0);
                    String text = getResources().getString(R.string.total_cost) + " " + totalCost + " " + getResources().getString(R.string.currency);
                    mtvBasketTotalCost.setText(text);
                    break;
            }
        }
    }
}
