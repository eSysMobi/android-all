package mobi.esys.dastarhan;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import mobi.esys.dastarhan.utils.RVFoodAdapterFavorite;

public class FavoriteFragment extends BaseFragment {

    private RecyclerView mrvFavorite;
    private final String TAG = "dtagFavorite";

    public FavoriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FavoriteFragment.
     */
    public static FavoriteFragment newInstance() {
        FavoriteFragment fragment = new FavoriteFragment();
        //args
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_favorite, container, false);

        mrvFavorite = (RecyclerView) view.findViewById(R.id.rvFavorite);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mrvFavorite.setLayoutManager(llm);

        return view;
    }

    @Override
    public void onResume() {
        updateFavoriteList();
        super.onResume();
    }

    private void updateFavoriteList() {
        String locale = getContext().getResources().getConfiguration().locale.getLanguage();
        RVFoodAdapterFavorite adapter = new RVFoodAdapterFavorite(mFragmentNavigation, (DastarhanApp) getActivity().getApplication(), locale);
        if (mrvFavorite.getAdapter() == null) {
            Log.d(TAG, "New adapter in mrvFavorite");
            mrvFavorite.setAdapter(adapter);
        } else {
            Log.d(TAG, "Swap adapter in mrvFavorite");
            mrvFavorite.swapAdapter(adapter, true);
        }
    }
}
