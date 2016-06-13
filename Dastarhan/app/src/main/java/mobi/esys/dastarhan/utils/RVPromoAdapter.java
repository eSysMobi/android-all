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

import java.util.ArrayList;
import java.util.List;

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
    private List<String> gifts_names;
    private List<String> conditions_names;

    //constructor
    public RVPromoAdapter(DatabaseHelper dbHelper, Context mContext, String locale) {
        this.mContext = mContext;
        this.locale = locale;
        gifts_names = new ArrayList<>();
        conditions_names = new ArrayList<>();
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
        TextView tvPromoAfflicted;
        TextView tvPromoAfflicted_0;
        TextView tvPromoTimeOfActionDays;
        TextView tvPromoTimeOfActionExact;
        TextView tvPromoConditionsExact;
        int restaraunt_id = 0;

        PromoViewHolder(View itemView) {
            super(itemView);
            tvPromoHeader = (TextView) itemView.findViewById(R.id.tvPromoHeader);
            ivPromoRestaurant = (ImageView) itemView.findViewById(R.id.ivPromoRestaurant);
            tvPromoRestaurant = (TextView) itemView.findViewById(R.id.tvPromoRestaurant);
            ivPromoRestaurantRating = (ImageView) itemView.findViewById(R.id.ivPromoRestaurantRating);
            tvPromoAfflicted = (TextView) itemView.findViewById(R.id.tvPromoAfflicted);
            tvPromoAfflicted_0 = (TextView) itemView.findViewById(R.id.tvPromoAfflicted_0);
            tvPromoTimeOfActionDays = (TextView) itemView.findViewById(R.id.tvPromoTimeOfActionDays);
            tvPromoTimeOfActionExact = (TextView) itemView.findViewById(R.id.tvPromoTimeOfActionExact);
            tvPromoConditionsExact = (TextView) itemView.findViewById(R.id.tvPromoConditionsExact);
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

        int restaurant_rating = cursor.getInt(cursor.getColumnIndex("total_rating"));
        switch (restaurant_rating) {
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

        //gifts
        String gift_type = cursor.getString(cursor.getColumnIndex("gift_type"));
        String gift = cursor.getString(cursor.getColumnIndex("gift"));
        int gift_condition = 0;
        switch (gift_type) {
            case Constants.GIFT_TYPE_DISCOUNT_PERCENT_ALL:
                //Скидка в процентах на весь заказ
                viewHolder.tvPromoHeader.setText(mContext.getString(R.string.discount) + " " + gift + "%");
                if (gifts_names.size() <= i) {
                    gifts_names.add("0");
                }
                viewHolder.tvPromoAfflicted_0.setText(R.string.afflicted_all_order);
                break;
            case Constants.GIFT_TYPE_DISCOUNT_AMOUNT_ALL:
                //Скидка в рублях на весь заказ
                viewHolder.tvPromoHeader.setText(mContext.getString(R.string.discount) + " " + gift + Constants.CURRENCY_VERY_SHORT);
                if (gifts_names.size() <= i) {
                    gifts_names.add("0");
                }
                viewHolder.tvPromoAfflicted_0.setText(R.string.afflicted_all_order);
                break;
            case Constants.GIFT_TYPE_DISCOUNT_PERCENT_OFFER:
                //Скидка в процентах на блюда, обозначенные в условиях акции
                gift_condition = cursor.getInt(cursor.getColumnIndex("gift_condition"));
                if (gift_condition > 0) {
                    viewHolder.tvPromoAfflicted.setText(R.string.afflicted_to_or);
                } else {
                    viewHolder.tvPromoAfflicted.setText(R.string.afflicted_to_and);
                }
                viewHolder.tvPromoHeader.setText(mContext.getString(R.string.discount) + " " + gift + "%");
                if (gifts_names.size() <= i) {
                    gifts_names.add(getFoodNames(cursor.getString(cursor.getColumnIndex("gift")), true));
                }
                viewHolder.tvPromoAfflicted_0.setText(gifts_names.get(i));
                break;
            case Constants.GIFT_TYPE_DISCOUNT_AMOUNT_OFFER:
                //Скидка в рублях на блюда, обозначенные в условиях акции
                gift_condition = cursor.getInt(cursor.getColumnIndex("gift_condition"));
                if (gift_condition > 0) {
                    viewHolder.tvPromoAfflicted.setText(R.string.afflicted_to_or);
                } else {
                    viewHolder.tvPromoAfflicted.setText(R.string.afflicted_to_and);
                }
                viewHolder.tvPromoHeader.setText(mContext.getString(R.string.discount) + " " + gift + Constants.CURRENCY_VERY_SHORT);
                if (gifts_names.size() <= i) {
                    gifts_names.add(getFoodNames(cursor.getString(cursor.getColumnIndex("gift")), true));
                }
                viewHolder.tvPromoAfflicted_0.setText(gifts_names.get(i));
                break;
            case Constants.GIFT_TYPE_GIFT_GOODS:
                //Блюда, которые получают в подарок
                gift_condition = cursor.getInt(cursor.getColumnIndex("gift_condition"));
                if (gift_condition > 0) {
                    viewHolder.tvPromoAfflicted.setText(R.string.afflicted_to_or);
                } else {
                    viewHolder.tvPromoAfflicted.setText(R.string.afflicted_to_and);
                }
                viewHolder.tvPromoHeader.setText(R.string.meal_for_gift);
                if (gifts_names.size() <= i) {
                    gifts_names.add(getFoodNames(cursor.getString(cursor.getColumnIndex("gift")), true));
                }
                viewHolder.tvPromoAfflicted_0.setText(gifts_names.get(i));
                break;
            case Constants.GIFT_TYPE_FREE_DELIVERY:
                //Бесплатная доставка
                viewHolder.tvPromoHeader.setText(R.string.free_delivery);
                if (gifts_names.size() <= i) {
                    gifts_names.add("0");
                }
                viewHolder.tvPromoAfflicted_0.setText(R.string.afflicted_all_order);
                break;
            case Constants.GIFT_TYPE_HIDDEN:
                //Скрыто
                viewHolder.tvPromoHeader.setText(R.string.hidden);
                if (gifts_names.size() <= i) {
                    gifts_names.add("0");
                }
                viewHolder.tvPromoAfflicted_0.setText(R.string.hidden);
                break;
        }

        //days
        StringBuilder sb = new StringBuilder();
        String[] days = cursor.getString(cursor.getColumnIndex("days")).split(",");
        for (String day : days) {
            switch (Integer.parseInt(day)) {
                case 1:
                    sb.append(mContext.getString(R.string.day_1)).append(" ");
                    break;
                case 2:
                    sb.append(mContext.getString(R.string.day_2)).append(" ");
                    break;
                case 3:
                    sb.append(mContext.getString(R.string.day_3)).append(" ");
                    break;
                case 4:
                    sb.append(mContext.getString(R.string.day_4)).append(" ");
                    break;
                case 5:
                    sb.append(mContext.getString(R.string.day_5)).append(" ");
                    break;
                case 6:
                    sb.append(mContext.getString(R.string.day_6)).append(" ");
                    break;
                case 7:
                    sb.append(mContext.getString(R.string.day_7)).append(" ");
                    break;
            }
        }
        viewHolder.tvPromoTimeOfActionDays.setText(sb.toString());

        //exact data
        StringBuilder sb_exact_data = new StringBuilder();

        int date = cursor.getInt(cursor.getColumnIndex("date"));
        if (date > 0) {
            String date1 = cursor.getString(cursor.getColumnIndex("date1"));
            String date2 = cursor.getString(cursor.getColumnIndex("date2"));
            sb_exact_data
                    .append(mContext.getString(R.string.from)).append(" ").append(date1)
                    .append(" ").append(mContext.getString(R.string.to)).append(" ").append(date2);
        } else {
            sb_exact_data.append(mContext.getString(R.string.every_day));
        }

        int time = cursor.getInt(cursor.getColumnIndex("time"));
        if (time > 0) {
            String time1 = cursor.getString(cursor.getColumnIndex("time1"));
            String time2 = cursor.getString(cursor.getColumnIndex("time2"));
            sb_exact_data
                    .append(" ").append(mContext.getString(R.string.from)).append(" ").append(time1)
                    .append(" ").append(mContext.getString(R.string.to)).append(" ").append(time2);
        } else {
            sb_exact_data.append(" ").append(mContext.getString(R.string.around_the_clock));
        }

        viewHolder.tvPromoTimeOfActionExact.setText(sb_exact_data.toString());


        //conditions
        StringBuilder sb_condition = new StringBuilder();
        int condition = cursor.getInt(cursor.getColumnIndex("condition"));
        String condition_par = cursor.getString(cursor.getColumnIndex("condition_par"));
        String tmp = "";
        switch (condition) {
            case 1:
                if (conditions_names.size() <= i) {
                    tmp = sb_condition.append("Сумма заказа больше").append(" ").append(condition_par).append(Constants.CURRENCY_VERY_SHORT).toString();
                    conditions_names.add(tmp);
                }
                break;
            case 2:
                if (conditions_names.size() <= i) {
                    tmp = sb_condition.append("Покупка блюд").append(" ").append(getFoodNames(condition_par, false)).toString();
                    conditions_names.add(tmp);
                }
                break;
            case 3:
                if (conditions_names.size() <= i) {
                    tmp = sb_condition.append("Покупка блюда").append(" ").append(getFoodNames(condition_par,false)).toString();
                    conditions_names.add(tmp);
                }
                break;
            case 4:
                if (conditions_names.size() <= i) {
                    tmp = sb_condition.append("Покупка блюд из категории").append(" ").append(condition_par).toString();
                    conditions_names.add(tmp);
                }
                break;
            case 5:
                if (conditions_names.size() <= i) {
                    tmp = sb_condition.append("Введён промокод").toString();
                    conditions_names.add(tmp);
                }
                break;
            case 6:
                if (conditions_names.size() <= i) {
                    tmp = sb_condition.append("Без условий").toString();
                    conditions_names.add(tmp);
                }
                break;
        }

        viewHolder.tvPromoConditionsExact.setText(conditions_names.get(i));


        CustomClickListener customClickListener = new CustomClickListener(mContext, viewHolder.restaraunt_id);
        viewHolder.itemView.setOnClickListener(customClickListener);

        CustomLongClickListener customLongClickListener = new CustomLongClickListener(mContext, viewHolder.restaraunt_id);
        viewHolder.itemView.setOnLongClickListener(customLongClickListener);

        if (i == getItemCount() - 1) {
            //cursor.close();
            db.close();
        }
    }

    private String getFoodNames(String ids, boolean needLines) {
        String names = "";
        Cursor cursor_food = null;
        try {
            String selectQuery_food = "SELECT * FROM "
                    + Constants.DB_TABLE_FOOD
                    + " WHERE";
            String[] parts = ids.split(",");
            if (parts.length > 0) {
                for (String part : parts) {
                    //+ "server_id = res_id";
                    selectQuery_food = selectQuery_food + " server_id = " + part + " OR ";
                }
                selectQuery_food = selectQuery_food.substring(0, selectQuery_food.length() - 4);
                cursor_food = db.rawQuery(selectQuery_food, null);
            }
            if (cursor_food != null && cursor_food.moveToFirst()) {
                boolean firstSeparator = false;
                do {
                    if(firstSeparator){
                        if (needLines) {
                            names = names + System.getProperty("line.separator");
                        } else {
                            names = names + ", ";
                        }
                    }
                    if (locale.equals("ru")) {
                        String name = cursor_food.getString(cursor_food.getColumnIndexOrThrow("ru_name"));
                        if (name.isEmpty()) {
                            names = names + cursor_food.getString(cursor_food.getColumnIndexOrThrow("en_name"));
                        } else {
                            names = names + name;
                        }
                    } else {
                        String name = cursor_food.getString(cursor_food.getColumnIndexOrThrow("en_name"));
                        if (name.isEmpty()) {
                            names = names + cursor_food.getString(cursor_food.getColumnIndexOrThrow("ru_name"));
                        } else {
                            names = names + name;
                        }
                    }
                    firstSeparator = true;
                } while (cursor_food.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor_food != null) {
                cursor_food.close();
            }
        }
        if (names.isEmpty()) {
            names = "Food with this ID not exists in DB!";
        }
        return names;
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