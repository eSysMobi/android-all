package mobi.esys.dastarhan;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import mobi.esys.dastarhan.utils.RVRestaurantsAdapter;

public class Restaurants extends BaseFragment {

    private RecyclerView mrvRestaurants;
    private ProgressBar mpbRestaurants;
    private final String TAG = "dtagRestaurants";

    private int cuisineID;
    private static final String ARG_CUISINE = "cuisine_id";

    public Restaurants() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Restaurants.
     */
    public static Restaurants newInstance(int cuisineID) {
        Restaurants fragment = new Restaurants();
        //args
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_CUISINE, cuisineID);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_restaurants, container, false);
        Bundle bundle = getArguments();
        cuisineID = bundle.getInt(ARG_CUISINE, -42);
        mrvRestaurants = (RecyclerView) view.findViewById(R.id.rvRestaurants);
        mpbRestaurants = (ProgressBar) view.findViewById(R.id.pbRestaurants);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mrvRestaurants.setLayoutManager(llm);

        Log.d(TAG, "Cuisine ID from intent = " + cuisineID);

        updateRestaurants();

        return view;
    }

    private void updateRestaurants() {
        String locale = getContext().getResources().getConfiguration().locale.getLanguage();
        RVRestaurantsAdapter adapter = new RVRestaurantsAdapter(mFragmentNavigation, (DastarhanApp) getActivity().getApplication(), locale, cuisineID);
        if (mrvRestaurants.getAdapter() == null) {
            Log.d(TAG, "New adapter in mrvRestaurants");
            mrvRestaurants.setAdapter(adapter);
        } else {
            Log.d(TAG, "Swap adapter in mrvRestaurants");
            mrvRestaurants.swapAdapter(adapter, true);
        }

        mpbRestaurants.setVisibility(View.GONE);
        mrvRestaurants.setVisibility(View.VISIBLE);
    }
}
