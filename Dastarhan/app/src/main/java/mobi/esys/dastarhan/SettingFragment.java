package mobi.esys.dastarhan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingFragment extends BaseFragment {

    private static final String TAG = "dtagSettingFragment";
    private SharedPreferences prefs;

    private TextView tvSettingsAddAddess;
    private TextView tvSettingsAuthDeauth;
    private boolean isAuthorized;

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
        tvSettingsAddAddess = (TextView) view.findViewById(R.id.tvSettingsAddAddess);
        tvSettingsAuthDeauth = (TextView) view.findViewById(R.id.tvSettingsAuthDeauth);

        prefs = getActivity().getApplicationContext().getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE);
        isAuthorized = !prefs.getString(Constants.PREF_SAVED_LOGIN, "").isEmpty();

        if (isAuthorized) {
            //Deathorization
            tvSettingsAuthDeauth.setText(getResources().getString(R.string.deauthorize));
        } else {
            //Authorization
            tvSettingsAuthDeauth.setText(getResources().getString(R.string.authorize));
        }

        tvSettingsAuthDeauth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAuthorized) {
                    //Deathorization
                    isAuthorized = !isAuthorized;
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Constants.PREF_SAVED_LOGIN, "");
                    editor.putString(Constants.PREF_SAVED_PASS, "");
                    editor.putString(Constants.PREF_SAVED_AUTH_TOKEN, "");
                    editor.putInt(Constants.PREF_SAVED_USER_ID, -1);
                    editor.putBoolean(Constants.PREF_SAVED_AUTH_IS_PERSIST, false);
                    editor.apply();
                    tvSettingsAuthDeauth.setText(getResources().getString(R.string.authorize));
                } else {
                    isAuthorized = !isAuthorized;
                    //Authorization
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivityForResult(intent, Constants.REQUEST_CODE_SETTINGS);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Login in settings requestCode " + requestCode + " resultCode " + resultCode);
        if (requestCode == Constants.REQUEST_CODE_SETTINGS) {
            isAuthorized = !prefs.getString(Constants.PREF_SAVED_LOGIN, "").isEmpty();
            if (isAuthorized) {
                //Deathorization
                tvSettingsAuthDeauth.setText(getResources().getString(R.string.deauthorize));
            } else {
                //Authorization
                tvSettingsAuthDeauth.setText(getResources().getString(R.string.authorize));
            }
        }
    }
}
