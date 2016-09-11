package mobi.esys.dastarhan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import mobi.esys.dastarhan.utils.RVPromoAdapter;

public class PromoFragment extends BaseFragment {

    private final String TAG = "dtagPromoActivity";
    private RecyclerView mrvPromo;

    public PromoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment PromoFragment.
     */
    public static PromoFragment newInstance() {
        PromoFragment fragment = new PromoFragment();
        //args if need
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_promo, container, false);

        mrvPromo = (RecyclerView) view.findViewById(R.id.rvPromo);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mrvPromo.setLayoutManager(llm);

        return view;
    }

    @Override
    public void onResume() {
        updatePromoList();
        super.onResume();
    }

    private void updatePromoList(){
        String locale = getContext().getResources().getConfiguration().locale.getLanguage();
        RVPromoAdapter adapter = new RVPromoAdapter(getContext(), mFragmentNavigation, (DastarhanApp) getActivity().getApplication(), locale);
        if (mrvPromo.getAdapter() == null) {
            Log.d(TAG, "New adapter in mrvPromo");
            mrvPromo.setAdapter(adapter);
        } else {
            Log.d(TAG, "Swap adapter in mrvPromo");
            mrvPromo.swapAdapter(adapter, true);
        }
    }
}
