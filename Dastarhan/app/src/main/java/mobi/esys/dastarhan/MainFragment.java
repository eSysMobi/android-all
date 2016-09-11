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
import android.widget.TextView;
import android.widget.Toast;

import mobi.esys.dastarhan.utils.RVCuisinesAdapter;

public class MainFragment extends BaseFragment {

    private final String TAG = "dtagMainActivity";

    private ProgressBar mpbCuisines;
    private RecyclerView mrvCuisines;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MainFragment.
     */
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        //args if need
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_main, container, false);
        mpbCuisines = (ProgressBar) view.findViewById(R.id.pbCuisines);
        mrvCuisines = (RecyclerView) view.findViewById(R.id.rvCuisines);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mrvCuisines.setLayoutManager(llm);

        updateCuisines();

        return view;
    }

    private void updateCuisines() {
        String locale = getActivity().getResources().getConfiguration().locale.getLanguage();
        RVCuisinesAdapter adapter = new RVCuisinesAdapter(mFragmentNavigation, (DastarhanApp) getActivity().getApplication(), locale);
        if (mrvCuisines.getAdapter() == null) {
            Log.d(TAG, "New adapter in mrvCuisines");
            mrvCuisines.setAdapter(adapter);
        } else {
            Log.d(TAG, "Swap adapter in mrvCuisines");
            mrvCuisines.swapAdapter(adapter, true);
        }

        mpbCuisines.setVisibility(View.GONE);
        mrvCuisines.setVisibility(View.VISIBLE);
    }

}
