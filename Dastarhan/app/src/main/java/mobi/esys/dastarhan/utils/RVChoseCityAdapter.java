package mobi.esys.dastarhan.utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import mobi.esys.dastarhan.AddAddressActivity;
import mobi.esys.dastarhan.R;
import mobi.esys.dastarhan.database.City;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVChoseCityAdapter extends RecyclerView.Adapter<RVChoseCityAdapter.CityViewHolder> {
    private final String TAG = "dtagRVChoseCityAdapter";

    CityOrDistrictChooser chooser;
    private List<City> cities;
    private String locale;

    //constructor
    public RVChoseCityAdapter(CityOrDistrictChooser chooser, List<City> cities, String locale) {
        this.chooser = chooser;
        this.cities = cities;
        this.locale = locale;
    }

    //preparing ViewHolder
    public static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView mtvCityOrDistrict;

        CityViewHolder(View itemView) {
            super(itemView);
            mtvCityOrDistrict = (TextView) itemView.findViewById(R.id.tvCityOrDistrict);
        }
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_choose_city_or_distcict_recyclerview, viewGroup, false);
        return new CityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CityViewHolder viewHolder, int i) {
        String name;
        if (cities.get(i).getCityEnName() != null
                || cities.get(i).getCityRuName() != null
                || !cities.get(i).getCityEnName().isEmpty()
                || !cities.get(i).getCityRuName().isEmpty()) {

            if (locale.equals("ru")) {
                name = cities.get(i).getCityRuName();
                if (name == null || name.isEmpty()) {
                    name = cities.get(i).getCityEnName();
                }
            } else {
                name = cities.get(i).getCityEnName();
                if (name == null || name.isEmpty()) {
                    name = cities.get(i).getCityRuName();
                }
            }
            viewHolder.mtvCityOrDistrict.setText(name);

            final City chosenCity = cities.get(i);
            final String finalName = name;
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooser.chooseCity(chosenCity, finalName);
                }
            });
        } else {
            cities.remove(i);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}