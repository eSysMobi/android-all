package mobi.esys.dastarhan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingFragment extends BaseFragment {

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingFragment.
     */
    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        //args if need
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_setting, container, false);
        //do something
        return view;
    }
}
