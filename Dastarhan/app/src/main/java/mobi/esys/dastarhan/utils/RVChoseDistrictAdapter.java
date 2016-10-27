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
import mobi.esys.dastarhan.database.District;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVChoseDistrictAdapter extends RecyclerView.Adapter<RVChoseDistrictAdapter.DistrictViewHolder> {
    private final String TAG = "dtagRVChoseCityAdapter";

    CityOrDistrictChooser chooser;
    private List<District> districts;
    private String locale;

    //constructor
    public RVChoseDistrictAdapter(CityOrDistrictChooser chooser, List<District> districts, String locale) {
        this.chooser = chooser;
        this.districts = districts;
        this.locale = locale;
    }

    //preparing ViewHolder
    public static class DistrictViewHolder extends RecyclerView.ViewHolder {
        TextView mtvCityOrDistrict;

        DistrictViewHolder(View itemView) {
            super(itemView);
            mtvCityOrDistrict = (TextView) itemView.findViewById(R.id.tvCityOrDistrict);
        }
    }

    @Override
    public DistrictViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_choose_city_or_distcict_recyclerview, viewGroup, false);
        return new DistrictViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DistrictViewHolder viewHolder, int i) {
        String name;
        if (districts.get(i).getDistrictEnName() != null
                || districts.get(i).getDistrictRuName() != null
                || !districts.get(i).getDistrictEnName().isEmpty()
                || !districts.get(i).getDistrictRuName().isEmpty()) {

            if (locale.equals("ru")) {
                name = districts.get(i).getDistrictRuName();
                if (name == null || name.isEmpty()) {
                    name = districts.get(i).getDistrictEnName();
                }
            } else {
                name = districts.get(i).getDistrictEnName();
                if (name == null || name.isEmpty()) {
                    name = districts.get(i).getDistrictRuName();
                }
            }
            viewHolder.mtvCityOrDistrict.setText(name);

            final District chosenDistrict = districts.get(i);
            final String finalName = name;
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooser.chooseDistrict(chosenDistrict, finalName);
                }
            });
        } else {
            districts.remove(i);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return districts.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}