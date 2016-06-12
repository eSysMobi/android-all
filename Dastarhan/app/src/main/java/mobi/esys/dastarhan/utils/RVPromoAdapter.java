package mobi.esys.dastarhan.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import mobi.esys.dastarhan.Constants;
import mobi.esys.dastarhan.CurrentRestaurantActivity;
import mobi.esys.dastarhan.FoodActivity;
import mobi.esys.dastarhan.R;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVPromoAdapter extends RecyclerView.Adapter<RVPromoAdapter.PromoViewHolder> {
    private Cursor cursor;
    private Context mContext;
    private SQLiteDatabase db;
    private String locale;

    //constructor
    public RVPromoAdapter(DatabaseHelper dbHelper, Context mContext, String locale) {
        this.mContext = mContext;
        this.locale = locale;
        db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT "
                + "a.ru_name as ru_name, "
                + "a.en_name as en_name, "
                + "a.total_rating as total_rating, "
                + "b.res_id as res_id, "
                + "b.server_id as server_id, "
                + "b.condition as condition, "
                + "b.condition_par as condition_par, "
                + "b.time as time, "
                + "b.time1 as time1, "
                + "b.time2 as time2, "
                + "b.days as days, "
                + "b.date as date, "
                + "b.date1 as date1, "
                + "b.date2 as date2, "
                + "b.gift_type as gift_type, "
                + "b.gift as gift, "
                + "b.gift_condition as gift_condition "
                + "FROM "
                + Constants.DB_TABLE_RESTAURANTS + " a, "
                + Constants.DB_TABLE_PROMO + " b "
                + "WHERE "
                + "a.server_id = b.res_id";

        cursor = db.rawQuery(selectQuery, null);
    }

    //preparing ViewHolder
    public static class PromoViewHolder extends RecyclerView.ViewHolder {
        TextView tvPromoHeader;
        ImageView ivPromoRestaurant;
        TextView tvPromoRestaurant;
        ImageView ivPromoRestaurantRating;
        TextView tvPromoAfflicted_0;
        TextView tvPromoTimeOfActionDays;
        TextView tvPromoTimeOfActionExact;
        TextView tvPromoConditions;
        int restaraunt_id = 0;

        PromoViewHolder(View itemView) {
            super(itemView);
            tvPromoHeader = (TextView) itemView.findViewById(R.id.tvPromoHeader);
            ivPromoRestaurant = (ImageView) itemView.findViewById(R.id.ivPromoRestaurant);
            tvPromoRestaurant = (TextView) itemView.findViewById(R.id.tvPromoRestaurant);
            ivPromoRestaurantRating = (ImageView) itemView.findViewById(R.id.ivPromoRestaurantRating);
            tvPromoAfflicted_0 = (TextView) itemView.findViewById(R.id.tvPromoAfflicted_0);
            tvPromoTimeOfActionDays = (TextView) itemView.findViewById(R.id.tvPromoTimeOfActionDays);
            tvPromoTimeOfActionExact = (TextView) itemView.findViewById(R.id.tvPromoTimeOfActionExact);
            tvPromoConditions = (TextView) itemView.findViewById(R.id.tvPromoConditions);
        }
    }

    @Override
    public RVPromoAdapter.PromoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_promo_recyclerview, viewGroup, false);
        PromoViewHolder promoViewHolder = new PromoViewHolder(v);
        return promoViewHolder;
    }

    @Override
    public void onBindViewHolder(RVPromoAdapter.PromoViewHolder viewHolder, int i) {
        cursor.moveToPosition(i);
        viewHolder.restaraunt_id = cursor.getInt(cursor.getColumnIndex("res_id"));
        if (locale.equals("ru")) {
            viewHolder.tvPromoRestaurant.setText(cursor.getString(cursor.getColumnIndex("ru_name")));
        } else {
            viewHolder.tvPromoRestaurant.setText(cursor.getString(cursor.getColumnIndex("en_name")));
        }

        int restaraunt_rating = cursor.getInt(cursor.getColumnIndex("total_rating"));
        switch (restaraunt_rating) {
            case 0:
                viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_0));
                break;
            case 1:
                viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_1));
                break;
            case 2:
                viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_2));
                break;
            case 3:
                viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_3));
                break;
            case 4:
                viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_4));
                break;
            case 5:
                viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_5));
                break;
            case 6:
                viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_6));
                break;
            case 7:
                viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_7));
                break;
            case 8:
                viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_8));
                break;
            case 9:
                viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_9));
                break;
            case 10:
                viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_10));
                break;
        }

        //TODO download and set image
        //viewHolder.ivPromoRestaurant.setImageBitmap(...);

        CustomClickListener customClickListener = new CustomClickListener(mContext, viewHolder.restaraunt_id);
        viewHolder.itemView.setOnClickListener(customClickListener);

        CustomLongClickListener customLongClickListener = new CustomLongClickListener(mContext, viewHolder.restaraunt_id);
        viewHolder.itemView.setOnLongClickListener(customLongClickListener);

        if (i == getItemCount() - 1) {
            //cursor.close();
            db.close();
        }
    }

    private static class CustomClickListener implements View.OnClickListener {
        private int id;
        private Context mContext;

        public CustomClickListener(Context mContext, int id) {
            this.mContext = mContext;
            this.id = id;
        }

        @Override
        public void onClick(View v) {
            Log.d("dtagRecyclerView", "Click PROMO in RecyclerView with id = " + id);
//            Intent intent = new Intent(mContext, FoodActivity.class);
//            intent.putExtra("restID",id);
//            intent.putExtra("cuisineID", -50);
//            mContext.startActivity(intent);
        }
    }

    private static class CustomLongClickListener implements View.OnLongClickListener {
        private int id;
        private Context mContext;

        public CustomLongClickListener(Context mContext, int id) {
            this.mContext = mContext;
            this.id = id;
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d("dtagRecyclerView", "Long click PROMO in RecyclerView with id = " + id);

//            Intent intent = new Intent(mContext, CurrentRestaurantActivity.class);
//            intent.putExtra("restID",id);
//            mContext.startActivity(intent);
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}