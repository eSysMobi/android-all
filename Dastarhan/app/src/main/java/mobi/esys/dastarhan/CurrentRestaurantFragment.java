package mobi.esys.dastarhan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import mobi.esys.dastarhan.database.RealmComponent;
import mobi.esys.dastarhan.database.Restaurant;

public class CurrentRestaurantFragment extends BaseFragment {

    private static final String ARG_RESTAURANT = "restaurant_id";
    private final String TAG = "dtagCurrRestActivity";
    private TextView mtvCurrRestName;
    private SimpleRatingBar mCurrRestRating;
    private ImageView mivCurrRestImage;
    private ImageView mivCurrRestVegan;
    private FrameLayout mflCurrRestInfo;
    private FrameLayout mflCurrRestRating;
    private TextView tvCurrRestRecomendationCount;

    private RealmComponent component;
    private Restaurant restaurant;

    public CurrentRestaurantFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CurrentRestaurantFragment.
     */
    public static CurrentRestaurantFragment newInstance(int restID) {
        CurrentRestaurantFragment fragment = new CurrentRestaurantFragment();
        //args
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_RESTAURANT, restID);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_current_restaurant, container, false);

        Bundle bundle = getArguments();
        int restID = bundle.getInt(ARG_RESTAURANT, -42);
        Log.d(TAG, "Start getting info from DB about restaurant with id " + restID);

        component = ((DastarhanApp) getActivity().getApplication()).realmComponent();

        mtvCurrRestName = (TextView) view.findViewById(R.id.tvCurrRestName);
        mCurrRestRating = (SimpleRatingBar) view.findViewById(R.id.сurrRestRating);
        mivCurrRestImage = (ImageView) view.findViewById(R.id.ivCurrRestImage);
        mivCurrRestVegan = (ImageView) view.findViewById(R.id.ivCurrRestVegan);
        mflCurrRestInfo = (FrameLayout) view.findViewById(R.id.flCurrRestInfo);
        mflCurrRestRating = (FrameLayout) view.findViewById(R.id.flCurrRestRating);
        tvCurrRestRecomendationCount = (TextView) view.findViewById(R.id.tvCurrRestRecomendationCount);


        restaurant = component.restaurantRepository().getById(restID);
        if (restaurant != null) {
            String locale = getContext().getResources().getConfiguration().locale.getLanguage();
            TextView mtvCurrRestDesrc = (TextView) view.findViewById(R.id.tvCurrRestDesrc);

            if (locale.equals("ru")) {
                mtvCurrRestName.setText(restaurant.getRu_name());
                mtvCurrRestDesrc.setText(restaurant.getAdditional_ru());
            } else {
                mtvCurrRestName.setText(restaurant.getEn_name());
                mtvCurrRestDesrc.setText(restaurant.getAdditional_en());
            }

            tvCurrRestRecomendationCount.setText(String.valueOf(restaurant.getTotal_votes()));

            mCurrRestRating.setRating(restaurant.getTotal_rating());
            mflCurrRestRating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRateDialog();
                }
            });

            if (restaurant.getVegetarian() == 1) {
                mivCurrRestVegan.setVisibility(View.GONE);
            }

            //ste "open hours"
            TextView mtvCurrRestTime = (TextView) view.findViewById(R.id.tvCurrRestTime);
            String openHours = "";
            if (restaurant.getTime1().length() > 5) {
                openHours = restaurant.getTime1().substring(0, 5) + " - ";
            } else {
                openHours = restaurant.getTime1() + " - ";
            }
            if (restaurant.getTime2().length() > 5) {
                openHours = openHours + restaurant.getTime2().substring(0, 5);
            } else {
                openHours = openHours + restaurant.getTime2();
            }
            mtvCurrRestTime.setText(openHours);

            //set schedule
            TextView mtvCurrRestSchedule = (TextView) view.findViewById(R.id.tvCurrRestSchedule);
            String schedule = restaurant.getSchedule();
            String setSchedule = "";
            if (schedule.contains("1")) setSchedule = setSchedule + "Mon, ";
            if (schedule.contains("2")) setSchedule = setSchedule + "Tue, ";
            if (schedule.contains("3")) setSchedule = setSchedule + "Wed, ";
            if (schedule.contains("4")) setSchedule = setSchedule + "Thu, ";
            if (schedule.contains("5")) setSchedule = setSchedule + "Fri, ";
            if (schedule.contains("6")) setSchedule = setSchedule + "Sat, ";
            if (schedule.contains("7")) setSchedule = setSchedule + "Sun, ";
            if (setSchedule.endsWith(", "))
                setSchedule = setSchedule.substring(0, setSchedule.length() - 2);
            mtvCurrRestSchedule.setText(setSchedule);

            //set phone
            TextView mtvCurrRestTel = (TextView) view.findViewById(R.id.tvCurrRestTel);
            if (!restaurant.getPhone().isEmpty() && !restaurant.getMobile().isEmpty()) {
                String setPhone = restaurant.getPhone();
                setPhone = setPhone + ", " + restaurant.getMobile();
                if (setPhone.endsWith(", "))
                    setPhone = setPhone.substring(0, setPhone.length() - 2);
                mtvCurrRestTel.setText(setPhone);
            } else {
                mtvCurrRestTel.setVisibility(View.GONE);
                TextView mtvCurrRestInfoTel = (TextView) view.findViewById(R.id.tvCurrRestInfoTel);
                mtvCurrRestInfoTel.setVisibility(View.GONE);
            }


            TextView mtvCurrRestTelOrder = (TextView) view.findViewById(R.id.tvCurrRestTelOrder);
            if (!restaurant.getOrder_phone().isEmpty()) {
                mtvCurrRestTelOrder.setText(restaurant.getOrder_phone());
            } else {
                mtvCurrRestTelOrder.setVisibility(View.GONE);
                TextView mtvCurrRestInfoTelOrder = (TextView) view.findViewById(R.id.tvCurrRestInfoTelOrder);
                mtvCurrRestInfoTelOrder.setVisibility(View.GONE);
            }

            TextView mtvCurrRestEmail = (TextView) view.findViewById(R.id.tvCurrRestEmail);
            if (!restaurant.getEmail1().isEmpty() && !restaurant.getEmail2().isEmpty()) {
                mtvCurrRestEmail.setText(restaurant.getEmail1() + " " + restaurant.getEmail2());
            } else {
                mtvCurrRestEmail.setVisibility(View.GONE);
                TextView mtvCurrRestInfoEmail = (TextView) view.findViewById(R.id.tvCurrRestInfoEmail);
                mtvCurrRestInfoEmail.setVisibility(View.GONE);
            }

            TextView mtvCurrRestPayment = (TextView) view.findViewById(R.id.tvCurrRestPayment);
            mtvCurrRestPayment.setText(restaurant.getPayment_methods());
        } else {
            hideNotFoundData();
            Log.e(TAG, "No restaurant with this ID ");
        }

        return view;
    }

    private void showRateDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alertdialog_rate_rest, null);
        dialogBuilder.setView(dialogView);

        final SimpleRatingBar rating = (SimpleRatingBar) dialogView.findViewById(R.id.сurrRestRatingAD);

        dialogBuilder.setTitle("Please rate the restaurant");
        dialogBuilder.setPositiveButton("Vote", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                Log.d(TAG, "Rating is " + rating.getRating());

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


    private void hideNotFoundData() {
        TextView mtvCurrRestNotFound = (TextView) getView().findViewById(R.id.tvCurrRestNotFound);
        if (mtvCurrRestNotFound != null) {
            mtvCurrRestNotFound.setVisibility(View.VISIBLE);
        }
        mtvCurrRestName.setVisibility(View.GONE);
        mCurrRestRating.setVisibility(View.GONE);
        mivCurrRestImage.setVisibility(View.GONE);
        mivCurrRestVegan.setVisibility(View.GONE);
        mflCurrRestInfo.setVisibility(View.GONE);
    }
}
